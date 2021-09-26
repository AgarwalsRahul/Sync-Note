package com.notesync.notes.framework.datasource.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.di.TestAppComponent
import com.notesync.notes.framework.BaseTest
import com.notesync.notes.framework.dataSource.network.abstraction.NoteFirestoreService
import com.notesync.notes.framework.dataSource.network.implementation.NoteFirestoreServiceImpl
import com.notesync.notes.framework.dataSource.network.mappers.NetworkMapper
import com.notesync.notes.framework.datasource.data.NoteDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import java.util.*
import javax.inject.Inject
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@FlowPreview
class NoteNetworkServiceTest : BaseTest() {

    // system in test
    private lateinit var noteFirestoreService: NoteFirestoreService


    override fun injectTest() {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    @Inject
    lateinit var networkMapper: NetworkMapper

    init {
        injectTest()
        signIn()
        insertTestData()
    }

    @Before
    fun before() {
        noteFirestoreService = NoteFirestoreServiceImpl(
            firebaseAuth = FirebaseAuth.getInstance(),
            firestore = firestore,
            networkMapper = networkMapper
        )
    }

    private fun signIn() = runBlocking {
        firebaseAuth.signInWithEmailAndPassword(
            EMAIL,
            PASSWORD
        ).await()
    }

    @Test
    fun a_insertSingleNote_CBS() = runBlocking {
        val note = noteDataFactory.createSingleNote(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        noteFirestoreService.insertOrUpdateNote(note)

        val searchResult = noteFirestoreService.searchNote(note)

        assertEquals(note, searchResult)
    }

    @Test
    fun updateSingleNote_CBS() = runBlocking{

        val searchResults = noteFirestoreService.getAllNotes()

        // choose a random note from list to update
        val randomNote = searchResults.get(Random.nextInt(0,searchResults.size-1) + 1)
        val UPDATED_TITLE = UUID.randomUUID().toString()
        val UPDATED_BODY = UUID.randomUUID().toString()
        var updatedNote = noteDataFactory.createSingleNote(
            id = randomNote.id,
            title = UPDATED_TITLE,
            body = UPDATED_BODY
        )

        // make the update
        noteFirestoreService.insertOrUpdateNote(updatedNote)

        // query the note after update
        updatedNote = noteFirestoreService.searchNote(updatedNote)!!

        assertEquals(UPDATED_TITLE, updatedNote.title)
        assertEquals(UPDATED_BODY, updatedNote.body)
    }

    @Test
    fun insertNoteList_CBS() = runBlocking {
        val list = noteDataFactory.createNoteList(50)

        noteFirestoreService.insertOrUpdateNotes(list)

        val searchResults = noteFirestoreService.getAllNotes()

        assertTrue { searchResults.containsAll(list) }
    }

    @Test
    fun deleteSingleNote_CBS() = runBlocking {
        val noteList = noteFirestoreService.getAllNotes()

        // choose one at random to delete
        val noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)

        noteFirestoreService.deleteNote(noteToDelete.id)

        // confirm it no longer exists in firestore
        val searchResults = noteFirestoreService.getAllNotes()

        assertFalse { searchResults.contains(noteToDelete) }
    }

    @Test
    fun insertIntoDeletesNode_CBS() = runBlocking {
        val noteList = noteFirestoreService.getAllNotes()

        // choose one at random to insert into "deletes" node
        val noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)

        noteFirestoreService.insertDeletedNote(noteToDelete)

        // confirm it is now in the "deletes" node
        val searchResults = noteFirestoreService.getDeletedNotes()

        assertTrue { searchResults.contains(noteToDelete) }
    }

    @Test
    fun insertListIntoDeletesNode_CBS() = runBlocking {
        val noteList = ArrayList(noteFirestoreService.getAllNotes())

        // choose some random notes to add to "deletes" node
        val notesToDelete: ArrayList<Note> = ArrayList()

        // 1st
        var noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 2nd
        noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 3rd
        noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 4th
        noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // insert into "deletes" node
        noteFirestoreService
            .insertDeletedNotes(notesToDelete)

        // confirm the notes are in "deletes" node
        val searchResults = noteFirestoreService.getDeletedNotes()

        assertTrue { searchResults.containsAll(notesToDelete) }
    }

    @Test
    fun deleteDeletedNote_CBS() = runBlocking {
        val note = noteDataFactory.createSingleNote(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        // insert into "deletes" node
        noteFirestoreService.insertDeletedNote(note)

        // confirm note is in "deletes" node
        var searchResults = noteFirestoreService.getDeletedNotes()

        assertTrue { searchResults.contains(note) }

        // delete from "deletes" node
        noteFirestoreService.deleteDeletedNote(note)

        // confirm note is deleted from "deletes" node
        searchResults = noteFirestoreService.getDeletedNotes()

        assertFalse { searchResults.contains(note) }
    }


    private fun insertTestData() {
        val entityList = networkMapper.noteListToEntityList(noteDataFactory.produceListOfNotes())

        for(entity in entityList){
            for(entity in entityList){
                firestore
                    .collection(NoteFirestoreServiceImpl.NOTES_COLLECTION)
                    .document(NoteFirestoreServiceImpl.USER_ID)
                    .collection(NoteFirestoreServiceImpl.NOTES_COLLECTION)
                    .document(entity.id)
                    .set(entity)
            }
        }
    }

    companion object {
        const val EMAIL = "rahultest@gmail.com"
        const val PASSWORD = "password"
    }
}