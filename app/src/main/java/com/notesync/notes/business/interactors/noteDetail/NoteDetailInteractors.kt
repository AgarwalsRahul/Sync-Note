package com.notesync.notes.business.interactors.noteDetail

import com.notesync.notes.business.interactors.common.DeleteNote
import com.notesync.notes.business.interactors.noteList.InsertNewNote
import com.notesync.notes.framework.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FlowPreview
class NoteDetailInteractors (
     val deleteNote:DeleteNote<NoteDetailViewState>,
     val updateNote: UpdateNote,
     val makeACopy: MakeACopy

)