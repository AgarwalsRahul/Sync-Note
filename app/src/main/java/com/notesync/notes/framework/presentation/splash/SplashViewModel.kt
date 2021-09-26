package com.notesync.notes.framework.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@FlowPreview


@Singleton
class SplashViewModel
constructor(
     val noteNetworkSyncManager: NoteNetworkSyncManager
) : ViewModel() {

    init {
        syncCacheWithNetwork()
    }

    fun hasSyncBeenExecuted() = noteNetworkSyncManager.hasSyncBeenExecuted

    private fun syncCacheWithNetwork() {
        noteNetworkSyncManager.executeDataSync(viewModelScope)
    }

}