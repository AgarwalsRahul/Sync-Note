package com.notesync.notes.framework.presentation.trash.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.framework.presentation.notelist.state.NoteListToolbarState
import com.notesync.notes.framework.presentation.notelist.state.NoteListToolbarState.*

class TrashInteractionManager {

    private val _selectedNotes: MutableLiveData<ArrayList<Note>> = MutableLiveData()

    private val _toolbarState: MutableLiveData<TrashToolbarState>
            = MutableLiveData(TrashToolbarState.DefaultState())

    val selectedNotes: LiveData<ArrayList<Note>>
        get() = _selectedNotes

    val toolbarState: LiveData<TrashToolbarState>
        get() = _toolbarState

    fun setToolbarState(state: TrashToolbarState){
        _toolbarState.value = state
    }

    fun getSelectedNotes():ArrayList<Note> = _selectedNotes.value?: ArrayList()

    fun isMultiSelectionStateActive(): Boolean{
        return _toolbarState.value.toString() == MultiSelectionState().toString()
    }

    fun addOrRemoveNoteFromSelectedList(note: Note){
        var list = _selectedNotes.value
        if(list == null){
            list = ArrayList()
        }
        if (list.contains(note)){
            list.remove(note)
        }
        else{
            list.add(note)
        }
        _selectedNotes.value = list
    }

    fun isNoteSelected(note: Note): Boolean{
        return _selectedNotes.value?.contains(note)?: false
    }

    fun clearSelectedNotes(){
        _selectedNotes.value = null
    }

}

