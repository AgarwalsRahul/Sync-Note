package com.notesync.notes.business.interactors.splash

import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.ApiResponseHandler
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.DataState
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.Dispatchers.IO

class SyncDeletedNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

     suspend fun syncDeletedNotes() {
        val deletedNotes = getDeletedNotes()
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.deleteNotes(deletedNotes)
        }
        object : CacheResponseHandler<Int, Int>(cacheResult, null) {
            override suspend fun handleSuccess(resultObj: Int): DataState<Int>? {
                printLogD("SyncNotes", "num Deleted notes $resultObj")
                return DataState.data(null, null, null)
            }

        }
    }

    private suspend fun getDeletedNotes(): List<Note> {
        val apiResult = safeApiCall(IO) {
            noteNetworkDataSource.getDeletedNotes()
        }

        val response = object : ApiResponseHandler<List<Note>, List<Note>>(apiResult, null) {
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>> {
                return DataState.data(null, resultObj, null)
            }

        }.getResult()

        return response.data ?: ArrayList()
    }
}