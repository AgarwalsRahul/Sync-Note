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
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import com.notesync.notes.framework.workers.InsertDeletedNoteWorker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestoreDeletedTrashNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val context: Context
) {

    fun restore(note: Note, stateEvent: StateEvent, user: User): Flow<DataState<TrashViewState>?> =
        flow {

            val response = insertTrashNote(note, stateEvent)
            emit(response)

            updateNetwork(response?.stateMessage?.response?.message, note, user)
        }

    private suspend fun insertTrashNote(
        note: Note,
        stateEvent: StateEvent
    ): DataState<TrashViewState>? {
        val insertTrashResult = safeCacheCall(IO) {
            noteCacheDataSource.insertTrashNote(note)
        }
        val trashResponse =
            object : CacheResponseHandler<TrashViewState, Long>(insertTrashResult, stateEvent) {
                override suspend fun handleSuccess(resultObj: Long): DataState<TrashViewState>? {
                    val viewState =
                        TrashViewState(
                            notePendingDelete = TrashViewState.NotePendingDelete(
                                note = note
                            )
                        )
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
                        ), viewState, stateEvent
                    )
                }

            }.getResult()
        return trashResponse
    }

    private suspend fun updateNetwork(message: String?, note: Note, user: User) {
        if (message == INSERT_TRASH_SUCCESS) {

            val data = workDataOf(
                Pair("newNote", GsonHelper.serializeToJson(note)),
                Pair("user", GsonHelper.serializeToJson(user))
            )
            val backgroundConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()
            val worker1 =
                OneTimeWorkRequestBuilder<InsertDeletedNoteWorker>().setInputData(data)
                    .setConstraints(backgroundConstraints)
                    .addTag("InsertDeletedNoteWorker").build()


            WorkManager.getInstance(context)
                .beginUniqueWork(
                    "InsertTrashNote",
                    ExistingWorkPolicy.APPEND,
                    worker1
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
        const val INSERT_TRASH_SUCCESS = "Successfully restored a note"
        const val INSERT_TRASH_FAILURE = "Failed to restore a note"
    }
}