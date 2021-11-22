package com.notesync.notes.business.interactors.trash

import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.ApiResponseHandler
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetTrashNotesFromNetwork(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    companion object {
        const val NO_TRASH_NOTES = "No trash notes."
        const val NOTES_FETCHED_SUCCESS = "All the notes are fetched successfully."
    }

    fun getNotes(stateEvent: StateEvent, user: User): Flow<DataState<TrashViewState>?> = flow {

        val apiResult = safeApiCall(IO) {
            noteNetworkDataSource.getDeletedNotes(user)
        }
        val response =
            object : ApiResponseHandler<TrashViewState, List<Note>>(apiResult, stateEvent) {
                override suspend fun handleSuccess(resultObj: List<Note>): DataState<TrashViewState> {
                    if (resultObj.isEmpty()) {
                        return DataState.data(
                            Response(
                                NO_TRASH_NOTES,
                                UIComponentType.None(),
                                MessageType.Success()
                            ), TrashViewState(), stateEvent
                        )
                    }
                    insertTrashNotesIntoCache(resultObj)
                    return DataState.data(
                        Response(
                            NOTES_FETCHED_SUCCESS,
                            UIComponentType.None(),
                            MessageType.Success()
                        ), TrashViewState(), stateEvent
                    )
                }

            }.getResult()

        emit(response)

    }

    private suspend fun insertTrashNotesIntoCache(notes:List<Note>){
        safeCacheCall(IO){
            noteCacheDataSource.insertTrashNotes(notes)
        }
    }

}