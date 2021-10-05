package com.notesync.notes.di.auth

import com.notesync.notes.business.interactors.auth.AuthInteractors
import com.notesync.notes.di.auth.AuthScope
import com.notesync.notes.framework.presentation.common.AuthViewModelFactory
import com.notesync.notes.framework.presentation.splash.NoteNetworkSyncManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@Module
object AuthViewModelModule {

    @AuthScope
    @JvmStatic
    @Provides
    fun provideAuthViewModelFactory(
        authInteractors: AuthInteractors,
        networkSyncManager: NoteNetworkSyncManager
    ): AuthViewModelFactory {
        return AuthViewModelFactory(
            authInteractors, networkSyncManager
        )
    }
}