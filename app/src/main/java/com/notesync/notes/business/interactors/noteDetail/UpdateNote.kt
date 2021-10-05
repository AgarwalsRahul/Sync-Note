package com.notesync.notes.business.interactors.noteDetail

import android.content.Context
import android.icu.text.IDNA
import androidx.work.*
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.framework.presentation.notedetail.state.NoteDetailViewState
import com.notesync.notes.framework.workers.InsertOrUpdateNoteWorker
import com.notesync.notes.framework.workers.InsertUpdatedOrNewNoteWorker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val context: Context

) {

    companion object {
        val UPDATE_NOTE_SUCCESS = "Successfully updated note."
        val UPDATE_NOTE_FAILED = "Failed to update note."
        val UPDATE_NOTE_FAILED_PK = "Update failed. Note is missing primary key."

    }

    fun updateNote(
        note: Note,
        stateEvent: StateEvent,
        user: User
    ): Flow<DataState<NoteDetailViewState>?> =
        flow {

            val cacheResult = safeCacheCall(IO) {
                noteCacheDataSource.updateNote(
                    note.id,
                    note.title,
                    note.body,
                    null
                )
            }

            val response =
                object : CacheResponseHandler<NoteDetailViewState, Int>(cacheResult, stateEvent) {
                    override suspend fun handleSuccess(resultObj: Int): DataState<NoteDetailViewState>? {
                        return if (resultObj > 0) {
                            DataState.data(
                                response = Response(
                                    UPDATE_NOTE_SUCCESS,
                                    UIComponentType.Toast(),
                                    MessageType.Success()
                                ), null, stateEvent
                            )
                        } else {
                            DataState.data(
                                response = Response(
                                    UPDATE_NOTE_FAILED,
                                    UIComponentType.SnackBar(),
                                    MessageType.Error()
                                ), null, stateEvent
                            )
                        }
                    }

                }.getResult()
            emit(response)
            updateNetwork(response?.stateMessage?.response?.message, note, user)
        }

    private suspend fun updateNetwork(message: String?, note: Note, user: User) {
        if (message == UPDATE_NOTE_SUCCESS) {
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
                .addTag("InsertNewNote").build()
            val worker1 =
                OneTimeWorkRequestBuilder<InsertUpdatedOrNewNoteWorker>().setInputData(data)
                    .setConstraints(backgroundConstraints)
                    .addTag("InsertUpdatedOrNewNote").build()

            WorkManager.getInstance(context)
                .beginUniqueWork("UpdateNote", ExistingWorkPolicy.APPEND, listOf(worker, worker1))
                .enqueue()
        }
    }

}