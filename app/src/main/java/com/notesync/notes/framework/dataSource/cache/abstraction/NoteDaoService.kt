package com.notesync.notes.framework.dataSource.cache.abstraction

import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.framework.dataSource.cache.database.NOTE_PAGINATION_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import java.util.*


interface NoteDaoService {


    suspend fun insertNote(note: Note): Long

    suspend fun deleteNote(primaryKey: String): Int

    suspend fun deleteNotes(notes: List<Note>): Int

    suspend fun updateNote(id: String, title: String, body: String?, timestamp: String?): Int


    fun searchNotes(): Flow<List<Note>>



     fun searchNotesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): Flow<List<Note>>

     fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): Flow<List<Note>>

     fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): Flow<List<Note>>

     fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): Flow<List<Note>>

    suspend fun searchNoteById(id: String): Note?

    suspend fun getNumNotes(): Int

     fun getAllNotes(): Flow<List<Note>>

    // Only used for testing purpose
    suspend fun insertNotes(notes: List<Note>): LongArray

     fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): Flow<List<Note>>
}