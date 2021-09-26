package com.notesync.notes.business.interactors.noteList

import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InsertNewNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteFactory: NoteFactory,
) {

    companion object {
        const val INSERT_NOTE_FAILED = "Note is not created."
        const val INSERT_NOTE_SUCCESS = "Note is successfully created"
    }

    fun insertNewNote(
        id: String? = null,
        title: String,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val newNote = noteFactory.createSingleNote(id, title, null)

        val cacheResult = safeCacheCall(IO){
            noteCacheDataSource.insertNote(newNote)
        }

        val cacheResponse = object: CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState> {
                return if(resultObj > 0){
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
                }
                else{
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

        updateNetwork(cacheResponse?.stateMessage?.response?.message,newNote)
    }

    private suspend fun updateNetwork(message: String?, newNote: Note) {
        if(message== INSERT_NOTE_SUCCESS){
          safeApiCall(IO){
              noteNetworkDataSource.insertOrUpdateNote(newNote)
          }
        }
    }


}