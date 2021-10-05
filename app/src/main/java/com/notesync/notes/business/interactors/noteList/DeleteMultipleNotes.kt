package com.notesync.notes.business.interactors.noteList

import android.content.Context
import androidx.work.*
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.DataState
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.domain.state.StateEvent
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import com.notesync.notes.framework.workers.DeleteNoteWorker
import com.notesync.notes.framework.workers.DeleteUpdatedNoteFromOtherDevicesWorker
import com.notesync.notes.framework.workers.InsertDeletedNoteWorker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMultipleNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
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
    ): Flow<DataState<NoteListViewState>?> = flow {

        val successfulDeletes: ArrayList<Note> = ArrayList() // notes that were successfully deleted
        for (note in notes) {
            val cacheResult = safeCacheCall(IO) {
                noteCacheDataSource.deleteNote(note.id)
            }

            val response = object : CacheResponseHandler<NoteListViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: Int): DataState<NoteListViewState>? {
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
                DataState.data<NoteListViewState>(
                    response = Response(
                        message = DELETE_NOTES_ERRORS,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        } else {
            emit(
                DataState.data<NoteListViewState>(
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

//            // delete from "notes" node
//            safeApiCall(IO) {
//                noteNetworkDataSource.deleteNote(note.id, user.id)
//            }
//
//            // insert into "deletes" node
//            safeApiCall(IO) {
//                noteNetworkDataSource.insertDeletedNote(note, user.id)
//            }
//            safeApiCall(IO) {
//                noteNetworkDataSource.deleteUpdatedNoteFromOtherDevices(user, note)
//            }
            val data = workDataOf(
                Pair("newNote", GsonHelper.serializeToJson(note)),
                Pair("user", GsonHelper.serializeToJson(user))
            )
            val backgroundConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()
            val worker = OneTimeWorkRequestBuilder<DeleteNoteWorker>().setInputData(data)
                .setConstraints(backgroundConstraints)
                .addTag("DeleteNoteWorker").build()
            val worker1 =
                OneTimeWorkRequestBuilder<InsertDeletedNoteWorker>().setInputData(data)
                    .setConstraints(backgroundConstraints)
                    .addTag("InsertDeletedNoteWorker").build()

            val worker2 =
                OneTimeWorkRequestBuilder<DeleteUpdatedNoteFromOtherDevicesWorker>().setInputData(
                    data
                )
                    .setConstraints(backgroundConstraints)
                    .addTag("DeleteUpdatedNoteFromOtherDevicesWorker").build()


            WorkManager.getInstance(context)
                .beginUniqueWork(
                    "DeleteMultipleNote",
                    ExistingWorkPolicy.APPEND,
                    listOf(worker, worker1, worker2)
                )
                .enqueue()
        }
    }

    companion object {
        val DELETE_NOTES_SUCCESS = "Successfully deleted notes."
        val DELETE_NOTES_ERRORS =
            "Not all the notes you selected were deleted. There was some errors."
        val DELETE_NOTES_YOU_MUST_SELECT = "You haven't selected any notes to delete."
        val DELETE_NOTES_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}