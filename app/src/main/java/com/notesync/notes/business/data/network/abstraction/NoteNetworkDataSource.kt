package com.notesync.notes.business.data.network.abstraction

import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import kotlinx.coroutines.flow.Flow

interface NoteNetworkDataSource {

    suspend fun insertOrUpdateNote(note: Note, user: User)

    suspend fun deleteNote(primaryKey: String, user: User)

    suspend fun insertDeletedNote(note: Note, user: User)

    suspend fun insertDeletedNotes(notes: List<Note>, user: User)

    suspend fun getAllNotes(user: User): List<Note>

    suspend fun deleteDeletedNote(note: Note, user: User)

    suspend fun getDeletedNotes(user: User): List<Note>

    suspend fun deleteAllNotes(user: User)

    suspend fun searchNote(note: Note, user: User): Note?

    suspend fun insertOrUpdateNotes(notes: List<Note>, user: User)

    suspend fun insertUpdatedOrNewNote(note: Note, user: User)

    fun getRealtimeUpdatedNotes(user: User): Flow<Pair<Note, Boolean>?>

    suspend fun deleteUpdatedNote(user: User, note: Note)

    suspend fun deleteUpdatedNoteFromOtherDevices(user: User, note: Note)

    fun getDeletedNoteChanges(user: User): Flow<Note>
}