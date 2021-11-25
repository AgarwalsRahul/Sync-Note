package com.notesync.notes.business.interactors.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi

class AuthInteractors @ExperimentalCoroutinesApi constructor(
    val login: Login,
    val register: Register,
    val forgotPassword: ForgotPassword,
    val checkAuthenticatedUser: CheckAuthenticatedUser,
    val changePassword: ChangePassword
) {
}