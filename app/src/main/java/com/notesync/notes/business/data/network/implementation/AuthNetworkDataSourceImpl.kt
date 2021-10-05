package com.notesync.notes.business.data.network.implementation

import com.notesync.notes.business.data.network.abstraction.AuthNetworkDataSource
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.dataSource.network.abstraction.AuthFirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthNetworkDataSourceImpl @Inject constructor(private val authFirestoreService: AuthFirestoreService) :
    AuthNetworkDataSource {
    override suspend fun login(email: String, password: String):User? {
        return authFirestoreService.login(email, password)
    }

    override suspend fun getCurrentUser(): User? {
        return authFirestoreService.getCurrentUser()
    }

    override suspend fun register(email: String, password: String):User? {
        return authFirestoreService.register(email, password)
    }

    override suspend fun forgotPassword(email: String) {
        return authFirestoreService.forgotPassword(email)
    }

    override suspend fun logOut() {
        authFirestoreService.logOut()
    }

    override suspend fun getFirebaseInstallationId(): String {
       return  authFirestoreService.getFirebaseInstallationId()
    }
}