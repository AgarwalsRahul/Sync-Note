package com.notesync.notes.framework.dataSource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notesync.notes.framework.dataSource.cache.model.NoteCacheEntity
import com.notesync.notes.framework.dataSource.cache.model.UserCacheEntity


@Database(entities = [NoteCacheEntity::class,UserCacheEntity::class ], version = 1, exportSchema = false)
abstract class NoteDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun authDao():AuthDao

//    abstract fun

    companion object{
        const val DATABASE_NAME: String = "note_db"
    }


}