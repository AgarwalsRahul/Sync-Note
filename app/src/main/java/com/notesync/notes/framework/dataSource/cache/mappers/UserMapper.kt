package com.notesync.notes.framework.dataSource.cache.mappers

import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.util.EntityMapper
import com.notesync.notes.framework.dataSource.cache.model.UserCacheEntity
import javax.inject.Singleton

@Singleton
class UserMapper : EntityMapper<UserCacheEntity, User> {
    override fun mapFromEntity(entity: UserCacheEntity,key:String?): User {
        return User(
            email = entity.email,
            id = entity.id
        )
    }

    override fun mapToEntity(domainModel: User,key:String?): UserCacheEntity {
        return UserCacheEntity(id = domainModel.id, email = domainModel.email)
    }
}