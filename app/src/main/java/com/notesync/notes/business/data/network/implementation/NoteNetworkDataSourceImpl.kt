package com.notesync.notes.business.data.network.implementation

import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.dataSource.network.abstraction.NoteFirestoreService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteNetworkDataSourceImpl @Inject constructor(private val firestoreService: NoteFirestoreService) :
    NoteNetworkDataSource {
    override suspend fun insertOrUpdateNote(note: Note,user:User) {
        return firestoreService.insertOrUpdateNote(note,user)
    }

    override suspend fun deleteNote(primaryKey: String,user:User) {
        return firestoreService.deleteNote(primaryKey,user)
    }

    override suspend fun insertDeletedNote(note: Note,user:User) {
        return firestoreService.insertDeletedNote(note,user)
    }

    override suspend fun insertDeletedNotes(notes: List<Note>,user:User) {
        return firestoreService.insertDeletedNotes(notes,user)
    }

    override suspend fun getAllNotes(user:User): List<Note> {
        return firestoreService.getAllNotes(user)
    }

    override suspend fun deleteDeletedNote(note: Note,user:User) {
        return firestoreService.deleteDeletedNote(note,user)
    }

    override suspend fun getDeletedNotes(user:User): List<Note> {

        return firestoreService.getDeletedNotes(user)
    }

    override suspend fun deleteAllNotes(user:User) {
        return firestoreService.deleteAllNotes(user)
    }

    override suspend fun searchNote(note: Note,user:User): Note? {
        return firestoreService.searchNote(note,user)
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>,user:User) {
        return firestoreService.insertOrUpdateNotes(notes,user)
    }

    override suspend fun insertUpdatedOrNewNote(note: Note, user: User) {
       return firestoreService.insertUpdatedOrNewNote(note,user)
    }

    override fun getRealtimeUpdatedNotes(user: User): Flow<Pair<Note,Boolean>?> {
        return firestoreService.getRealtimeUpdatedNotes(user)
    }

    override suspend fun deleteUpdatedNote(user: User, note: Note) {
       return firestoreService.deleteUpdatedNote(user,note)
    }
    override suspend fun deleteUpdatedNoteFromOtherDevices(user: User, note: Note){
        return firestoreService.deleteUpdatedNoteFromOtherDevices(user,note)
    }

    override fun getDeletedNoteChanges(user: User): Flow<Pair<Note,Boolean>> {
        return firestoreService.getDeletedNoteChanges(user)
    }

    override suspend fun deleteAllTrashNotes(notes:List<Note>,user: User) {
        return firestoreService.deleteAllTrashNotes(notes,user)
    }
}