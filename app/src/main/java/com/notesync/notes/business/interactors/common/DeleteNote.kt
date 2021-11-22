package com.notesync.notes.business.interactors.common

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
import com.notesync.notes.framework.workers.DeleteNoteWorker
import com.notesync.notes.framework.workers.DeleteUpdatedNoteFromOtherDevicesWorker
import com.notesync.notes.framework.workers.InsertDeletedNoteWorker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val context: Context
) {

    fun deleteNote(note: Note, stateEvent: StateEvent, user: User): Flow<DataState<ViewState>?> =
        flow {

            val cacheResult = safeCacheCall(IO) {
                noteCacheDataSource.deleteNote(note.id)
            }

            val response =
                object : CacheResponseHandler<ViewState, Int>(cacheResult, stateEvent) {
                    override suspend fun handleSuccess(resultObj: Int): DataState<ViewState> {
                        if (resultObj < 0) {
                            return DataState.data(
                                response = Response(
                                    DELETE_FAILURE,
                                    UIComponentType.Toast(),
                                    MessageType.Error()
                                ), null, stateEvent
                            )
                        }

                        insertTrashNote(note, stateEvent)

                        return DataState.data(
                            response = Response(
                                DELETE_SUCCESS,
                                UIComponentType.None(),
                                MessageType.Success()
                            ), null, stateEvent
                        )

                    }

                }.getResult()
            emit(response)

            updateNetwork(response?.stateMessage?.response?.message, note, user)
        }

    private suspend fun insertTrashNote(note: Note, stateEvent: StateEvent): DataState<ViewState>? {
        val insertTrashResult = safeCacheCall(IO) {
            noteCacheDataSource.insertTrashNote(note)
        }
        val trashResponse =
            object : CacheResponseHandler<ViewState, Long>(insertTrashResult, stateEvent) {
                override suspend fun handleSuccess(resultObj: Long): DataState<ViewState>? {
                    if (resultObj < 0) {
                        return DataState.data(
                            response = Response(
                                INSERT_TRASH_FAILURE,
                                UIComponentType.None(),
                                MessageType.Success()
                            ), null, stateEvent
                        )
                    }
                    return DataState.data(
                        response = Response(
                            INSERT_TRASH_SUCCESS,
                            UIComponentType.None(),
                            MessageType.Success()
                        ), null, stateEvent
                    )
                }

            }.getResult()
        return trashResponse
    }

    private suspend fun updateNetwork(message: String?, note: Note, user: User) {
        if (message == DELETE_SUCCESS) {

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
                    "DeleteNote",
                    ExistingWorkPolicy.APPEND,
                    listOf(worker, worker1, worker2)
                )
                .enqueue()
//            safeApiCall(IO) {
//                noteNetworkDataSource.deleteNote(note.id, user.id)
//            }
//
//            safeApiCall(IO) {
//                noteNetworkDataSource.insertDeletedNote(note, user.id)
//            }
//            safeApiCall(IO) {
//                noteNetworkDataSource.deleteUpdatedNoteFromOtherDevices(user, note)
//            }
        }
    }

    companion object {
        const val DELETE_FAILURE = "Failed to delete a note"
        const val DELETE_SUCCESS = "Successfully delete a note"
        const val INSERT_TRASH_SUCCESS = "Successfully insert a note to trash"
        const val INSERT_TRASH_FAILURE = "Failed to insert a note to trash"
    }
}