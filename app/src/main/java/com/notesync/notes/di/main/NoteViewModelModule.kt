package com.notesync.notes.di.main

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.state.SessionManager
import com.notesync.notes.business.interactors.noteDetail.NoteDetailInteractors
import com.notesync.notes.business.interactors.noteList.NoteListInteractors
import com.notesync.notes.business.interactors.splash.SyncDeletedNotes
import com.notesync.notes.framework.presentation.common.NoteViewModelFactory
import com.notesync.notes.framework.presentation.splash.NoteNetworkSyncManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Singleton
@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@Module
object NoteViewModelModule {
    @MainScope
    @JvmStatic
    @Provides
    fun provideNoteViewModelFactory(
        noteListInteractors: NoteListInteractors,
        noteDetailInteractors: NoteDetailInteractors,
        noteFactory: NoteFactory,
        sharedPreferences: SharedPreferences,
        editor: SharedPreferences.Editor,
        sessionManager: SessionManager,
        syncDeletedNotes: SyncDeletedNotes
    ): NoteViewModelFactory {
        return NoteViewModelFactory(
            noteListInteractors,
            noteDetailInteractors,
            noteFactory,
            editor,
            sharedPreferences,
            sessionManager,
            syncDeletedNotes
        )
    }
}