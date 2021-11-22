package com.notesync.notes.business.interactors.noteList

import android.content.Context
import androidx.work.*
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import com.notesync.notes.framework.workers.DeleteDeletedNoteWorker
import com.notesync.notes.framework.workers.InsertOrUpdateNoteWorker
import com.notesync.notes.framework.workers.InsertUpdatedOrNewNoteWorker
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FlowPreview
class RestoreDeletedNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val context: Context
) {

    fun restoreDeleteNote(
        note: Note,
        stateEvent: StateEvent,
        user: User
    ): Flow<DataState<NoteListViewState>?> =
        flow {
            val cacheResult = safeCacheCall(IO) {
                noteCacheDataSource.insertNote(note)
            }
            val response =
                object : CacheResponseHandler<NoteListViewState, Long>(cacheResult, stateEvent) {
                    override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState>? {
                        return if (resultObj > 0) {
                            val viewState =
                                NoteListViewState(
                                    notePendingDelete = NoteListViewState.NotePendingDelete(
                                        note = note
                                    )
                                )
                            safeCacheCall(IO){
                                noteCacheDataSource.deleteTrashNote(note.id)
                            }
                            DataState.data(
                                response = Response(
                                    message = RESTORE_NOTE_SUCCESS,
                                    uiComponentType = UIComponentType.Toast(),
                                    messageType = MessageType.Success()
                                ),
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        } else {
                            DataState.data(
                                response = Response(
                                    message = RESTORE_NOTE_FAILED,
                                    uiComponentType = UIComponentType.Toast(),
                                    messageType = MessageType.Error()
                                ),
                                data = null,
                                stateEvent = stateEvent
                            )
                        }
                    }

                }.getResult()


            emit(response)

            updateNetwork(response?.stateMessage?.response?.message, note, user)
        }

    private suspend fun updateNetwork(response: String?, note: Note, user: User) {
        if (response.equals(RESTORE_NOTE_SUCCESS)) {

//            // insert into "notes" node
//            safeApiCall(IO) {
//                noteNetworkDataSource.insertOrUpdateNote(note, user.id)
//            }
//
//            // remove from "deleted" node
//            safeApiCall(IO) {
//                noteNetworkDataSource.deleteDeletedNote(note, user.id)
//            }
//
//            safeApiCall(IO){
//                noteNetworkDataSource.insertUpdatedOrNewNote(note,user)
//            }
            val data = workDataOf(
                Pair("newNote", GsonHelper.serializeToJson(note)),
                Pair("user", GsonHelper.serializeToJson(user))
            )
            val backgroundConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()
            val worker = OneTimeWorkRequestBuilder<InsertOrUpdateNoteWorker>().setInputData(data)
                .setConstraints(backgroundConstraints)
                .addTag("InsertOrUpdateNoteWorker").build()
            val worker1 =
                OneTimeWorkRequestBuilder<DeleteDeletedNoteWorker>().setInputData(data)
                    .setConstraints(backgroundConstraints)
                    .addTag("DeleteDeletedNoteWorker").build()

            val worker2 =
                OneTimeWorkRequestBuilder<InsertUpdatedOrNewNoteWorker>().setInputData(
                    data
                )
                    .setConstraints(backgroundConstraints)
                    .addTag("InsertUpdatedOrNewNoteWorker").build()
            WorkManager.getInstance(context)
                .beginUniqueWork(
                    "RestoreDeletedNote",
                    ExistingWorkPolicy.APPEND,
                    listOf(worker, worker1, worker2)
                )
                .enqueue()
        }
    }

    companion object {

        val RESTORE_NOTE_SUCCESS = "Successfully restored the deleted note."
        val RESTORE_NOTE_FAILED = "Failed to restore the deleted note."

    }
}