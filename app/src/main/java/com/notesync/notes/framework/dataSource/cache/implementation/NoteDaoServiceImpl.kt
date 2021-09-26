package com.notesync.notes.framework.dataSource.cache.implementation

import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.framework.dataSource.cache.abstraction.NoteDaoService
import com.notesync.notes.framework.dataSource.cache.mappers.CacheMapper
import com.notesync.notes.framework.dataSource.cache.database.NoteDao
import com.notesync.notes.framework.dataSource.cache.database.returnOrderedQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDaoServiceImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val cacheMapper: CacheMapper,
    private val dateUtil: DateUtil
) : NoteDaoService {
    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(cacheMapper.mapToEntity(note))
    }

    override suspend fun deleteNote(primaryKey: String): Int {
        return noteDao.deleteNote(primaryKey)
    }

    override suspend fun deleteNotes(notes: List<Note>): Int {
        val ids = notes.mapIndexed { _, note -> note.id }
        return noteDao.deleteNotes(ids)
    }

    override suspend fun updateNote(
        id: String,
        title: String,
        body: String?,
        timestamp: String?
    ): Int {
        return if (timestamp != null) {
            noteDao.updateNote(id, title, body, timestamp)
        } else {
            noteDao.updateNote(id, title, body, dateUtil.getCurrentTimestamp())
        }

    }

    override suspend fun searchNotes(): List<Note> {
        return cacheMapper.entityListToNoteList(noteDao.searchNotes())
    }

    override suspend fun searchNotesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return cacheMapper.entityListToNoteList(
            noteDao.searchNotesOrderByDateDESC(
                query,
                page,
                pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return cacheMapper.entityListToNoteList(
            noteDao.searchNotesOrderByDateASC(
                query,
                page,
                pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return cacheMapper.entityListToNoteList(
            noteDao.searchNotesOrderByTitleDESC(
                query,
                page,
                pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return cacheMapper.entityListToNoteList(
            noteDao.searchNotesOrderByTitleASC(
                query,
                page,
                pageSize
            )
        )
    }

    override suspend fun searchNoteById(id: String): Note? {
        return noteDao.searchNoteById(id)?.let {
            cacheMapper.mapFromEntity(it)
        }
    }

    override suspend fun getNumNotes(): Int {
        return noteDao.getNumNotes()
    }

    override suspend fun getAllNotes(): List<Note> {
        return cacheMapper.entityListToNoteList(noteDao.searchNotes())
    }

    override suspend fun insertNotes(notes: List<Note>): LongArray {
        return noteDao.insertNotes(cacheMapper.noteListToEntityList(notes))
    }

    override suspend fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Note> {
        return cacheMapper.entityListToNoteList(
            noteDao.returnOrderedQuery(
                query,
                filterAndOrder,
                page
            )
        )
    }
}