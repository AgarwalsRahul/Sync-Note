package com.notesync.notes.framework.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.business.interactors.auth.AuthInteractors
import com.notesync.notes.di.auth.AuthScope
import com.notesync.notes.framework.presentation.auth.AuthViewModel
import com.notesync.notes.framework.presentation.splash.NoteNetworkSyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi

@ObsoleteCoroutinesApi
@AuthScope
class AuthViewModelFactory
@Inject
constructor(
    private val authInteractors: AuthInteractors,
    private val noteNetworkSyncManager: NoteNetworkSyncManager
    ) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {

            AuthViewModel::class.java -> {
                AuthViewModel(authInteractors,noteNetworkSyncManager) as T
            }

            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}