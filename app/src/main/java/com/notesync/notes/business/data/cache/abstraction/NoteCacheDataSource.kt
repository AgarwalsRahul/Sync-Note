package com.notesync.notes.business.data.cache.abstraction

import com.notesync.notes.business.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteCacheDataSource {

    suspend fun insertNote(note: Note): Long

    suspend fun deleteNote(primaryKey: String): Int


    suspend fun deleteTrashNote(primaryKey: String): Int

    suspend fun deleteTrashNotes(notes: List<Note>): Int

    suspend fun emptyTrash(): Int

    suspend fun deleteNotes(notes: List<Note>): Int

    suspend fun updateNote(id: String, title: String, body: String, timestamp: String?): Int


     fun searchNotes(query: String, filterAndOrder: String, page: Int): Flow<List<Note>>

    suspend fun searchNoteById(id: String): Note?

    suspend fun getNumNotes(): Int

    suspend fun getNumTrashNotes(): Int

    // Only used for testing purpose
    suspend fun insertNotes(notes: List<Note>): LongArray

    suspend fun insertTrashNote(note:Note):Long

    suspend fun insertTrashNotes(notes:List<Note>):LongArray

    fun getAllNotes(): Flow<List<Note>>

    fun getTrashNotes(page:Int):Flow<List<Note>>
}