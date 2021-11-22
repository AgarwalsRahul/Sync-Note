package com.notesync.notes.framework.presentation.trash.state

import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.StateEvent
import com.notesync.notes.business.domain.state.StateMessage

sealed class TrashStateEvent : StateEvent {


    object GetNumDeletedNotesInCacheEvent : TrashStateEvent() {

        override fun errorInfo(): String {
            return "Error getting the number of notes from the cache."
        }

        override fun eventName(): String {
            return "GetNumNotesInCacheEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ) : TrashStateEvent() {

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

    class RestoreTrashNoteEvent(val note: Note) : TrashStateEvent() {
        override fun errorInfo(): String {
            return "Error restoring note."
        }

        override fun eventName(): String {
            return "RestoreTrashNoteEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class RestoreToTrashEvent(val note: Note) : TrashStateEvent() {
        override fun errorInfo(): String {
            return "Error while restoring to trash."
        }

        override fun eventName(): String {
            return "RestoreToTrashEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

    class RestoreMultipleTrashNoteEvent(val notes: List<Note>) : TrashStateEvent() {
        override fun errorInfo(): String {
            return "Error restoring notes."
        }

        override fun eventName(): String {
            return "RestoreMultipleTrashNoteEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteTrashNoteForeverEvent(val note: Note) : TrashStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting note."
        }

        override fun eventName(): String {
            return "DeleteTrashNoteForeverEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteMultipleTrashNoteForeverEvent(val notes: List<Note>) : TrashStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting notes."
        }

        override fun eventName(): String {
            return "DeleteMultipleTrashNoteForeverEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class RestoreDeletedTrashNoteEvent(val note: Note) : TrashStateEvent() {
        override fun errorInfo(): String {
            return "Error restoring note"
        }

        override fun eventName(): String {
            return "RestoreDeletedTrashNoteEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }


    class GetAllTrashNotesFromNetwork(val showProgressBar: Boolean=true) : TrashStateEvent() {

        override fun errorInfo(): String {
            return "Error while fetching notes from network"
        }

        override fun eventName(): String {
            return "GetAllNotesFromNetwork"
        }

        override fun shouldDisplayProgressBar() = showProgressBar
    }

    class GetAllTrashNotesFromCache(
        val clearLayoutStateManager: Boolean = false,
        val showProgressBar: Boolean = true
    ) : TrashStateEvent() {

        override fun errorInfo(): String {
            return "Error while fetching notes from cache"
        }

        override fun eventName(): String {
            return "GetAllTrashNotesFromCache"
        }

        override fun shouldDisplayProgressBar() = showProgressBar
    }

    object EmptyTrashEvent:TrashStateEvent(){
        override fun errorInfo(): String {
            return "Error has occurred"
        }

        override fun eventName(): String {
            return "EmptyTrashEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }
}