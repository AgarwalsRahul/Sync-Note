package com.notesync.notes.framework.presentation.common

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.interactors.noteDetail.NoteDetailInteractors
import com.notesync.notes.business.interactors.noteList.NoteListInteractors
import com.notesync.notes.framework.presentation.notedetail.NoteDetailViewModel
import com.notesync.notes.framework.presentation.notelist.NoteListViewModel
import com.notesync.notes.framework.presentation.splash.NoteNetworkSyncManager
import com.notesync.notes.framework.presentation.splash.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
@ObsoleteCoroutinesApi
class NoteViewModelFactory
@Inject
constructor(
    private val noteListInteractors: NoteListInteractors,
    private val noteDetailInteractors: NoteDetailInteractors,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences,
    private val noteNetworkSyncManager: NoteNetworkSyncManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {

            NoteListViewModel::class.java -> {
                NoteListViewModel(
                    noteListInteractors = noteListInteractors,
                    noteFactory = noteFactory,
                    editor = editor,
                    sharedPreferences = sharedPreferences
                ) as T
            }

            NoteDetailViewModel::class.java -> {
                NoteDetailViewModel(
                    noteDetailInteractors = noteDetailInteractors
                ) as T
            }

            SplashViewModel::class.java -> {
                SplashViewModel(noteNetworkSyncManager) as T
            }

            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}