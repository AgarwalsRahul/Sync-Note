package com.notesync.notes.business.interactors.noteList

import com.notesync.notes.business.interactors.common.DeleteNote
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState

class NoteListInteractors(
    val insertNewNote: InsertNewNote,
    val deleteNote: DeleteNote<NoteListViewState>,
    val searchNotes: SearchNotes,
    val getNumNotes: GetNumNotes,
    val restoreDeletedNote: RestoreDeletedNote,
    val deleteMultipleNotes: DeleteMultipleNotes,
    val getAllNotesFromNetwork: GetAllNotesFromNetwork,
    val getUpdatedNotes: GetUpdatedNotes
) {
}