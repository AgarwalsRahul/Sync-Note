package com.notesync.notes.framework.dataSource.cache.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notesync.notes.framework.dataSource.cache.model.UserCacheEntity

@Dao
interface AuthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setUser(user: UserCacheEntity): Long

    @Query("SELECT * FROM user WHERE email =:email")
    suspend fun retrieveUser(email: String): UserCacheEntity?

    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteUser(id: String): Int
}