package com.notesync.notes.business.interactors.trash

import android.content.Context
import androidx.work.*
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import com.notesync.notes.framework.workers.DeleteDeletedNoteWorker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMultipleTrashNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val context: Context
) {

    // set true if an error occurs when deleting any of the notes from cache
    private var onDeleteError: Boolean = false

    /**
     * Logic:
     * 1. execute all the deletes and save result into an ArrayList<DataState<NoteListViewState>>
     * 2a. If one of the results is a failure, emit an "error" response
     * 2b. If all success, emit success response
     * 3. Update network with notes that were successfully deleted
     */
    fun deleteNotes(
        notes: List<Note>,
        stateEvent: StateEvent, user: User
    ): Flow<DataState<TrashViewState>?> = flow {

        val successfulDeletes: ArrayList<Note> = ArrayList() // notes that were successfully deleted
        for (note in notes) {
            val cacheResult = safeCacheCall(IO) {
                noteCacheDataSource.deleteTrashNote(note.id)
            }

            val response = object : CacheResponseHandler<TrashViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: Int): DataState<TrashViewState>? {
                    if (resultObj < 0) { // if error
                        onDeleteError = true
                    } else {
                        successfulDeletes.add(note)
                    }
                    return null
                }
            }.getResult()

            // check for random errors
            if (response?.stateMessage?.response?.message
                    ?.contains(stateEvent.errorInfo()) == true
            ) {
                onDeleteError = true
            }

        }

        if (onDeleteError) {
            emit(
                DataState.data<TrashViewState>(
                    response = Response(
                        message = DELETE_NOTES_ERRORS,
                        uiComponentType = UIComponentType.SnackBar(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        } else {
            emit(
                DataState.data<TrashViewState>(
                    response = Response(
                        message = DELETE_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }

        updateNetwork(successfulDeletes, user)
    }

    private suspend fun updateNetwork(successfulDeletes: ArrayList<Note>, user: User) {
        for (note in successfulDeletes) {
            val data = workDataOf(
                Pair("newNote", GsonHelper.serializeToJson(note)),
                Pair("user", GsonHelper.serializeToJson(user))
            )
            val backgroundConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()
            val worker = OneTimeWorkRequestBuilder<DeleteDeletedNoteWorker>().setInputData(data)
                .setConstraints(backgroundConstraints)
                .addTag("DeleteDeletedNoteWorker").build()

            WorkManager.getInstance(context)
                .beginUniqueWork(
                    "DeleteMultipleTrashNote",
                    ExistingWorkPolicy.APPEND,
                    worker
                )
                .enqueue()
        }
    }

    companion object {
        const val DELETE_NOTES_SUCCESS = "Successfully deleted notes."
        const val DELETE_NOTES_ERRORS =
            "Not all the notes you selected were deleted. There was some errors."
        const val DELETE_NOTES_YOU_MUST_SELECT = "You haven't selected any notes to delete."
        const val DELETE_NOTES_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}