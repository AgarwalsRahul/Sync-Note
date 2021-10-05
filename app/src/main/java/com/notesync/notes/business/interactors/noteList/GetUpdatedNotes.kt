package com.notesync.notes.business.interactors.noteList


import android.content.Context
import androidx.work.*
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.util.GsonHelper
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.StateEvent
import com.notesync.notes.framework.workers.GetUpdatedNotesWorker
import java.util.concurrent.TimeUnit


class GetUpdatedNotes constructor(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val context: Context
) {

    suspend fun getUpdatedNotes(user: User, stateEvent: StateEvent?) {
        val data = workDataOf(Pair("user", GsonHelper.serializeToJson(user)))

        val backgroundConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val worker =
            PeriodicWorkRequestBuilder<GetUpdatedNotesWorker>(1, TimeUnit.HOURS).setInputData(data)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .setConstraints(backgroundConstraints).addTag("GetUpdatedNotes").build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("GetUpdatedNote", ExistingPeriodicWorkPolicy.REPLACE, worker)
    }

}