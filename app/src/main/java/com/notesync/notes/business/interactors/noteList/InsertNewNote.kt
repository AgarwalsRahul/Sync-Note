package com.notesync.notes.business.interactors.noteList

import android.content.Context
import androidx.work.*
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import com.notesync.notes.framework.workers.InsertOrUpdateNoteWorker
import com.notesync.notes.framework.workers.InsertUpdatedOrNewNoteWorker
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class InsertNewNote  constructor(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteFactory: NoteFactory,
    private val context: Context
) {

    companion object {
        const val INSERT_NOTE_FAILED = "Note is not created."
        const val INSERT_NOTE_SUCCESS = "Note is successfully created"
    }

    fun insertNewNote(
        id: String? = null,
        title: String,
        stateEvent: StateEvent, user: User
    ): Flow<DataState<NoteListViewState>?> = flow {

        printLogD("InsertNewNote","Inserting....")
        val newNote = noteFactory.createSingleNote(id, title, null)

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.insertNote(newNote)
        }
        printLogD("InsertNewNote","Inserting....")
        val cacheResponse = object : CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState> {
                return if (resultObj > 0) {
                    val viewState =
                        NoteListViewState(
                            newNote = newNote
                        )
                    DataState.data(
                        response = Response(
                            message = INSERT_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = INSERT_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(cacheResponse)

        updateNetwork(cacheResponse?.stateMessage?.response?.message, newNote, user)
    }

    private  fun updateNetwork(message: String?, newNote: Note, user: User) {
        if (message == INSERT_NOTE_SUCCESS) {


            val data = workDataOf(
                Pair("newNote", GsonHelper.serializeToJson(newNote)),
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
                OneTimeWorkRequestBuilder<InsertUpdatedOrNewNoteWorker>().setInputData(data)
                    .setConstraints(backgroundConstraints)
                    .addTag("InsertUpdatedOrNewNote").build()

            WorkManager.getInstance(context)
                .beginUniqueWork(
                    "InsertNewNote",
                    ExistingWorkPolicy.APPEND,
                    listOf(worker, worker1)
                ).enqueue()


        }
    }


}