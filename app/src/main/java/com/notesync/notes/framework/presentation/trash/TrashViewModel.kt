package com.notesync.notes.framework.presentation.trash

import android.os.Parcelable
import androidx.lifecycle.LiveData
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.interactors.noteList.DeleteMultipleNotes
import com.notesync.notes.business.interactors.trash.EmptyTrash
import com.notesync.notes.business.interactors.trash.TrashInteractors
import com.notesync.notes.framework.presentation.common.BaseViewModel
import com.notesync.notes.framework.presentation.notelist.DELETE_PENDING_ERROR
import com.notesync.notes.framework.presentation.notelist.state.NoteListStateEvent
import com.notesync.notes.framework.presentation.trash.state.TrashInteractionManager
import com.notesync.notes.framework.presentation.trash.state.TrashStateEvent
import com.notesync.notes.framework.presentation.trash.state.TrashStateEvent.*
import com.notesync.notes.framework.presentation.trash.state.TrashToolbarState
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow


@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class TrashViewModel(
    private val noteFactory: NoteFactory,
    private val sessionManager: SessionManager,
    private val trashInteractors: TrashInteractors
) : BaseViewModel<TrashViewState>() {

    val trashInteractionManager =
        TrashInteractionManager()

    val toolbarState: LiveData<TrashToolbarState>
        get() = trashInteractionManager.toolbarState

    override fun handleNewData(data: TrashViewState) {
        data.let { viewState ->
            viewState.noteList?.let { noteList ->
                setNoteListData(noteList)
            }

            viewState.numNotesInCache?.let { numNotes ->
                setNumNotesInCache(numNotes)
            }

            viewState.newNote?.let { note ->
                setNote(note)
            }

            viewState.notePendingDelete?.let { restoredNote ->
                restoredNote.note?.let { note ->
                    setRestoredNoteId(note)
                }
                setNotePendingDelete(null)
            }
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedUser.value?.let {
            val job: Flow<DataState<TrashViewState>?> = when (stateEvent) {

                is GetAllTrashNotesFromCache -> {
                    if(stateEvent.clearLayoutStateManager){
                        clearLayoutManagerState()
                    }
                    trashInteractors.getTrashNotes.getTrashNotes(getPage(), stateEvent = stateEvent)
                }

                is GetAllTrashNotesFromNetwork -> {
                    trashInteractors.getTrashNotesFromNetwork.getNotes(stateEvent,it)
                }

                is RestoreDeletedTrashNoteEvent -> {
                    trashInteractors.restoreDeletedTrashNote.restore(
                        stateEvent.note,
                        stateEvent,
                        it
                    )
                }

                is DeleteTrashNoteForeverEvent -> {
                    trashInteractors.deleteTrashNote.deleteTrashNote(
                        stateEvent.note,
                        stateEvent,
                        it
                    )
                }

                is DeleteMultipleTrashNoteForeverEvent -> {
                    trashInteractors.deleteMultipleTrashNote.deleteNotes(
                        stateEvent.notes,
                        stateEvent,
                        it
                    )
                }

                is EmptyTrashEvent->{
                    trashInteractors.emptyTrash.emptyTrash(
                        getCurrentViewStateOrNew().noteList!!,
                        stateEvent,it)
                }

                is GetNumDeletedNotesInCacheEvent->{
                    trashInteractors.getTrashNumNotes.getNumTrashNotes(stateEvent)
                }


                is CreateStateMessageEvent -> {
                    emitStateMessageEvent(
                        stateMessage = stateEvent.stateMessage,
                        stateEvent = stateEvent
                    )
                }

                else -> {
                    emitInvalidStateEvent(stateEvent)
                }
            }
            launchJob(stateEvent, job)
        } ?: sessionManager.logout()
    }

    /*
        Setters
     */
    private fun setNoteListData(notesList: ArrayList<Note>) {
        val update = getCurrentViewStateOrNew()
        update.noteList = notesList
        setViewState(update)
    }

    // can be selected from Recyclerview or created new from dialog
    fun setNote(note: Note?) {
        val update = getCurrentViewStateOrNew()
        update.newNote = note

        setViewState(update)
    }

    // if a note is deleted and then restored, the id will be incorrect.
    // So need to reset it here.
    private fun setRestoredNoteId(restoredNote: Note) {
        val update = getCurrentViewStateOrNew()
        update.noteList?.let { noteList ->
            for ((index, note) in noteList.withIndex()) {
                if (note.title.equals(restoredNote.title)) {
                    noteList.remove(note)
                    noteList.add(index, restoredNote)
                    update.noteList = noteList
                    break
                }
            }
        }
        setViewState(update)
    }


    override fun initNewViewState(): TrashViewState {
        return TrashViewState()
    }

    /*
    State
 */
    fun getSelectedNotes() = trashInteractionManager.getSelectedNotes()

    fun setToolbarState(state: TrashToolbarState) =
        trashInteractionManager.setToolbarState(state)

    fun isMultiSelectionStateActive() = trashInteractionManager.isMultiSelectionStateActive()

    fun addOrRemoveNoteFromSelectedList(note: Note) =
        trashInteractionManager.addOrRemoveNoteFromSelectedList(note)

    fun isNoteSelected(note: Note): Boolean = trashInteractionManager.isNoteSelected(note)

    fun clearSelectedNotes() = trashInteractionManager.clearSelectedNotes()

    private fun getPage(): Int {
        return getCurrentViewStateOrNew().page
            ?: return 1
    }

    fun getNoteListSize() = getCurrentViewStateOrNew().noteList?.size ?: 0

    private fun getNumNotesInCache() = getCurrentViewStateOrNew().numNotesInCache ?: 0

    // for debugging
    fun getActiveJobs() = dataChannelManager.getActiveJobs()

    fun getLayoutManagerState(): Parcelable? {
        return getCurrentViewStateOrNew().layoutManagerState
    }

    private fun findListPositionOfNote(note: Note?): Int {
        val viewState = getCurrentViewStateOrNew()
        viewState.noteList?.let { noteList ->
            for ((index, item) in noteList.withIndex()) {
                if (item.id == note?.id) {
                    return index
                }
            }
        }
        return 0
    }

    fun isPaginationExhausted() = getNoteListSize() >= getNumNotesInCache()

    fun isQueryExhausted(): Boolean {
        printLogD(
            "NoteListViewModel",
            "is query exhasuted? ${getCurrentViewStateOrNew().isQueryExhausted ?: true}"
        )
        return getCurrentViewStateOrNew().isQueryExhausted ?: true
    }

    fun setQueryExhausted(isExhausted: Boolean) {
        val update = getCurrentViewStateOrNew()
        update.isQueryExhausted = isExhausted
        setViewState(update)
    }

    private fun removePendingNoteFromList(note: Note?) {
        val update = getCurrentViewStateOrNew()
        val list = update.noteList
        if (list?.contains(note) == true) {
            list.remove(note)
            update.noteList = list
            setViewState(update)
        }
    }

    fun setNotePendingDelete(note: Note?) {
        val update = getCurrentViewStateOrNew()
        if (note != null) {
            update.notePendingDelete = TrashViewState.NotePendingDelete(
                note = note,
                listPosition = findListPositionOfNote(note)
            )
        } else {
            update.notePendingDelete = null
        }
        setViewState(update)
    }

    private fun setNumNotesInCache(numNotes: Int) {
        val update = getCurrentViewStateOrNew()
        update.numNotesInCache = numNotes
        setViewState(update)
    }

    fun clearList() {
        printLogD("ListViewModel", "clearList")
        val update = getCurrentViewStateOrNew()
        update.noteList = ArrayList()
        setViewState(update)
    }

    private fun resetPage() {
        val update = getCurrentViewStateOrNew()
        update.page = 1
        setViewState(update)
    }


    private fun incrementPageNumber() {
        val update = getCurrentViewStateOrNew()
        val page = update.copy().page ?: 1
        update.page = page.plus(1)
        setViewState(update)
    }

    fun setLayoutManagerState(layoutManagerState: Parcelable) {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = layoutManagerState
        setViewState(update)
    }

    private fun clearLayoutManagerState() {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = null
        setViewState(update)
    }

    private fun removeSelectedNotesFromList() {
        val update = getCurrentViewStateOrNew()
        update.noteList?.removeAll(getSelectedNotes())
        setViewState(update)
        clearSelectedNotes()
    }

    /*
        StateEvent Triggers
     */


    fun deleteNotes() {
        if (getSelectedNotes().size > 0) {
            setStateEvent(DeleteMultipleTrashNoteForeverEvent(getSelectedNotes()))
            removeSelectedNotesFromList()
        } else {
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DeleteMultipleNotes.DELETE_NOTES_YOU_MUST_SELECT,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
        }
    }

    private fun removeAllTrashNotes(){
        val update = getCurrentViewStateOrNew()
        update.noteList=ArrayList<Note>()
        setViewState(update)
    }

    fun emptyTrash(){
        if(getNumNotesInCache()>0){
            setStateEvent(EmptyTrashEvent)
            removeAllTrashNotes()
        }else{
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = EmptyTrash.EMPTY_TRASH_NO_NOTES,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
        }
    }

    fun isDeletePending(): Boolean {
        val pendingNote = getCurrentViewStateOrNew().notePendingDelete
        if (pendingNote != null) {
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_PENDING_ERROR,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
            return true
        } else {
            return false
        }
    }


    fun undoDelete() {
        // replace note in viewstate
        val update = getCurrentViewStateOrNew()
        update.notePendingDelete?.let { note ->
            if (note.listPosition != null && note.note != null) {
                update.noteList?.add(
                    note.listPosition as Int,
                    note.note as Note
                )
                setStateEvent(RestoreDeletedTrashNoteEvent(note.note as Note))
            }
        }
        setViewState(update)
    }

    fun beginPendingDelete(note: Note) {
        setNotePendingDelete(note)
        removePendingNoteFromList(note)
        setStateEvent(
            DeleteTrashNoteForeverEvent(
                note = note
            )
        )
    }

    fun loadFirstPage() {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(GetAllTrashNotesFromNetwork(false))

    }

    fun nextPage() {
        if (!isQueryExhausted()) {
            printLogD("NoteListViewModel", "attempting to load next page...")
            clearLayoutManagerState()
            incrementPageNumber()
            setStateEvent(GetAllTrashNotesFromCache(showProgressBar = false))
        }
    }

    fun retrieveNumNotesInCache() {
        setStateEvent(GetNumDeletedNotesInCacheEvent)
    }

    fun refreshSearchQuery() {
        setQueryExhausted(false)
        setStateEvent(GetAllTrashNotesFromCache(false, showProgressBar = false))
    }
//


}