package com.notesync.notes.framework.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notesync.notes.business.data.network.ApiResult
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.domain.state.SessionManager
import com.notesync.notes.util.Constants.MAX_RETRY_LIMIT
import com.notesync.notes.util.printLogD
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class InsertOrUpdateNoteWorker
@AssistedInject constructor(
    @Assisted private val appContext: Context, @Assisted private val params: WorkerParameters,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val sessionManager: SessionManager
) :
    CoroutineWorker(appContext, params) {


    override suspend fun doWork(): Result {
        if (runAttemptCount > MAX_RETRY_LIMIT) {
            Result.failure(inputData)
        }
        val data = inputData.keyValueMap
        val newNote = GsonHelper.deserializeToNote(data["newNote"] as String)
        val user = GsonHelper.deserializeToUser(data["user"] as String)
        val result = safeApiCall(IO) {
            noteNetworkDataSource.insertOrUpdateNote(
                newNote,
                user
            )
        }
        return when (result) {
            is ApiResult.Success -> {
                Result.success()
            }
            else -> {
                printLogD("InsertOrUpdateNewNoteWorker", "Retrying request...")
                Result.retry()
            }
        }
    }

    @AssistedFactory
    interface Factory : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): InsertOrUpdateNoteWorker
    }
}