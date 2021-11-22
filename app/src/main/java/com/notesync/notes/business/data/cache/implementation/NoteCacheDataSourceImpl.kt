package com.notesync.notes.business.data.cache.implementation

import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.framework.dataSource.cache.abstraction.NoteDaoService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCacheDataSourceImpl
@Inject constructor(private val noteDaoService: NoteDaoService) :
    NoteCacheDataSource {
    override suspend fun insertNote(note: Note): Long {
        return noteDaoService.insertNote(note)
    }

    override suspend fun deleteNote(primaryKey: String): Int {
        return noteDaoService.deleteNote(primaryKey)
    }

    override suspend fun deleteTrashNote(primaryKey: String): Int {
        return noteDaoService.deleteTrashNote(primaryKey)
    }

    override suspend fun deleteTrashNotes(notes: List<Note>): Int {
        return noteDaoService.deleteTrashNotes(notes)
    }

    override suspend fun emptyTrash(): Int {
       return noteDaoService.emptyTrash()
    }

    override suspend fun deleteNotes(notes: List<Note>): Int {
        return noteDaoService.deleteNotes(notes)
    }

    override suspend fun updateNote(
        id: String,
        title: String,
        body: String,
        timestamp: String?
    ): Int {
        return noteDaoService.updateNote(id, title, body, timestamp)
    }

    override  fun searchNotes(query: String, filterAndOrder: String, page: Int): Flow<List<Note>> {
        return noteDaoService.returnOrderedQuery(query, filterAndOrder, page)
    }

    override suspend fun searchNoteById(id: String): Note? {
        return noteDaoService.searchNoteById(id)
    }

    override suspend fun getNumNotes(): Int {
        return noteDaoService.getNumNotes()
    }

    override suspend fun getNumTrashNotes(): Int {
        return noteDaoService.getNumTrashNotes()
    }

    override suspend fun insertNotes(notes: List<Note>): LongArray {
        return noteDaoService.insertNotes(notes)
    }

    override suspend fun insertTrashNote(note: Note): Long {
        return noteDaoService.insertTrashNote(note)
    }

    override suspend fun insertTrashNotes(notes: List<Note>): LongArray {
        return noteDaoService.insertTrashNotes(notes)
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDaoService.getAllNotes()
    }

    override fun getTrashNotes(page: Int): Flow<List<Note>> {
        return noteDaoService.getTrashNotes(page)
    }
}