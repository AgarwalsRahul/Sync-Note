package com.notesync.notes.business.interactors.splash

import android.content.Context
import androidx.work.*
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.workers.SyncDeleteNoteWorker
import com.notesync.notes.util.printLogD
import java.util.concurrent.TimeUnit

class SyncDeletedNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val context: Context
) {


    fun syncDeletedNotes(user: User,) {

        printLogD("syncDeletedNotes","Start Syncing")
        val data = workDataOf(Pair("user", GsonHelper.serializeToJson(user)))

        val backgroundConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val worker =
            PeriodicWorkRequestBuilder<SyncDeleteNoteWorker>(1, TimeUnit.HOURS).setInputData(data)

                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .setConstraints(backgroundConstraints)
                .addTag("SyncDeletedNotes").build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("SyncDeletedNote", ExistingPeriodicWorkPolicy.KEEP, worker)

//        printLogD("syncDeleteNotes", "Event generated")
//        try
//        {
//            noteNetworkDataSource.getDeletedNoteChanges(user).collect {
//
//                val result = noteCacheDataSource.deleteNote(it.id)
//                printLogD("syncDeletedNotes", "$result")
//
//            }
//        } catch (e: Exception)
//        {
//            printLogD("syncDeletedNotes", "${e.message}")
//        }


    }


//     suspend fun syncDeletedNotes() {
//        val deletedNotes = getDeletedNotes()
//        val cacheResult = safeCacheCall(IO) {
//            noteCacheDataSource.deleteNotes(deletedNotes)
//        }
//        object : CacheResponseHandler<Int, Int>(cacheResult, null) {
//            override suspend fun handleSuccess(resultObj: Int): DataState<Int>? {
//                printLogD("SyncNotes", "num Deleted notes $resultObj")
//                return DataState.data(null, null, null)
//            }
//
//        }
//    }
//
//    private suspend fun getDeletedNotes(): List<Note> {
//        val apiResult = safeApiCall(IO) {
//            noteNetworkDataSource.getDeletedNotes("")
//        }
//
//        val response = object : ApiResponseHandler<List<Note>, List<Note>>(apiResult, null) {
//            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>> {
//                return DataState.data(null, resultObj, null)
//            }
//
//        }.getResult()
//
//        return response.data ?: ArrayList()
//    }
}