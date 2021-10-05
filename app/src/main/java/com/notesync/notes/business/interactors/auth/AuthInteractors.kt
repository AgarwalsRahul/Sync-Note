package com.notesync.notes.business.interactors.auth

class AuthInteractors(
    val login: Login,
    val register: Register,
    val forgotPassword: ForgotPassword,
    val checkAuthenticatedUser: CheckAuthenticatedUser
) {
}