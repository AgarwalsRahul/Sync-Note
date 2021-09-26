package com.notesync.notes.buisness.interactors.common

import com.notesync.notes.buisness.data.cache.FORCE_DELETE_NOTE_EXCEPTION
import com.notesync.notes.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.state.DataState
import com.notesync.notes.business.interactors.common.DeleteNote
import com.notesync.notes.business.interactors.common.DeleteNote.Companion.DELETE_FAILURE
import com.notesync.notes.business.interactors.common.DeleteNote.Companion.DELETE_SUCCESS
import com.notesync.notes.di.DependencyInjector

import com.notesync.notes.framework.presentation.notelist.state.NoteListStateEvent.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/**
Test cases:
1. deleteNote_success_confirmNetworkUpdated()
    a) delete a note
    b) check for success message from flow emission
    c) confirm note was deleted from "notes" node in network
    d) confirm note was added to "deletes" node in network
2. deleteNote_fail_confirmNetworkUnchanged()
    a) attempt to delete a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm network was not changed
3. throwException_checkGenericError_confirmNetworkUnchanged()
    a) attempt to delete a note, force an exception to throw
    b) check for failure message from flow emission
    c) confirm network was not changed
 **/
@InternalCoroutinesApi
class DeleteNoteTest {

    // system in test
    private val deleteNotes: DeleteNote<NoteListViewState>

    // dependencies
    private val dependencyContainer: DependencyInjector
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyInjector()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        deleteNotes = DeleteNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNote_success_confirmNetworkUpdated() = runBlocking {

        // choose a note at random to delete
        // select a random note to update
        val noteToDelete = noteCacheDataSource
            .searchNotes("", "", 1).get(0)

        deleteNotes.deleteNote(
            noteToDelete,
            DeleteNoteEvent(noteToDelete)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_SUCCESS
                )
            }
        })

        // confirm was deleted from "notes" node
        val wasNoteDeleted = !noteNetworkDataSource.getAllNotes()
            .contains(noteToDelete)
        assertTrue { wasNoteDeleted }

        // confirm was inserted into "deletes" node
        val wasDeletedNoteInserted = noteNetworkDataSource.getDeletedNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

    @Test
    fun deleteNote_fail_confirmNetworkUnchanged() =  runBlocking{

        // create a note to delete that doesn't exist in data set
        val noteToDelete = Note(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )


        deleteNotes.deleteNote(
            noteToDelete,
            DeleteNoteEvent(noteToDelete)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_FAILURE
                )
            }
        })

        // confirm nothing was deleted from "notes" node
        val notes = noteNetworkDataSource.getAllNotes()
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotesInCache == notes.size}

        // confirm was NOT inserted into "deletes" node
        val wasDeletedNoteInserted = !noteNetworkDataSource.getDeletedNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkUnchanged() = runBlocking{

        // create a note to delete that will throw exception
        val noteToDelete = Note(
            id = FORCE_DELETE_NOTE_EXCEPTION ,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )


        deleteNotes.deleteNote(
            noteToDelete,
            DeleteNoteEvent(noteToDelete)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        // confirm nothing was deleted from "notes" node
        val notes = noteNetworkDataSource.getAllNotes()
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotesInCache == notes.size}

        // confirm was NOT inserted into "deletes" node
        val wasDeletedNoteInserted = !noteNetworkDataSource.getDeletedNotes()
            .contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

}