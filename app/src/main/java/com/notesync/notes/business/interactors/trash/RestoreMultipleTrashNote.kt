package com.notesync.notes.business.interactors.trash

import android.content.Context
import androidx.work.*
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import com.notesync.notes.framework.workers.DeleteDeletedNoteWorker
import com.notesync.notes.framework.workers.InsertOrUpdateNoteWorker
import com.notesync.notes.framework.workers.InsertUpdatedOrNewNoteWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class RestoreMultipleTrashNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val context: Context,
) {

    companion object {
        const val RESTORE_NOTE_SUCCESS = "Successfully restored notes."
        const val RESTORE_NOTES_ERRORS =
            "Not all the notes you selected were restored. There was some errors."
        const val RESTORE_NOTES_YOU_MUST_SELECT = "You haven't selected any notes to restore."
        const val RESTORE_NOTES_ARE_YOU_SURE = "Are you sure you want to restore these?"
    }

    // set true if an error occurs when deleting any of the notes from cache
    private var onRestoreError: Boolean = false

    /**
     * Logic:
     * 1. execute all the restores and save result into an ArrayList<DataState<NoteListViewState>>
     * 2a. If one of the results is a failure, emit an "error" response
     * 2b. If all success, emit success response
     * 3. Update network with notes that were successfully deleted
     */

    fun restoreMultipleTrashNotes(
        notes: List<Note>,
        stateEvent: StateEvent,
        user: User
    ): Flow<DataState<TrashViewState>?> = flow {
        val successfulRestores: ArrayList<Note> =
            ArrayList() // notes that were successfully deleted
        for (note in notes) {
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                noteCacheDataSource.deleteTrashNote(note.id)
            }

            val response = object : CacheResponseHandler<TrashViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: Int): DataState<TrashViewState>? {
                    if (resultObj < 0) { // if error
                        onRestoreError = true
                    } else {
                        safeCacheCall(Dispatchers.IO) {
                            noteCacheDataSource.insertNote(note)
                        }
                        successfulRestores.add(note)
                    }
                    return null
                }
            }.getResult()

            // check for random errors
            if (response?.stateMessage?.response?.message
                    ?.contains(stateEvent.errorInfo()) == true
            ) {
                onRestoreError = true
            }

        }

        if (onRestoreError) {
            emit(
                DataState.data<TrashViewState>(
                    response = Response(
                        message = RESTORE_NOTES_ERRORS,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        } else {
            emit(
                DataState.data(
                    response = Response(
                        message = RESTORE_NOTE_SUCCESS,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }

        updateNetwork(successfulRestores, user)
    }


    private suspend fun updateNetwork(successfulRestores: ArrayList<Note>, user: User) {
        for (note in successfulRestores) {
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
            val worker1 =
                OneTimeWorkRequestBuilder<InsertOrUpdateNoteWorker>().setInputData(data)
                    .setConstraints(backgroundConstraints)
                    .addTag("InsertOrUpdateNoteWorker").build()

            val worker2 =
                OneTimeWorkRequestBuilder<InsertUpdatedOrNewNoteWorker>().setInputData(
                    data
                )
                    .setConstraints(backgroundConstraints)
                    .addTag("InsertUpdatedOrNewNoteWorker").build()


            WorkManager.getInstance(context)
                .beginUniqueWork(
                    "RestoreMultipleNotes",
                    ExistingWorkPolicy.APPEND,
                    listOf(worker, worker1, worker2)
                )
                .enqueue()
        }
    }
}