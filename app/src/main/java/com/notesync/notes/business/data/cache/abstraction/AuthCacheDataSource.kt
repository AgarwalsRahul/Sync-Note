package com.notesync.notes.business.data.cache.abstraction

import com.notesync.notes.business.domain.model.User

interface AuthCacheDataSource {

    suspend fun setUser(user:User):Long

    suspend fun retrieveUser(email:String):User

    suspend fun deleteUser(id:String):Int
}