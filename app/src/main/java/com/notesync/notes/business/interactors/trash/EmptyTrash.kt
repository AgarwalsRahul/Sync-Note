package com.notesync.notes.business.interactors.trash

import android.content.Context
import androidx.work.*
import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import com.notesync.notes.framework.workers.EmptyTrashWorker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EmptyTrash(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val context: Context
) {

    companion object {
        const val EMPTY_TRASH_FAILED = "Trash cannot be emptied. Some error has occurred."
        const val EMPTY_TRASH_SUCCESS = "Successfully deletes all trash notes"
        const val EMPTY_TRASH_ARE_YOU_SURE = "Are you sure you want to empty trash?"
        const val EMPTY_TRASH_NO_NOTES = "There are no trash notes."
    }

    fun emptyTrash(
        notes: List<Note>,
        stateEvent: StateEvent,
        user: User
    ): Flow<DataState<TrashViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.emptyTrash()
        }

        val response = object : CacheResponseHandler<TrashViewState, Int>(cacheResult, stateEvent) {
            override suspend fun handleSuccess(resultObj: Int): DataState<TrashViewState> {
                if (resultObj < 0) {
                    return DataState.data(
                        response = Response(
                            EMPTY_TRASH_FAILED,
                            UIComponentType.SnackBar(),
                            MessageType.Error()
                        ), null, stateEvent
                    )
                }
                return DataState.data(
                    response = Response(
                        EMPTY_TRASH_SUCCESS,
                        UIComponentType.Toast(),
                        MessageType.Success()
                    ), null, stateEvent
                )
            }

        }.getResult()
        emit(response)
        updateNetwork(response?.stateMessage?.response?.message, notes, user)
    }

    private suspend fun updateNetwork(message: String?, notes: List<Note>, user: User) {
        if (message == EMPTY_TRASH_SUCCESS) {

            val data = workDataOf(
                Pair("user", GsonHelper.serializeToJson(user)),
                Pair("notes", GsonHelper.serializeToNotes(notes))
            )
            val backgroundConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()
            val worker = OneTimeWorkRequestBuilder<EmptyTrashWorker>().setInputData(data)
                .setConstraints(backgroundConstraints)
                .addTag("EmptyTrashWorker").build()


            WorkManager.getInstance(context)
                .beginUniqueWork(
                    "EmptyTrash",
                    ExistingWorkPolicy.REPLACE,
                    worker
                )
                .enqueue()
        }
    }
}