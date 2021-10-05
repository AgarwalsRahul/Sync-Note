package com.notesync.notes.framework.presentation.notedetail

import androidx.lifecycle.LiveData
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.DataState
import com.notesync.notes.business.domain.state.StateEvent
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.interactors.noteDetail.NoteDetailInteractors
import com.notesync.notes.business.interactors.noteDetail.UpdateNote.Companion.UPDATE_NOTE_FAILED
import com.notesync.notes.framework.dataSource.cache.model.NoteCacheEntity
import com.notesync.notes.framework.presentation.common.BaseViewModel
import com.notesync.notes.framework.presentation.notedetail.state.*
import com.notesync.notes.framework.presentation.notedetail.state.CollapsingToolbarState.*
import com.notesync.notes.framework.presentation.notedetail.state.NoteDetailStateEvent.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow

const val NOTE_DETAIL_ERROR_RETRIEVEING_SELECTED_NOTE =
    "Error retrieving selected note from bundle."
const val NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY = "selectedNote"
const val NOTE_TITLE_CANNOT_BE_EMPTY = "Note title can not be empty."

@ObsoleteCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class NoteDetailViewModel(
    private val noteDetailInteractors: NoteDetailInteractors,
    private val sessionManager: SessionManager
) :
    BaseViewModel<NoteDetailViewState>() {
    private val noteInteractionManager: NoteInteractionManager =
        NoteInteractionManager()
    val noteTitleInteractionState: LiveData<NoteInteractionState>
        get() = noteInteractionManager.noteTitleState
    val noteBodyInteractionState: LiveData<NoteInteractionState>
        get() = noteInteractionManager.noteBodyState
    val collapsingToolbarState: LiveData<CollapsingToolbarState>
        get() = noteInteractionManager.collapsingToolbarState

    override fun handleNewData(data: NoteDetailViewState) {
        // no data coming in from requests...
    }


    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedUser.value?.let {
            val job: Flow<DataState<NoteDetailViewState>?> = when (stateEvent) {

                is UpdateNoteEvent -> {
                    val pk = getNote()?.id
                    if (!isNoteTitleNull() && pk != null) {
                        noteDetailInteractors.updateNote.updateNote(
                            note = getNote()!!,
                            stateEvent = stateEvent,it
                        )
                    } else {
                        emitStateMessageEvent(
                            stateMessage = StateMessage(
                                response = Response(
                                    message = UPDATE_NOTE_FAILED,
                                    uiComponentType = UIComponentType.Dialog(),
                                    messageType = MessageType.Error()
                                )
                            ),
                            stateEvent = stateEvent
                        )
                    }
                }

                is DeleteNoteEvent -> {
                    noteDetailInteractors.deleteNote.deleteNote(
                        note = stateEvent.note,
                        stateEvent = stateEvent,it
                    )
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

    fun beginPendingDelete(note: Note) {
        setStateEvent(
            DeleteNoteEvent(
                note = note
            )
        )
    }

    private fun isNoteTitleNull(): Boolean {
        val title = getNote()?.title
        if (title.isNullOrBlank()) {
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = NOTE_TITLE_CANNOT_BE_EMPTY,
                            uiComponentType = UIComponentType.Dialog(),
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

    fun getNote(): Note? {
        return getCurrentViewStateOrNew().note
    }

    override fun initNewViewState(): NoteDetailViewState {
        return NoteDetailViewState()
    }

    fun setNote(note: Note?) {
        val update = getCurrentViewStateOrNew()
        update.note = note
        setViewState(update)
    }

    fun setCollapsingToolbarState(
        state: CollapsingToolbarState
    ) = noteInteractionManager.setCollapsingToolbarState(state)

    fun updateNote(title: String?, body: String?) {
        updateNoteTitle(title)
        updateNoteBody(body)
    }

    fun updateNoteTitle(title: String?) {
        if (title == null) {
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = NoteCacheEntity.nullTitleError(),
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        )
                    )
                )
            )
        } else {
            val update = getCurrentViewStateOrNew()
            val updatedNote = update.note?.copy(
                title = title
            )
            update.note = updatedNote
            setViewState(update)
        }
    }

    fun updateNoteBody(body: String?) {
        val update = getCurrentViewStateOrNew()
        val updatedNote = update.note?.copy(
            body = body ?: ""
        )
        update.note = updatedNote
        setViewState(update)
    }

    fun setNoteInteractionTitleState(state: NoteInteractionState) {
        noteInteractionManager.setNewNoteTitleState(state)
    }

    fun setNoteInteractionBodyState(state: NoteInteractionState) {
        noteInteractionManager.setNewNoteBodyState(state)
    }

    fun isToolbarCollapsed() = collapsingToolbarState.toString()
        .equals(Collapsed().toString())

    fun setIsUpdatePending(isPending: Boolean) {
        val update = getCurrentViewStateOrNew()
        update.isUpdatePending = isPending
        setViewState(update)
    }

    fun getIsUpdatePending(): Boolean {
        return getCurrentViewStateOrNew().isUpdatePending ?: false
    }

    fun isToolbarExpanded() = collapsingToolbarState.toString()
        .equals(Expanded().toString())

    // return true if in EditState
    fun checkEditState() = noteInteractionManager.checkEditState()

    fun exitEditState() = noteInteractionManager.exitEditState()

    fun isEditingTitle() = noteInteractionManager.isEditingTitle()

    fun isEditingBody() = noteInteractionManager.isEditingBody()

    // force observers to refresh
    fun triggerNoteObservers() {
        getCurrentViewStateOrNew().note?.let { note ->
            setNote(note)
        }
    }
}