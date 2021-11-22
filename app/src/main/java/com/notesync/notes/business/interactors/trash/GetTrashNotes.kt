package com.notesync.notes.business.interactors.trash

import com.notesync.notes.business.data.cache.CacheErrors
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.interactors.noteList.SearchNotes
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeoutException

class GetTrashNotes (private val noteCacheDataSource: NoteCacheDataSource) {

    companion object{
        const val NO_NOTES_IN_TRASH = "No notes in trash"
        const val GET_TRASH_NOTES_SUCCESS = "Successfully retrieved all trash notes"
        const val GET_TRASH_NOTES_FAILED = "Failed to retrieve trash notes"
    }

    fun getTrashNotes(page:Int,stateEvent:StateEvent):Flow<DataState<TrashViewState>?> = flow{

        var updatedPage = page
        if (page <= 0) {
            updatedPage = 1
        }

        try {
            noteCacheDataSource.getTrashNotes(updatedPage)
                .collect {

                    if (it.isEmpty()) {
                        emit(
                            DataState.data(
                                Response(
                                    NO_NOTES_IN_TRASH,
                                    UIComponentType.None(),
                                    MessageType.Success()
                                ), TrashViewState(noteList = ArrayList(it)), stateEvent
                            )
                        )
                    } else {
                        emit(
                            DataState.data(
                                Response(
                                    GET_TRASH_NOTES_SUCCESS,
                                    UIComponentType.None(),
                                    MessageType.Success()
                                ), TrashViewState(noteList = ArrayList(it)), stateEvent
                            )
                        )
                    }


                }
        } catch (e: Exception) {
            when (e) {
                is TimeoutCancellationException -> {
                    emit(
                        DataState.error(
                            Response(
                                CacheErrors.CACHE_ERROR_TIMEOUT,
                                UIComponentType.SnackBar(),
                                MessageType.Error()
                            ), stateEvent
                        )
                    )
                }
                is TimeoutException -> {
                    emit(
                        DataState.error(
                            Response(
                                CacheErrors.CACHE_ERROR_TIMEOUT,
                                UIComponentType.SnackBar(),
                                MessageType.Error()
                            ), stateEvent
                        )
                    )
                }
                else -> {
                    emit(
                        DataState.error(
                            Response(
                                GET_TRASH_NOTES_FAILED,
                                UIComponentType.SnackBar(),
                                MessageType.Error()
                            ), stateEvent
                        )
                    )
                }
            }
        }
    }
}