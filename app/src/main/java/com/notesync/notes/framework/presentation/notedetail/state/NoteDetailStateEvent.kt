package com.notesync.notes.framework.presentation.notedetail.state

import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.StateEvent
import com.notesync.notes.business.domain.state.StateMessage
import com.notesync.notes.framework.presentation.notelist.state.NoteListStateEvent

sealed class NoteDetailStateEvent : StateEvent {


    class UpdateNoteEvent : NoteDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error updating note."
        }

        override fun eventName(): String {
            return "UpdateNoteEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteNoteEvent(
        val note: Note
    ) : NoteDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error deleting note."
        }

        override fun eventName(): String {
            return "DeleteNoteEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ) : NoteDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

    class MakeACopyEvent(
        val title: String,
        val body:String?=null
    ) : NoteDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error copying a note."
        }

        override fun eventName(): String {
            return "MakeACopyEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

}