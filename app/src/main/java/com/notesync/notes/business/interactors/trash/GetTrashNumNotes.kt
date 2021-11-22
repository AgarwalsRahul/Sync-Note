package com.notesync.notes.business.interactors.trash

import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetTrashNumNotes(private val noteCacheDataSource: NoteCacheDataSource) {
    companion object{
        const val GET_NUM_TRASH_NOTES_SUCCESS = "Successfully retrieved the number of trash notes from cache"
        const val GET_NUM_TRASH_NOTES_FAILED   = "Failed to retrieve the number of trash notes from cache"
    }
    fun getNumTrashNotes(
        stateEvent: StateEvent
    ): Flow<DataState<TrashViewState>?> = flow {
        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.getNumTrashNotes()
        }

        val response =
            object : CacheResponseHandler<TrashViewState, Int>(cacheResult, stateEvent) {
                override suspend fun handleSuccess(resultObj: Int): DataState<TrashViewState> {
                    return DataState.data(
                        response = Response(
                            GET_NUM_TRASH_NOTES_SUCCESS,
                            uiComponentType = UIComponentType.None(),
                            MessageType.Success()
                        ),
                        data = TrashViewState(numNotesInCache = resultObj),stateEvent
                    )
                }

            }.getResult()

        emit(response)
    }
}