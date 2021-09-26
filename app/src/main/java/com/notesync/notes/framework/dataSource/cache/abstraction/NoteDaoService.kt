package com.notesync.notes.framework.dataSource.cache.abstraction

import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.framework.dataSource.cache.database.NOTE_PAGINATION_PAGE_SIZE

interface NoteDaoService {


    suspend fun insertNote(note: Note): Long

    suspend fun deleteNote(primaryKey: String): Int

    suspend fun deleteNotes(notes: List<Note>): Int

    suspend fun updateNote(id: String, title: String, body: String?, timestamp: String?): Int


    suspend fun searchNotes(): List<Note>

    suspend fun searchNotesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>

    suspend fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>

    suspend fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>

    suspend fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>

    suspend fun searchNoteById(id: String): Note?

    suspend fun getNumNotes(): Int

    suspend fun getAllNotes(): List<Note>

    // Only used for testing purpose
    suspend fun insertNotes(notes: List<Note>): LongArray

    suspend fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Note>
}