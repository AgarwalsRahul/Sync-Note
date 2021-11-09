package com.notesync.notes.framework.workers


import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.util.Constants.MAX_RETRY_LIMIT
import com.notesync.notes.util.cLog
import com.notesync.notes.util.printLogD
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

class GetUpdatedNotesWorker @AssistedInject constructor(
    @Assisted private val appContext: Context, @Assisted private val params: WorkerParameters,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteCacheDataSource: NoteCacheDataSource
) :
    CoroutineWorker(appContext, params) {




    override suspend fun doWork(): Result {

//        if (runAttemptCount > MAX_RETRY_LIMIT) {
//            return Result.failure(inputData)
//        }
        val data = inputData.keyValueMap
        val user = GsonHelper.deserializeToUser(data["user"] as String)
        try {
            withContext(IO) {
                noteNetworkDataSource.getRealtimeUpdatedNotes(user)
                    .collect {
                        printLogD("getUpdatedNotes", "getting udpated notes...")
                        it?.let {
                            if (it.second) {
                                val result = noteCacheDataSource.updateNote(
                                    it.first.id,
                                    it.first.title,
                                    it.first.body,
                                    it.first.updated_at
                                )
                                if (result > 0) {

                                    noteNetworkDataSource.deleteUpdatedNote(user, it.first)

                                }
                            } else {
                                val oldNote = noteCacheDataSource.searchNoteById(it.first.id)
                                    var result: Int? = null
                                    result = if (oldNote != null && oldNote.id == it.first.id) {
                                    noteCacheDataSource.updateNote(
                                        it.first.id,
                                        it.first.title,
                                        it.first.body,
                                        it.first.updated_at,
                                    )
                                } else {
                                    noteCacheDataSource.insertNote(it.first).toInt()
                                }
                                if (result > 0) {

                                    noteNetworkDataSource.deleteUpdatedNote(user, it.first)

                                }
                            }
                        } ?: return@collect


                    }
            }
            return Result.success()
        } catch (e: Exception) {
            printLogD("GetUpdatedNotes", "${e.message}")
            printLogD("GetUpdatedNotesWorker", "Retrying Request")
            cLog(e.message)
            return Result.retry()
        }
    }


    @AssistedFactory
    interface Factory : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): GetUpdatedNotesWorker
    }


}


//