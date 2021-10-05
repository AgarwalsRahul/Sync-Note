package com.notesync.notes.framework.dataSource.cache.implementation

import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.framework.dataSource.cache.abstraction.NoteDaoService
import com.notesync.notes.framework.dataSource.cache.mappers.CacheMapper
import com.notesync.notes.framework.dataSource.cache.database.NoteDao
import com.notesync.notes.framework.dataSource.cache.database.returnOrderedQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDaoServiceImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val cacheMapper: CacheMapper,
    private val dateUtil: DateUtil
) : NoteDaoService {
    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(cacheMapper.mapToEntity(note,null))
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

    override fun searchNotes(): Flow<List<Note>> {
        return noteDao.searchNotes().map {
            cacheMapper.entityListToNoteList(it)
        }
    }

    override  fun searchNotesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): Flow<List<Note>> {
        return noteDao.searchNotesOrderByDateDESC(
            query,
            page,
            pageSize
        ).map {
            cacheMapper.entityListToNoteList(it)
        }

    }

    override  fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int
    ): Flow<List<Note>> {
        return noteDao.searchNotesOrderByDateASC(
            query,
            page,
            pageSize
        ).map {
            cacheMapper.entityListToNoteList(it)
        }
    }

    override  fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): Flow<List<Note>> {
        return noteDao.searchNotesOrderByTitleDESC(
            query,
            page,
            pageSize
        ).map {
            cacheMapper.entityListToNoteList(it)
        }
    }

    override  fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int
    ): Flow<List<Note>> {
        return noteDao.searchNotesOrderByTitleASC(
            query,
            page,
            pageSize
        ).map {
            cacheMapper.entityListToNoteList(it)
        }
    }

    override suspend fun searchNoteById(id: String): Note? {
        return noteDao.searchNoteById(id)?.let {
            cacheMapper.mapFromEntity(it,null)
        }
    }

    override suspend fun getNumNotes(): Int {
        return noteDao.getNumNotes()
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.searchNotes().map {
            cacheMapper.entityListToNoteList(it)
        }
    }

    override suspend fun insertNotes(notes: List<Note>): LongArray {
        return noteDao.insertNotes(cacheMapper.noteListToEntityList(notes))
    }

    override  fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): Flow<List<Note>> {
        return noteDao.returnOrderedQuery(
            query,
            filterAndOrder,
            page
        ).map {
            cacheMapper.entityListToNoteList(it)
        }

    }
}