package com.notesync.notes.framework.presentation.trash.state

sealed class TrashToolbarState {

    class MultiSelectionState: TrashToolbarState(){

        override fun toString(): String {
            return "MultiSelectionState"
        }
    }

    class DefaultState: TrashToolbarState(){

        override fun toString(): String {
            return "DefaultState"
        }
    }
}