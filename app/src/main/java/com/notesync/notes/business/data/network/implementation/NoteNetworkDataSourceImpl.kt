package com.notesync.notes.business.data.network.implementation

import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.framework.dataSource.network.abstraction.NoteFirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteNetworkDataSourceImpl @Inject constructor(private val firestoreService: NoteFirestoreService) :
    NoteNetworkDataSource {
    override suspend fun insertOrUpdateNote(note: Note) {
        return firestoreService.insertOrUpdateNote(note)
    }

    override suspend fun deleteNote(primaryKey: String) {
        return firestoreService.deleteNote(primaryKey)
    }

    override suspend fun insertDeletedNote(note: Note) {
        return firestoreService.insertDeletedNote(note)
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        return firestoreService.insertDeletedNotes(notes)
    }

    override suspend fun getAllNotes(): List<Note> {
        return firestoreService.getAllNotes()
    }

    override suspend fun deleteDeletedNote(note: Note) {
        return firestoreService.deleteDeletedNote(note)
    }

    override suspend fun getDeletedNotes(): List<Note> {

        return firestoreService.getDeletedNotes()
    }

    override suspend fun deleteAllNotes() {
        return firestoreService.deleteAllNotes()
    }

    override suspend fun searchNote(note: Note): Note? {
        return firestoreService.searchNote(note)
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>) {
        return firestoreService.insertOrUpdateNotes(notes)
    }
}