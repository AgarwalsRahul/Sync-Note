package com.notesync.notes.business.domain.model

import com.notesync.notes.business.domain.util.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class NoteFactory @Inject constructor(private val dateUtil: DateUtil) {

    fun createSingleNote(id: String? = null, title: String, body: String? = null): Note {
        return Note(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            body = body ?: "",
            created_at = dateUtil.getCurrentTimestamp(),
            updated_at = dateUtil.getCurrentTimestamp()
        )
    }

    //Only for unit Testing purpose
    fun createNoteList(numNotes: Int): List<Note> {
        val list = ArrayList<Note>()
        for (i in 0 until numNotes) {
            list.add(
                createSingleNote(
                    null, UUID.randomUUID().toString(), UUID.randomUUID().toString()
                )
            )
        }
        return list
    }

}