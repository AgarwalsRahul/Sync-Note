//package com.notesync.notes.buisness.interactors.noteList
//
//import com.notesync.notes.buisness.data.cache.FORCE_GENERAL_FAILURE
//import com.notesync.notes.buisness.data.cache.FORCE_NEW_NOTE_EXCEPTION
//import com.notesync.notes.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
//import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
//import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
//import com.notesync.notes.business.domain.model.NoteFactory
//import com.notesync.notes.business.domain.state.DataState
//import com.notesync.notes.business.interactors.noteList.InsertNewNote
//import com.notesync.notes.business.interactors.noteList.InsertNewNote.Companion.INSERT_NOTE_FAILED
//import com.notesync.notes.business.interactors.noteList.InsertNewNote.Companion.INSERT_NOTE_SUCCESS
//import com.notesync.notes.di.DependencyInjector
//import com.notesync.notes.framework.presentation.notelist.state.NoteListStateEvent
//import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
//
//
//import kotlinx.coroutines.InternalCoroutinesApi
//import kotlinx.coroutines.flow.FlowCollector
//import kotlinx.coroutines.runBlocking
//
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.Test
//import org.junit.runner.RunWith
//import java.lang.Exception
//import java.util.*
//import kotlin.jvm.Throws
//
///**
//Test cases:
//1. insertNote_success_confirmNetworkAndCacheUpdated()
//a) insert a new note
//b) listen for INSERT_NOTE_SUCCESS emission from flow
//c) confirm cache was updated with new note
//d) confirm network was updated with new note
//2. insertNote_fail_confirmNetworkAndCacheUnchanged()
//a) insert a new note
//b) force a failure (return -1 from db operation)
//c) listen for INSERT_NOTE_FAILED emission from flow
//e) confirm cache was not updated
//e) confirm network was not updated
//3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
//a) insert a new note
//b) force an exception
//c) listen for CACHE_ERROR_UNKNOWN emission from flow
//e) confirm cache was not updated
//e) confirm network was not updated
// **/
//
//@InternalCoroutinesApi
//
//class InsertNewNoteTest {
//
//    //system in test
//    private val insertNewNote: InsertNewNote
//
//    // dependencies
//    private val dependencyInjector: DependencyInjector
//    private val noteCacheDataSource: NoteCacheDataSource
//    private val noteNetworkDataSource: NoteNetworkDataSource
//    private val noteFactory: NoteFactory
//
//    init {
//        dependencyInjector = DependencyInjector()
//        dependencyInjector.build()
//        noteCacheDataSource = dependencyInjector.noteCacheDataSource
//        noteNetworkDataSource = dependencyInjector.noteNetworkDataSource
//        noteFactory = dependencyInjector.noteFactory
//        insertNewNote = InsertNewNote(noteCacheDataSource, noteNetworkDataSource, noteFactory)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun insertNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
//        //Arrange
//        val newNote = noteFactory.createSingleNote(null, UUID.randomUUID().toString())
//        //Act
//        insertNewNote.insertNewNote(
//            newNote.id,
//            newNote.title,
//            NoteListStateEvent.InsertNewNoteEvent(newNote.title)
//        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
//            override suspend fun emit(value: DataState<NoteListViewState>?) {
//                assertEquals(value?.stateMessage?.response?.message, INSERT_NOTE_SUCCESS)
//            }
//
//        })
//        //Confirm cache was updated
//        val insertedCachedNote = noteCacheDataSource.searchNoteById(newNote.id)
//        assertTrue { insertedCachedNote == newNote }
//
//        //Confirm network was updated
//        val insertedNetworkNote = noteNetworkDataSource.searchNote(newNote)
//        assertTrue { insertedNetworkNote == newNote }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun insertNote_fail_confirmNetworkAndCacheUnchanged()= runBlocking {
//        //Arrange
//        val newNote = noteFactory.createSingleNote(FORCE_GENERAL_FAILURE, UUID.randomUUID().toString())
//        //Act
//        insertNewNote.insertNewNote(
//            newNote.id,
//            newNote.title,
//            NoteListStateEvent.InsertNewNoteEvent(newNote.title)
//        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
//            override suspend fun emit(value: DataState<NoteListViewState>?) {
//                assertEquals(value?.stateMessage?.response?.message, INSERT_NOTE_FAILED)
//            }
//
//        })
//        //Confirm cache was updated
//        val insertedCachedNote = noteCacheDataSource.searchNoteById(newNote.id)
//        assertTrue { insertedCachedNote == null }
//
//        //Confirm network was updated
//        val insertedNetworkNote = noteNetworkDataSource.searchNote(newNote)
//        assertTrue { insertedNetworkNote == null }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged()= runBlocking {
//        //Arrange
//        val newNote = noteFactory.createSingleNote(FORCE_NEW_NOTE_EXCEPTION, UUID.randomUUID().toString())
//        //Act
//        insertNewNote.insertNewNote(
//            newNote.id,
//            newNote.title,
//            NoteListStateEvent.InsertNewNoteEvent(newNote.title)
//        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
//            override suspend fun emit(value: DataState<NoteListViewState>?) {
//                assert(value?.stateMessage?.response?.message?.contains(CACHE_ERROR_UNKNOWN)?:false)
//            }
//
//        })
//        //Confirm cache was updated
//        val insertedCachedNote = noteCacheDataSource.searchNoteById(newNote.id)
//        assertTrue { insertedCachedNote == null }
//
//        //Confirm network was updated
//        val insertedNetworkNote = noteNetworkDataSource.searchNote(newNote)
//        assertTrue { insertedNetworkNote == null }
//    }
//
//}