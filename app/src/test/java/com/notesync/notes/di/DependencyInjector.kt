package com.notesync.notes.di

import com.notesync.notes.buisness.data.NoteDataFactory
import com.notesync.notes.buisness.data.cache.FakeNoteCacheDataSourceImpl
import com.notesync.notes.buisness.data.network.FakeNoteNetworkDataSourceImpl
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DependencyInjector {


    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    val dateUtil =
        DateUtil(dateFormat)
    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory
    lateinit var noteDataFactory: NoteDataFactory

    init {
        isUnitTest = true // for Logger.kt
    }

    // data sets
    lateinit var notesData: HashMap<String, Note>

    fun build() {
        this.javaClass.classLoader?.let { classLoader ->
            noteDataFactory = NoteDataFactory(classLoader)

            // fake data set
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteDataFactory.produceListOfNotes()
            )
        }
        val noteList = noteDataFactory.produceListOfNotes()
        noteFactory = NoteFactory(dateUtil)
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteList
            ),
            deletedNotesData = HashMap(),
            dateUtil = dateUtil
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteList
            ),
            dateUtil = dateUtil
        )
    }
}