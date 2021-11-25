package com.notesync.notes.business.data.cache.implementation

import com.notesync.notes.business.data.cache.abstraction.AuthCacheDataSource
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.dataSource.cache.abstraction.AuthDaoService
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthCacheDataSourceImpl @Inject constructor(private val authDaoService: AuthDaoService) :
    AuthCacheDataSource {
    override suspend fun setUser(user: User): Long {
       return authDaoService.setUser(user)
    }

    override suspend fun retrieveUser(email:String): User {
       return authDaoService.retrieveUser(email)?: User.userNotFound()
    }

    override suspend fun deleteUser(id: String): Int {
        return authDaoService.deleteUser(id)
    }
}