package com.notesync.notes.business.interactors.noteList

import com.notesync.notes.business.interactors.common.DeleteNote
import com.notesync.notes.business.interactors.splash.SyncNotes
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FlowPreview
class NoteListInteractors(
    val insertNewNote: InsertNewNote,
    val deleteNote: DeleteNote<NoteListViewState>,
    val searchNotes: SearchNotes,
    val getNumNotes: GetNumNotes,
    val restoreDeletedNote: RestoreDeletedNote,
    val deleteMultipleNotes: DeleteMultipleNotes,
    val getAllNotesFromNetwork: GetAllNotesFromNetwork,
    val getUpdatedNotes: GetUpdatedNotes,
    val syncNotes: SyncNotes
) {
}