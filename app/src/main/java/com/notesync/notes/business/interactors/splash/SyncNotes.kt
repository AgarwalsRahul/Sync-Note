package com.notesync.notes.business.interactors.splash

import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.ApiResponseHandler
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.DataState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

//
class SyncNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
) {
    suspend fun syncNotes(user: User) {
        val cachedNotesList = getCachedNotes()
        val networkNotesList = getNetworkNotes(user)
        syncNetworkNotesWithCachedNotes(ArrayList(cachedNotesList), networkNotesList)
    }

    //
//
    private suspend fun getNetworkNotes(user: User): List<Note> {
        val networkResult = safeApiCall(IO) {
            noteNetworkDataSource.getAllNotes(user)
        }
        val response =
            object : ApiResponseHandler<List<Note>, List<Note>>(networkResult, null) {
                override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>> {
                    return DataState.data(null, resultObj, null)
                }

            }.getResult()

        return response.data ?: ArrayList<Note>()
    }

    //
    private suspend fun getCachedNotes(): List<Note> {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.getAllNotes()
        }

        val response =
            object : CacheResponseHandler<List<Note>, Flow<List<Note>>>(cacheResult, null) {
                override suspend fun handleSuccess(resultObj: Flow<List<Note>>): DataState<List<Note>>? {
                    return DataState.data(null, resultObj.first(), null)
                }

            }.getResult()

        return response?.data ?: emptyList()
    }

    ////
//    // get All the notes from network
//    // if they do not exist in cache, insert them
//    // if they do exist in cache, make sure they are up-to-Date
//    // while looping remove notes from cachedNotesList
//    // If any remains it should be inserted in network
    private suspend fun syncNetworkNotesWithCachedNotes(
        cachedNotes: ArrayList<Note>,
        networkNotes: List<Note>
    ) =
        withContext(IO) {


            for (note in networkNotes) {
                noteCacheDataSource.searchNoteById(note.id)?.let { cachedNote ->
                    cachedNotes.remove(cachedNote)
                } ?: noteCacheDataSource.insertNote(note)
            }


            // insert remaining into the network

            safeCacheCall(IO) { noteCacheDataSource.deleteNotes(cachedNotes) }

        }

//    private suspend fun checkIfCachedNoteRequiresUpdate(cachedNote: Note, networkNote: Note) {
//        val cacheUpdatedAt = cachedNote.updated_at
//        val networkUpdatedAt = networkNote.updated_at
//
//        // update cache (network has newest data)
//        if (networkUpdatedAt > cacheUpdatedAt) {
//            printLogD(
//                "SyncNotes",
//                "cacheUpdatedAt: ${cacheUpdatedAt}, " +
//                        "networkUpdatedAt: ${networkUpdatedAt}, " +
//                        "note: ${cachedNote.title}"
//            )
//            safeCacheCall(IO) {
//                noteCacheDataSource.updateNote(
//                    networkNote.id,
//                    networkNote.title,
//                    networkNote.body,
//                    networkNote.updated_at
//                )
//            }
//        }
//        // update network (cache has newest data)
//        else if (networkUpdatedAt < cacheUpdatedAt) {
//            safeApiCall(IO) {
//                noteNetworkDataSource.insertOrUpdateNote(cachedNote, "")
//            }
//        }
//    }
}

