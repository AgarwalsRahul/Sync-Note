package com.notesync.notes.business.interactors.noteList

import android.util.Log
import com.notesync.notes.business.data.cache.CacheConstants
import com.notesync.notes.business.data.cache.CacheErrors
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.CacheResult
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.ApiResponseHandler
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.lang.Exception
import java.util.concurrent.TimeoutException

class GetAllNotesFromNetwork(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    companion object {
        const val NO_NOTES_CREATED_YET = "No notes has been created yet."
        const val NOTES_FETCHED_SUCCESS = "All the notes are fetched successfully."
    }

    fun getNotes(stateEvent: StateEvent, user: User): Flow<DataState<NoteListViewState>?> = flow {
        Log.d("GetAllNotesFromNetwork","Started")
        val apiResult = safeApiCall(IO) {
            noteNetworkDataSource.getAllNotes(user)
        }
        Log.d("GetAllNotesFromNetwork","${apiResult}")
        val response =
            object : ApiResponseHandler<NoteListViewState, List<Note>>(apiResult, stateEvent) {
                override suspend fun handleSuccess(resultObj: List<Note>): DataState<NoteListViewState> {
                    Log.d("GetAllNotesFromNetwork","${resultObj.size}")
                    if (resultObj.isEmpty()) {
                        return DataState.data(
                            Response(
                                NO_NOTES_CREATED_YET,
                                UIComponentType.None(),
                                MessageType.Success()
                            ), NoteListViewState(), stateEvent
                        )
                    }
                    insertNotesIntoCache(resultObj)
                    return DataState.data(
                        Response(
                            NOTES_FETCHED_SUCCESS,
                            UIComponentType.None(),
                            MessageType.Success()
                        ), NoteListViewState(), stateEvent
                    )
                }

            }.getResult()

        emit(response)

    }

    private suspend fun insertNotesIntoCache(notes:List<Note>){
        safeCacheCall(IO){
            noteCacheDataSource.insertNotes(notes)
        }
    }

//        return try {
//            noteCacheDataSource.getAllNotes().map {
//                return@map DataState.data(
//                    Response(
//                        "SUCCESS",
//                        UIComponentType.None(),
//                        MessageType.Success()
//                    ),
//                    NoteListViewState(noteList = ArrayList(it)),
//                    stateEvent
//                )
//            }
//        } catch (e: Throwable) {
//            flow<DataState<NoteListViewState>> {
//                when (e) {
//                    is TimeoutException -> {
//                        emit(
//                            DataState.error<NoteListViewState>(
//                                Response(
//                                    CacheErrors.CACHE_ERROR_TIMEOUT,
//                                    UIComponentType.SnackBar(),
//                                    MessageType.Error()
//                                ), stateEvent
//                            ) as DataState<NoteListViewState>
//                        )
//                    }
//                    else -> {
//                        emit(
//                            DataState.error<NoteListViewState>(
//                                Response(
//                                    CacheErrors.CACHE_ERROR,
//                                    UIComponentType.SnackBar(),
//                                    MessageType.Error()
//                                ), stateEvent
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    }
}



