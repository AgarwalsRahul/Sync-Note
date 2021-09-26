package com.notesync.notes.framework.dataSource.network.implementation

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.framework.dataSource.network.abstraction.NoteFirestoreService
import com.notesync.notes.framework.dataSource.network.mappers.NetworkMapper
import com.notesync.notes.framework.dataSource.network.model.NoteNetworkEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteFirestoreServiceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val networkMapper: NetworkMapper
) :
    NoteFirestoreService {
    override suspend fun insertOrUpdateNote(note: Note) {
        val entity = networkMapper.mapToEntity(note)
        entity.updated_at = Timestamp.now()
        firestore.collection(NOTES_COLLECTION).document(USER_ID).collection(NOTES_COLLECTION)
            .document(entity.id).set(entity).await()
    }

    override suspend fun deleteNote(primaryKey: String) {
        firestore.collection(NOTES_COLLECTION).document(USER_ID).collection(NOTES_COLLECTION)
            .document(primaryKey).delete().await()
    }

    override suspend fun insertDeletedNote(note: Note) {
        val entity = networkMapper.mapToEntity(note)
        firestore.collection(DELETES_COLLECTION).document(USER_ID).collection(NOTES_COLLECTION)
            .document(entity.id).set(entity).await()
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time")
        }
        val collectionRef = firestore.collection(DELETES_COLLECTION).document(USER_ID).collection(
            NOTES_COLLECTION
        )
        firestore.runBatch { batch ->
            for (note in notes) {
                val entity = networkMapper.mapToEntity(note)
                val documentRef = collectionRef.document(entity.id)
                batch.set(documentRef, entity)
            }
        }
    }

    override suspend fun getAllNotes(): List<Note> {
        return networkMapper.entityListToNoteList(
            firestore.collection(NOTES_COLLECTION).document(USER_ID).collection(NOTES_COLLECTION)
                .get().await().toObjects(NoteNetworkEntity::class.java)
        )
    }

    override suspend fun deleteDeletedNote(note: Note) {

        firestore.collection(DELETES_COLLECTION).document(USER_ID).collection(
            NOTES_COLLECTION
        ).document(note.id).delete().await()
    }

    override suspend fun deleteAllNotes() {
        firestore.collection(DELETES_COLLECTION).document(USER_ID).delete().await()
        firestore.collection(NOTES_COLLECTION).document(USER_ID).delete().await()
    }

    override suspend fun getDeletedNotes(): List<Note> {
        return networkMapper.entityListToNoteList(
            firestore.collection(DELETES_COLLECTION).document(USER_ID).collection(NOTES_COLLECTION)
                .get().await().toObjects(NoteNetworkEntity::class.java)
        )
    }

    override suspend fun searchNote(note: Note): Note? {
        return firestore.collection(NOTES_COLLECTION).document(USER_ID).collection(NOTES_COLLECTION)
            .document(note.id).get().await().toObject(NoteNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it)
            }
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>) {
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time")
        }
        val collectionRef = firestore.collection(NOTES_COLLECTION).document(USER_ID).collection(
            NOTES_COLLECTION
        )
        firestore.runBatch { batch ->
            for (note in notes) {
                val entity = networkMapper.mapToEntity(note)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef.document(entity.id)
                batch.set(documentRef, entity)
            }
        }
    }

    companion object {
        const val NOTES_COLLECTION = "notes"
        const val USERS_COLLECTION = "users"
        const val DELETES_COLLECTION = "deletes"
        const val USER_ID = "8T1413UUHgWBOWG4tntEtJAuwqy1" // hardcoded for single user
        const val EMAIL = "rahultest@gmail.com"
    }
}