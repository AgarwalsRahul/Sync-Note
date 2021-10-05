//package com.notesync.notes.buisness.interactors.noteList
//
//import com.notesync.notes.buisness.data.cache.FORCE_SEARCH_NOTES_EXCEPTION
//import com.notesync.notes.business.data.cache.CacheErrors
//import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
//import com.notesync.notes.business.domain.model.Note
//import com.notesync.notes.business.domain.model.NoteFactory
//import com.notesync.notes.business.domain.state.DataState
//import com.notesync.notes.business.interactors.noteList.SearchNotes
//import com.notesync.notes.business.interactors.noteList.SearchNotes.Companion.SEARCH_NOTES_NO_MATCHING_RESULT
//import com.notesync.notes.business.interactors.noteList.SearchNotes.Companion.SEARCH_NOTES_SUCCESS
//import com.notesync.notes.di.DependencyInjector
//import com.notesync.notes.framework.dataSource.cache.database.ORDER_BY_ASC_DATE_UPDATED
//import com.notesync.notes.framework.presentation.notelist.state.NoteListStateEvent
//import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
//import kotlinx.coroutines.InternalCoroutinesApi
//import kotlinx.coroutines.flow.FlowCollector
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.Test
//import java.lang.Exception
//import kotlin.jvm.Throws
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertTrue
//
//
///**
//Test cases:
//1. blankQuery_success_confirmNotesRetrieved()
//a) query with some default search options
//b) listen for SEARCH_NOTES_SUCCESS emitted from flow
//c) confirm notes were retrieved
//d) confirm notes in cache match with notes that were retrieved
//
//2. randomQuery_success_confirmNoResults()
//a) query with something that will yield no results
//b) listen for SEARCH_NOTES_NO_MATCHING_RESULTS emitted from flow
//c) confirm nothing was retrieved
//d) confirm there is notes in the cache
//
//3. searchNotes_fail_confirmNoResults()
//a) force an exception to be thrown
//b) listen for CACHE_ERROR_UNKNOWN emitted from flow
//c) confirm nothing was retrieved
//d) confirm there is notes in the cache
// **/
//
//
//@InternalCoroutinesApi
//class SearchNotesTest {
//
//    //system in test
//    private val searchNotes: SearchNotes
//
//    private val dependencyInjector: DependencyInjector
//    private val noteCacheDataSource: NoteCacheDataSource
//    private val noteFactory: NoteFactory
//
//    init {
//        dependencyInjector = DependencyInjector()
//        dependencyInjector.build()
//        noteCacheDataSource = dependencyInjector.noteCacheDataSource
//        noteFactory = dependencyInjector.noteFactory
//        searchNotes = SearchNotes(noteCacheDataSource)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun blankQuery_success_confirmNotesRetrieved() = runBlocking {
//        //Arrange
//        val query = ""
//        var results: ArrayList<Note>? = null
//        searchNotes.searchNotes(
//            query,
//            ORDER_BY_ASC_DATE_UPDATED,
//            1,
//            stateEvent = NoteListStateEvent.SearchNotesEvent()
//        ).collect(
//            object : FlowCollector<DataState<NoteListViewState>?> {
//                override suspend fun emit(value: DataState<NoteListViewState>?) {
//                    assertEquals(value?.stateMessage?.response?.message,SEARCH_NOTES_SUCCESS)
//
//                    value?.data?.noteList?.let {
//                        results = it
//                    }
//                }
//
//            }
//        )
//
//        //Confirm notes were recieved
//        assertTrue { results!=null }
//
//        //Confirm notes in cache matches notes retreived
//        val notesInCache = noteCacheDataSource.searchNotes(query, ORDER_BY_ASC_DATE_UPDATED,1)
//        assertTrue { results?.containsAll(notesInCache) ?:false }
//
//    }
//
//    @Test
//    fun randomQuery_success_confirmNoResults() = runBlocking {
//
//        val query = "hthrthrgrkgenrogn843nn4u34n934v53454hrth"
//        var results: ArrayList<Note>? = null
//        searchNotes.searchNotes(
//            query = query,
//            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
//            page = 1,
//            stateEvent = NoteListStateEvent.SearchNotesEvent()
//        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
//            override suspend fun emit(value: DataState<NoteListViewState>?) {
//                assertEquals(
//                    value?.stateMessage?.response?.message,
//                    SEARCH_NOTES_NO_MATCHING_RESULT
//                )
//                value?.data?.noteList?.let { list ->
//                    results = ArrayList(list)
//                }
//            }
//        })
//
//        // confirm nothing was retrieved
//        assertTrue { results?.run { size == 0 }?: true }
//
//        // confirm there is notes in the cache
//        val notesInCache = noteCacheDataSource.searchNotes(
//            query = "",
//            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
//            page = 1
//        )
//        assertTrue { notesInCache.size > 0}
//    }
//
//    @Test
//    fun searchNotes_fail_confirmNoResults() = runBlocking {
//
//        val query = FORCE_SEARCH_NOTES_EXCEPTION
//        var results: ArrayList<Note>? = null
//        searchNotes.searchNotes(
//            query = query,
//            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
//            page = 1,
//            stateEvent = NoteListStateEvent.SearchNotesEvent()
//        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
//            override suspend fun emit(value: DataState<NoteListViewState>?) {
//                assert(
//                    value?.stateMessage?.response?.message
//                        ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
//                )
//                value?.data?.noteList?.let { list ->
//                    results = ArrayList(list)
//                }
//                println("results: $results")
//            }
//        })
//
//        // confirm nothing was retrieved
//        assertTrue { results?.run { size == 0 }?: true }
//
//        // confirm there is notes in the cache
//        val notesInCache = noteCacheDataSource.searchNotes(
//            query = "",
//            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
//            page = 1
//        )
//        assertTrue { notesInCache.isNotEmpty() }
//    }
//
//}