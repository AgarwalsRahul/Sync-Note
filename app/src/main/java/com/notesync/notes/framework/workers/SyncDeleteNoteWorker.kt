package com.notesync.notes.framework.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.util.cLog
import com.notesync.notes.util.printLogD
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

class SyncDeleteNoteWorker @AssistedInject constructor(
    @Assisted private val appContext: Context, @Assisted private val params: WorkerParameters,

    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteCacheDataSource: NoteCacheDataSource
) : CoroutineWorker(appContext, params) {


    override suspend fun doWork(): Result {


//        if (runAttemptCount > Constants.MAX_RETRY_LIMIT) {
//            return Result.failure(inputData)
//        }

        val data = inputData.keyValueMap

        val user = GsonHelper.deserializeToUser(data["user"] as String)

        try {

                withContext(IO) {
                    noteNetworkDataSource.getDeletedNoteChanges(user)
                        .collect {
                            withContext(IO) {

                                if (it.second) {
                                    safeCacheCall(IO) {
                                        noteCacheDataSource.deleteTrashNote(it.first.id)
                                    }
                                } else {
                                    val result = safeCacheCall(IO){
                                        noteCacheDataSource.deleteNote(it.first.id)
                                    }
                                    safeCacheCall(IO) {
                                        noteCacheDataSource.insertTrashNote(it.first)
                                    }
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