package com.notesync.notes.business.interactors.noteList

import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListStateEvent
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetNumNotes(private val noteCacheDataSource: NoteCacheDataSource) {
    companion object{
        const val GET_NUM_NOTES_SUCCESS = "Successfully retrieved the number of notes from cache"
        const val GET_NUM_NOTES_FAILED   = "Failed to retrieve the number of notes from cache"
    }
    fun getNumNotes(
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.getNumNotes()
        }

        val response =
            object : CacheResponseHandler<NoteListViewState, Int>(cacheResult, stateEvent) {
                override suspend fun handleSuccess(resultObj: Int): DataState<NoteListViewState> {
                    return DataState.data(
                        response = Response(
                            GET_NUM_NOTES_SUCCESS,
                            uiComponentType = UIComponentType.None(),
                            MessageType.Success()
                        ),
                        data = NoteListViewState(numNotesInCache = resultObj),stateEvent
                    )
                }

            }.getResult()

        emit(response)
    }
}