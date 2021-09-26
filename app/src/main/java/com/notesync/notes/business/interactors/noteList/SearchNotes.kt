package com.notesync.notes.business.interactors.noteList

import android.webkit.DateSorter
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class SearchNotes(private val noteCacheDataSource: NoteCacheDataSource) {

    companion object {
        const val SEARCH_NOTES_SUCCESS = "Successfully retrieved all the notes"
        const val SEARCH_NOTES_NO_MATCHING_RESULT = "There are no notes that match the query"
        const val SEARCH_NOTES_FAILED = "Failed to retrieve the list of notes"
    }

    fun searchNotes(
        query: String, filterAndOrder: String, page: Int, stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        var updatedPage = page
        if (page <= 0) {
            updatedPage = 1
        }

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.searchNotes(query, filterAndOrder, updatedPage)
        }

        val response = object : CacheResponseHandler<NoteListViewState, List<Note>>(
            cacheResult, stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<NoteListViewState> {
                var message: String? = SEARCH_NOTES_SUCCESS
                var uiComponentType: UIComponentType = UIComponentType.None()
                if (resultObj.isEmpty()) {
                    message = SEARCH_NOTES_NO_MATCHING_RESULT
                    uiComponentType = UIComponentType.Toast()
                }
                return DataState.data(
                    response = Response(
                        message,
                        uiComponentType,
                        MessageType.Success()
                    ),
                    data = NoteListViewState(noteList = ArrayList(resultObj)), stateEvent
                )
            }
        }.getResult()

        emit(response)

    }
}