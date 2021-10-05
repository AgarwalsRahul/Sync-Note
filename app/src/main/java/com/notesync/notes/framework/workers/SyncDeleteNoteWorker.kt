package com.notesync.notes.framework.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ListenerRegistration
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.util.Constants
import com.notesync.notes.util.cLog
import com.notesync.notes.util.printLogD
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty

class SyncDeleteNoteWorker @AssistedInject constructor(
    @Assisted private val appContext: Context, @Assisted private val params: WorkerParameters,

    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteCacheDataSource: NoteCacheDataSource
) : CoroutineWorker(appContext, params) {

    var jobToCancel: Job? = null
    override suspend fun doWork(): Result {


        if (runAttemptCount > Constants.MAX_RETRY_LIMIT) {
            return Result.failure(inputData)
        }

        val data = inputData.keyValueMap

        val user = GsonHelper.deserializeToUser(data["user"] as String)

        try {
            if (!isStopped)
                jobToCancel = CoroutineScope(IO).launch {
                    noteNetworkDataSource.getDeletedNoteChanges(user).onEmpty {
                        printLogD("SyncDeleteNoteWorker", "onEmpty")
                        return@onEmpty
                    }
                        .onCompletion {
                            printLogD("SyncDeleteNoteWorker", "onComplete")
                            return@onCompletion
                        }
                        .collect {
                            withContext(IO) {
                                if (!isStopped) {
                                    val result = noteCacheDataSource.deleteNote(it.id)
                                    printLogD("syncDeletedNotes", "$result")
                                }

                            }

                        }
                }
            return Result.success()
        } catch (e: Exception) {
            printLogD("syncDeletedNotes", "${e.message}")
            printLogD("SyncDeleteNoteWorker", "Retrying Request")
            cLog(e.message)
            return Result.retry()
        }

    }


    @AssistedFactory
    interface Factory : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): SyncDeleteNoteWorker
    }
}