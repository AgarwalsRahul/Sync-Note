package com.notesync.notes.framework.dataSource.cache.abstraction

import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.framework.dataSource.cache.database.NOTE_PAGINATION_PAGE_SIZE
import kotlinx.coroutines.flow.Flow


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

    suspend fun getNumTrashNotes(): Int

    fun getAllNotes(): Flow<List<Note>>

    suspend fun deleteTrashNote(primaryKey: String): Int

    suspend fun deleteTrashNotes(notes: List<Note>): Int

    suspend fun emptyTrash():Int

    // Only used for testing purpose
    suspend fun insertNotes(notes: List<Note>): LongArray

    suspend fun insertTrashNote(note: Note): Long

    suspend fun insertTrashNotes(notes: List<Note>): LongArray

    fun getTrashNotes(page:Int):Flow<List<Note>>

    fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): Flow<List<Note>>
}