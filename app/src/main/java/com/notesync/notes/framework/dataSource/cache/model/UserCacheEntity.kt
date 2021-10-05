package com.notesync.notes.framework.dataSource.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserCacheEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="id")
    var id:String,
    @ColumnInfo(name="email")
    var email:String
)