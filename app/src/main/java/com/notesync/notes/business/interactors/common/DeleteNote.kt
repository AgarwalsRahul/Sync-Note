package com.notesync.notes.business.interactors.common

import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    fun deleteNote(note: Note, stateEvent: StateEvent): Flow<DataState<ViewState>?> = flow {

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
                            ),null,stateEvent
                        )
                    }
                     return DataState.data(
                        response = Response(
                            DELETE_SUCCESS,
                            UIComponentType.None(),
                            MessageType.Success()
                        ),null,stateEvent
                    )

                }

            }.getResult()
        emit(response)

        updateNetwork(response?.stateMessage?.response?.message,note)
    }

    private suspend  fun updateNetwork(message:String?,note:Note){
        if(message== DELETE_SUCCESS){
            safeApiCall(IO){
                noteNetworkDataSource.deleteNote(note.id)
            }

            safeApiCall(IO){
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object {
        const val DELETE_FAILURE = "Failed to delete a note"
        const val DELETE_SUCCESS = "Successfully delete a note"
    }
}