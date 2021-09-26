package com.notesync.notes.business.interactors.noteDetail

import com.notesync.notes.business.interactors.common.DeleteNote
import com.notesync.notes.framework.presentation.notedetail.state.NoteDetailViewState
import javax.inject.Inject

class NoteDetailInteractors (
     val deleteNote:DeleteNote<NoteDetailViewState>,
     val updateNote: UpdateNote
)