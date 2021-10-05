package com.notesync.notes.framework.dataSource.network.implementation

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.DocumentChange.Type.*
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.dataSource.network.abstraction.NoteFirestoreService
import com.notesync.notes.framework.dataSource.network.mappers.NetworkMapper
import com.notesync.notes.framework.dataSource.network.model.NoteNetworkEntity
import com.notesync.notes.util.cLog
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
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
    override suspend fun insertOrUpdateNote(note: Note, user: User) {
        val entity = networkMapper.mapToEntity(note,user.sk)
        entity.updated_at = Timestamp.now()
        firestore.collection(USERS_COLLECTION).document(user.id).collection(NOTES_COLLECTION)
            .document(entity.id).set(entity).await()
    }

    override suspend fun deleteNote(primaryKey: String, user: User) {
        firestore.collection(USERS_COLLECTION).document(user.id).collection(NOTES_COLLECTION)
            .document(primaryKey).delete().await()
    }

    override suspend fun insertDeletedNote(note: Note, user: User) {
        val entity = networkMapper.mapToEntity(note,user.sk)
        firestore.collection(USERS_COLLECTION).document(user.id).collection(DELETES_COLLECTION)
            .document(entity.id).set(entity).await()
    }

    override suspend fun insertDeletedNotes(notes: List<Note>, user: User) {
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time")
        }
        val collectionRef = firestore.collection(USERS_COLLECTION).document(user.id).collection(
            DELETES_COLLECTION
        )
        firestore.runBatch { batch ->
            for (note in notes) {
                val entity = networkMapper.mapToEntity(note, user.sk!!)
                val documentRef = collectionRef.document(entity.id)
                batch.set(documentRef, entity)
            }
        }
    }

    override suspend fun getAllNotes(user: User): List<Note> {
        return networkMapper.entityListToNoteList(
            firestore.collection(USERS_COLLECTION).document(user.id).collection(NOTES_COLLECTION)
                .get().await().toObjects(NoteNetworkEntity::class.java), user.sk!!
        )
    }

    override suspend fun deleteDeletedNote(note: Note, user: User) {

        firestore.collection(USERS_COLLECTION).document(user.id).collection(
            DELETES_COLLECTION
        ).document(note.id).delete().await()
    }

    override suspend fun deleteAllNotes(user: User) {
        firestore.collection(USERS_COLLECTION).document(user.id).delete().await()
    }

    override suspend fun getDeletedNotes(user: User): List<Note> {
        return networkMapper.entityListToNoteList(
            firestore.collection(USERS_COLLECTION).document(user.id).collection(DELETES_COLLECTION)
                .get().await().toObjects(NoteNetworkEntity::class.java), user.sk!!
        )
    }

    override suspend fun searchNote(note: Note, user: User): Note? {
        return firestore.collection(USERS_COLLECTION).document(user.id).collection(NOTES_COLLECTION)
            .document(note.id).get().await().toObject(NoteNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it,user.sk)
            }
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>, user: User) {
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time")
        }
        val collectionRef = firestore.collection(USERS_COLLECTION).document(user.id).collection(
            NOTES_COLLECTION
        )
        firestore.runBatch { batch ->
            for (note in notes) {
                val entity = networkMapper.mapToEntity(note,user.sk)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef.document(entity.id)
                batch.set(documentRef, entity)
            }
        }
    }

    override suspend fun insertUpdatedOrNewNote(note: Note, user: User) {
        val devicesCollection = firestore.collection(USERS_COLLECTION).document(user.id).collection(
            DEVICES_COLLECTION
        ).get().await()
        val deviceIds = devicesCollection.documents.filter {
            it.id != user.deviceId
        }.map {
            it.id
        }
//        printLogD("FirestoreService", "Devices are ${deviceIds.get(0)}")
        val entity = networkMapper.mapToEntity(note, user.sk)
        firestore.runBatch { batch ->
            for (device in deviceIds) {
                val documentRef =
                    firestore.collection(USERS_COLLECTION).document(user.id).collection(
                        UPDATES_COLLECTION
                    ).document(device).collection(NOTES_COLLECTION).document(entity.id)
                batch.set(documentRef, entity)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun getRealtimeUpdatedNotes(user: User): Flow<Pair<Note, Boolean>?> {
        return callbackFlow {
//            val response = MutableStateFlow<Pair<Note, Boolean>?>(null)


            val documentRef = firestore.collection(USERS_COLLECTION).document(user.id)
                .collection(UPDATES_COLLECTION)
                .document(user.deviceId!!).collection(NOTES_COLLECTION)
            val listener = documentRef.addSnapshotListener { value, error ->
                if (error != null) {
                    cLog(error.message)
                    printLogD("FirestoreService", error.message!!)
                    return@addSnapshotListener
                }

                if (value != null && value.documentChanges.isNotEmpty()) {
                    for (dc in value.documentChanges) {
                        when (dc.type) {
                            ADDED -> {
                                val entity = dc.document.toObject(NoteNetworkEntity::class.java)
                                val note = networkMapper.mapFromEntity(entity, user.sk)
                                trySend(Pair(note, false))
                            }
                            MODIFIED -> {
                                val entity = dc.document.toObject(NoteNetworkEntity::class.java)
                                val note = networkMapper.mapFromEntity(entity, user.sk)
//                                    response.update {
//                                        Pair(note, true)
//                                    }
                                trySend(Pair(note, true))
                            }
                            else -> {
                            }
                        }


                    }
                }

            }
            awaitClose {
                printLogD("GetRealtimeUpdateChanges", "Removing the listener.")
                listener.remove()
            }
//            response.collect { note ->
//
//                printLogD("getRealtimeUpdatedNotes", "${note?.first?.title}")
//                emit(note)
//
//            }

        }
    }

    override suspend fun deleteUpdatedNote(user: User, note: Note) {
        firestore.collection(USERS_COLLECTION).document(user.id).collection(UPDATES_COLLECTION)
            .document(user.deviceId!!).collection(NOTES_COLLECTION).document(note.id).delete()
            .await()
    }

    override suspend fun deleteUpdatedNoteFromOtherDevices(user: User, note: Note) {
        val devicesCollection = firestore.collection(USERS_COLLECTION).document(user.id).collection(
            DEVICES_COLLECTION
        ).get().await()
        val deviceIds = devicesCollection.documents.filter {
            it.id != user.deviceId
        }.map {
            it.id
        }
        val entity = networkMapper.mapToEntity(note, user.sk)
        firestore.runBatch { batch ->
            for (device in deviceIds) {
                val documentRef =
                    firestore.collection(USERS_COLLECTION).document(user.id).collection(
                        UPDATES_COLLECTION
                    ).document(device).collection(NOTES_COLLECTION)
                        .document(entity.id)
                batch.delete(documentRef)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun getDeletedNoteChanges(user: User): Flow<Note> {
        return callbackFlow {
//            val result = MutableStateFlow<Note?>(null)

            printLogD("getDeletedNoteChanges", "Looking for changes")

            val documentRef = firestore.collection(USERS_COLLECTION).document(user.id)
                .collection(DELETES_COLLECTION)
            val listener = documentRef.addSnapshotListener { value, error ->
                if (error != null) {

                    cLog(error.message)
                    printLogD("FirestoreService", "${error.message}")

                    return@addSnapshotListener
                }
                printLogD("getDeletedNoteChanges", "${value?.documentChanges}")
                if (value != null && value.documentChanges.isNotEmpty()) {
                    for (dc in value.documentChanges) {
                        when (dc.type) {

                            ADDED -> {
                                val entity = dc.document.toObject(NoteNetworkEntity::class.java)
                                val note = networkMapper.mapFromEntity(entity, user.sk)
//                                    result.update {
//                                        note
//                                    }
                                trySend(note)
                            }
                            MODIFIED -> {
                                val entity = dc.document.toObject(NoteNetworkEntity::class.java)
                                val note = networkMapper.mapFromEntity(entity, user.sk)
                                trySend(note)
//                                    result.update {
//                                        note
//                                    }
                            }
                            else -> {
                            }

                        }
                    }
                }
            }

            awaitClose {
                printLogD("GetDeleteNoteChanges", "listener is removed")
                listener.remove()
//            result.collect { note ->
//                note?.let {
//                    printLogD("DELETE CHANGES", note.id)
//                    emit(it)
//                }
            }
        }
    }


    companion object {
        const val NOTES_COLLECTION = "notes"
        const val UPDATES_COLLECTION = "updates"
        const val USERS_COLLECTION = "users"
        const val DELETES_COLLECTION = "deletes"

        const val DEVICES_COLLECTION = "devices"


    }
}