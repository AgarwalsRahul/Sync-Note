package com.notesync.notes.framework.dataSource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.notesync.notes.framework.dataSource.cache.model.NoteCacheEntity
import com.notesync.notes.framework.dataSource.cache.model.TrashNoteCacheEntity
import com.notesync.notes.framework.dataSource.cache.model.UserCacheEntity


@Database(
    entities = [NoteCacheEntity::class, UserCacheEntity::class, TrashNoteCacheEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class NoteDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun authDao():AuthDao

//    abstract fun

    companion object {
        const val DATABASE_NAME: String = "note_db"
        val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `trashNotes` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `updated_at` TEXT NOT NULL, `created_at` TEXT NOT NULL, `device_id` TEXT, PRIMARY KEY(`id`))"
                )
            }
        }
    }


}