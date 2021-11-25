package com.notesync.notes.framework.dataSource.network.abstraction

import com.notesync.notes.business.domain.model.User

interface AuthFirestoreService {

    suspend fun login(email:String,password:String):User?

    suspend fun getCurrentUser(): User?

    suspend fun register(email: String,password: String):User?

    suspend fun forgotPassword(email: String)

    suspend fun logOut()

    suspend fun getFirebaseInstallationId():String

    suspend fun changePassword(oldPassword:String,newPassword:String)
}