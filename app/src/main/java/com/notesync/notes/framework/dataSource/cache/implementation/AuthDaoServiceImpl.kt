package com.notesync.notes.framework.dataSource.cache.implementation

import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.dataSource.cache.abstraction.AuthDaoService
import com.notesync.notes.framework.dataSource.cache.database.AuthDao
import com.notesync.notes.framework.dataSource.cache.mappers.UserMapper
import javax.inject.Inject

class AuthDaoServiceImpl @Inject constructor(
    private val authDao: AuthDao,
    private val userMapper: UserMapper
) : AuthDaoService {
    override suspend fun setUser(user: User): Long {
        return authDao.setUser(userMapper.mapToEntity(user,null))
    }

    override suspend fun retrieveUser(email: String): User? {
        return authDao.retrieveUser(email)?.let {
            userMapper.mapFromEntity(it,null)
        }
    }

    override suspend fun deleteUser(id: String): Int {
        return authDao.deleteUser(id)
    }
}