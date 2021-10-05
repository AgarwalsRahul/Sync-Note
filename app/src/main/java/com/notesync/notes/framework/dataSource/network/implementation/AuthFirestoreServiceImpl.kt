package com.notesync.notes.framework.dataSource.network.implementation

import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.installations.FirebaseInstallations
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.dataSource.network.abstraction.AuthFirestoreService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthFirestoreServiceImpl @Inject constructor(private val firebaseAuth: FirebaseAuth) :
    AuthFirestoreService {
    override suspend fun login(email: String, password: String):User? {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        return firebaseUser?.let {
            User(it.email!!,it.uid)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let {
            User(it.email!!, it.uid)
        }
    }

    override suspend fun register(email: String, password: String):User? {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        return firebaseUser?.let {
            User(it.email!!,it.uid)
        }
    }

    override suspend fun forgotPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email,ActionCodeSettings.zzb()).await()
    }

    override suspend fun logOut() {
        firebaseAuth.signOut()
    }

    override suspend fun getFirebaseInstallationId(): String {
        return FirebaseInstallations.getInstance().id.await()
    }
}