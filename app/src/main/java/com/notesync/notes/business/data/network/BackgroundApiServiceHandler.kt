package com.notesync.notes.business.data.network

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.CoroutineDispatcher

abstract class BackgroundApiServiceHandler(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(
        context, workerParams
    ) {


    override suspend fun doWork(): Result {
        return when(performNetworkCall()){
            is ApiResult.Success->{
                 Result.success()
            }
            else->{
                Result.retry()
            }
        }
    }

    companion object{
        val backgroundConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()
    }


    abstract suspend fun performNetworkCall():ApiResult<*>
}