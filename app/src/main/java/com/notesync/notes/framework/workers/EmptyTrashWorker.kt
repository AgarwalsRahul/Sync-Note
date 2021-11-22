package com.notesync.notes.framework.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notesync.notes.business.data.network.ApiResult
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers

class EmptyTrashWorker @AssistedInject constructor(
    @Assisted private val appContext: Context, @Assisted private val params: WorkerParameters,
    private val noteNetworkDataSource: NoteNetworkDataSource,
) :
    CoroutineWorker(appContext, params) {


    override suspend fun doWork(): Result {

        if (runAttemptCount > Constants.MAX_RETRY_LIMIT) {
            Result.failure(inputData)
        }
        val data = inputData.keyValueMap
        val user = GsonHelper.deserializeToUser(data["user"] as String)
        val notes = GsonHelper.deserializeToNotes(data["notes"] as String)
        val result = safeApiCall(Dispatchers.IO) {
            noteNetworkDataSource.deleteAllTrashNotes(
                notes,
                user
            )
        }
        return when (result) {
            is ApiResult.Success -> {
                Result.success()
            }
            else -> {
                Result.retry()
            }
        }
    }

    @AssistedFactory
    interface Factory : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): EmptyTrashWorker
    }
}