package com.notesync.notes.di.worker

import com.notesync.notes.framework.workers.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface WorkerBindingModule {

    @Binds
    @IntoMap
    @WorkerKey(InsertOrUpdateNoteWorker::class)
    fun bindInsertOrUpdateNoteWorker(factory: InsertOrUpdateNoteWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(DeleteNoteWorker::class)
    fun bindDeleteNoteWorker(factory: DeleteNoteWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(InsertDeletedNoteWorker::class)
    fun bindInsertDeletedNoteWorker(factory: InsertDeletedNoteWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(InsertUpdatedOrNewNoteWorker::class)
    fun bindInsertUpdatedOrNewNoteWorker(factory: InsertUpdatedOrNewNoteWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(DeleteDeletedNoteWorker::class)
    fun bindDeleteDeletedNoteWorker(factory: DeleteDeletedNoteWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(DeleteUpdatedNoteFromOtherDevicesWorker::class)
    fun bindDeleteUpdatedNoteFromOtherDevicesWorker(factory: DeleteUpdatedNoteFromOtherDevicesWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(GetUpdatedNotesWorker::class)
    fun bindGetUpdatesNotesWorker(factory: GetUpdatedNotesWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncDeleteNoteWorker::class)
    fun bindSyncDeleteNoteWorker(factory: SyncDeleteNoteWorker.Factory): ChildWorkerFactory
}