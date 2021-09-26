package com.notesync.notes.framework.datasource.cache

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.di.TestAppComponent
import com.notesync.notes.framework.BaseTest
import com.notesync.notes.framework.dataSource.cache.abstraction.NoteDaoService
import com.notesync.notes.framework.dataSource.cache.database.NoteDao
import com.notesync.notes.framework.dataSource.cache.implementation.NoteDaoServiceImpl
import com.notesync.notes.framework.dataSource.cache.mappers.CacheMapper
import com.notesync.notes.framework.datasource.data.NoteDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.test.*
import kotlin.random.Random

/*
    LEGEND:
    1. CBS = "Confirm by searching"
    Test cases:
    1. confirm database not  empty to start (should be test data inserted from CacheTest.kt)
    2. insert a new note, CBS
    3. insert a list of notes, CBS
    4. insert 1000 new notes, confirm filtered search query works correctly
    5. insert 1000 new notes, confirm db size increased
    6. delete new note, confirm deleted
    7. delete list of notes, CBS
    8. update a note, confirm updated
    9. search notes, order by date (ASC), confirm order
    10. search notes, order by date (DESC), confirm order
    11. search notes, order by title (ASC), confirm order
    12. search notes, order by title (DESC), confirm order
 */
@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4ClassRunner::class)
class NoteDaoServiceTest : BaseTest() {

    // System in test
    private val noteDaoService: NoteDaoService

    // dependencies
    @Inject
    lateinit var noteDao: NoteDao

    @Inject
    lateinit var cacheMapper: CacheMapper

    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    @Inject
    lateinit var dateUtil: DateUtil

    init {
        injectTest()
        insertTestData()
        noteDaoService = NoteDaoServiceImpl(noteDao, cacheMapper, dateUtil)
    }

    private fun insertTestData() = runBlocking {
        val entityList = cacheMapper.noteListToEntityList(
            noteDataFactory.produceListOfNotes()
        )
        noteDao.insertNotes(entityList)
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

    /**
     * This test runs first. Check to make sure the test data was inserted from
     * CacheTest class.
     */
    @Test
    fun a_searchNotes_confirmDbNotEmpty() = runBlocking {

        val numNotes = noteDaoService.getNumNotes()

        assertTrue { numNotes > 0 }

    }

    @Test
    fun insertNote_CBS() = runBlocking {

        val newNote = noteDataFactory.createSingleNote(
            null,
            "Super cool title",
            "Some content for the note"
        )
        noteDaoService.insertNote(newNote)

        val notes = noteDaoService.searchNotes()
        assert(notes.contains(newNote))
    }

    @Test
    fun insertNoteList_CBS() = runBlocking {

        val noteList = noteDataFactory.createNoteList(10)
        noteDaoService.insertNotes(noteList)

        val queriedNotes = noteDaoService.searchNotes()

        assertTrue { queriedNotes.containsAll(noteList) }
    }

    @Test
    fun insert1000Notes_confirmNumNotesInDb() = runBlocking {
        val currentNumNotes = noteDaoService.getNumNotes()

        // insert 1000 notes
        val noteList = noteDataFactory.createNoteList(1000)
        noteDaoService.insertNotes(noteList)

        val numNotes = noteDaoService.getNumNotes()
        assertEquals(currentNumNotes + 1000, numNotes)
    }

    @Test
    fun insert1000Notes_searchNotesByTitle_confirm50ExpectedValues() = runBlocking {

        // insert 1000 notes
        val noteList = noteDataFactory.createNoteList(1000)
        noteDaoService.insertNotes(noteList)

        // query 50 notes by specific title
        repeat(50) {
            val randomIndex = Random.nextInt(0, noteList.size - 1)
            val result = noteDaoService.searchNotesOrderByTitleASC(
                query = noteList.get(randomIndex).title,
                page = 1,
                pageSize = 1
            )
            assertEquals(noteList.get(randomIndex).title, result.get(0).title)
        }
    }


    @Test
    fun insertNote_deleteNote_confirmDeleted() = runBlocking {
        val newNote = noteDataFactory.createSingleNote(
            null,
            "Super cool title",
            "Some content for the note"
        )
        noteDaoService.insertNote(newNote)

        var notes = noteDaoService.searchNotes()
        assert(notes.contains(newNote))

        noteDaoService.deleteNote(newNote.id)
        notes = noteDaoService.searchNotes()
        assert(!notes.contains(newNote))
    }

    @Test
    fun deleteNoteList_confirmDeleted() = runBlocking {
        val noteList: ArrayList<Note> = ArrayList(noteDaoService.searchNotes())

        // select some random notes for deleting
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

        noteDaoService.deleteNotes(notesToDelete)

        // confirm they were deleted
        val searchResults = noteDaoService.searchNotes()
        assertFalse { searchResults.containsAll(notesToDelete) }
    }

    @Test
    fun insertNote_updateNote_confirmUpdated() = runBlocking {
        val newNote = noteDataFactory.createSingleNote(
            null,
            "Super cool title",
            "Some content for the note"
        )
        noteDaoService.insertNote(newNote)

        println("NewNoteId: ${newNote.updated_at}")

        val newTitle = UUID.randomUUID().toString()
        val newBody = UUID.randomUUID().toString()
        delay(1000)
        noteDaoService.updateNote(
            id = newNote.id,
            title = newTitle,
            body = newBody,
            timestamp = null
        )

        val notes = noteDaoService.searchNotes()

        var foundNote = false
        for (note in notes) {
            if (note.id.equals(newNote.id)) {
                foundNote = true
                println("UpdatedNoteId: ${note.updated_at}")
                assertEquals(newNote.id, note.id)
                assertEquals(newTitle, note.title)
                assertEquals(newBody, note.body)
                assert(newNote.updated_at != note.updated_at)
                assertEquals(
                    newNote.created_at,
                    note.created_at
                )
                break
            }
        }
        assertTrue { foundNote }
    }

    @Test
    fun searchNotes_orderByDateASC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previousNoteDate = noteList.get(0).updated_at
        for (index in 1..noteList.size - 1) {
            val currentNoteDate = noteList.get(index).updated_at
            assertTrue { currentNoteDate >= previousNoteDate }
            previousNoteDate = currentNoteDate
        }
    }


    @Test
    fun searchNotes_orderByDateDESC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateDESC(
            query = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList.get(0).updated_at
        for (index in 1..noteList.size - 1) {
            val current = noteList.get(index).updated_at
            assertTrue { current <= previous }
            previous = current
        }
    }

    @Test
    fun searchNotes_orderByTitleASC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByTitleASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList.get(0).title
        for (index in 1..noteList.size - 1) {
            val current = noteList.get(index).title

            assertTrue {
                listOf(previous, current)
                    .asSequence()
                    .zipWithNext { a, b ->
                        a <= b
                    }.all { it }
            }
            previous = current
        }
    }

    @Test
    fun searchNotes_orderByTitleDESC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByTitleDESC(
            query = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList.get(0).title
        for (index in 1..noteList.size - 1) {
            val current = noteList.get(index).title

            assertTrue {
                listOf(previous, current)
                    .asSequence()
                    .zipWithNext { a, b ->
                        a >= b
                    }.all { it }
            }
            previous = current
        }
    }

}