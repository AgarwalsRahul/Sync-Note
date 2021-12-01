package com.notesync.notes.framework.presentation.common

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.state.SessionManager
import com.notesync.notes.business.interactors.auth.ChangePassword
import com.notesync.notes.business.interactors.noteDetail.NoteDetailInteractors
import com.notesync.notes.business.interactors.noteList.NoteListInteractors
import com.notesync.notes.business.interactors.splash.SyncDeletedNotes
import com.notesync.notes.business.interactors.trash.TrashInteractors
import com.notesync.notes.di.main.MainScope
import com.notesync.notes.framework.presentation.changePassword.ChangePasswordViewModel
import com.notesync.notes.framework.presentation.notedetail.NoteDetailViewModel
import com.notesync.notes.framework.presentation.notelist.NoteListViewModel
import com.notesync.notes.framework.presentation.splash.NoteNetworkSyncManager
import com.notesync.notes.framework.presentation.trash.TrashViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@MainScope
@ObsoleteCoroutinesApi
class   NoteViewModelFactory
@Inject
constructor(
    private val noteListInteractors: NoteListInteractors,
    private val noteDetailInteractors: NoteDetailInteractors,
    private val trashInteractors: TrashInteractors,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences,
    private val sessionManager: SessionManager,
    private val syncDeletedNotes: SyncDeletedNotes,
    private val changePassword:ChangePassword,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {

            NoteListViewModel::class.java -> {
                NoteListViewModel(
                    noteListInteractors = noteListInteractors,
                    noteFactory = noteFactory,
                    editor = editor,
                    sharedPreferences = sharedPreferences,
                    sessionManager,
                    syncDeletedNotes
                ) as T
            }

            NoteDetailViewModel::class.java -> {
                NoteDetailViewModel(
                    noteDetailInteractors = noteDetailInteractors, sessionManager
                ) as T
            }

            MainViewModel::class.java -> {
                MainViewModel(syncDeletedNotes, noteListInteractors, sessionManager) as T
            }

            TrashViewModel::class.java->{
                TrashViewModel(noteFactory,sessionManager,trashInteractors) as T
            }

            ChangePasswordViewModel::class.java->{
                ChangePasswordViewModel(changePassword) as T
            }


            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}