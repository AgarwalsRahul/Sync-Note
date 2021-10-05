package com.notesync.notes.business.data.util

object FirebaseErrors {

    // Sign In Error Codes
    const val ERROR_INVALID_EMAIL = "Provided email is invalid."
    const val ERROR_WRONG_PASSWORD = "Provided password and email combination are invalid."
    const val ERROR_USER_NOT_FOUND = "User with provided email doesn't exist"
    const val ERROR_USER_DISABLED = "User with this has been disabled."
    const val ERROR_TOO_MANY_REQUESTS = "Too many requests. Try again later."
    const val ERROR_OPERATION_NOT_ALLOWED = "Signing with email and password is not enabled."

    // Register Error Codes

    const val ERROR_OPERATION_NOT_ALLOWED_REGISTER = "Anonymous accounts are not allowed."
    const val ERROR_WEAK_PASSWORD = "Provided password is too weak."
    const val ERROR_EMAIL_ALREADY_IN_USE = "Account with this email already exists."
    const val ERROR_INVALID_CREDENTIAL = "Provided password and email combination are invalid."


    // Firestore Error Codes

    const val ERROR_UNKNOWN = "An unknown error has occurred. Please try again."
    const val ERROR_OBJECT_NOT_FOUND = "An unknown error has occurred. Please try again."
    const val ERROR_PROJECT_NOT_FOUND = "An unknown error has occurred. Please try again."
    const val ERROR_QUOTA_EXCEEDED = "An unknown error has occurred. Please try again."
    const val ERROR_NOT_AUTHENTICATED =
        "User is unauthenticated, please authenticate and try again."
    const val ERROR_NOT_AUTHORIZED = "You are not authorised to perform the desired action. Contact for support"
    const val ERROR_RETRY_LIMIT_EXCEEDED = "The maximum limit for the operation has been exceeded. Please Try Again"
    const val ERROR_CANCELED = "User Canceled the operation"

}