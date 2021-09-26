package com.notesync.notes.framework.dataSource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notesync.notes.framework.dataSource.cache.model.NoteCacheEntity


@Database(entities = [NoteCacheEntity::class ], version = 1, exportSchema = false)
abstract class NoteDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object{
        const val DATABASE_NAME: String = "note_db"
    }


}