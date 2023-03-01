package com.notesync.notes.business.domain.util

sealed class Validators<out T> {

    data class Success<T>(val data: T) : Validators<T>()

    sealed class ValueFailure<T>(val data: T) : Validators<T>() {
        data class InvalidEmail<T>(val msg: T) : ValueFailure<T>(msg)
        data class InvalidPassword<T>(val msg: T) : ValueFailure<T>(msg)
    }
}


fun validateEmailAddress(input: String): Validators<String> {
    val regex = Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")
    if (input.matches(regex)) {
        return Validators.Success(input)
    } else if (input.isEmpty()) {
        return Validators.ValueFailure.InvalidEmail("Email Address is Required")
    }
    return Validators.ValueFailure.InvalidEmail("Email Address is invalid.")
}

fun validatePassword(input: String): Validators<String> {
    val regex = Regex("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{6,}$")
    if (input.matches(regex)) {
        return Validators.Success(input)
    } else if (input.isEmpty()) {
        return Validators.ValueFailure.InvalidPassword("Password is Required")
    }
    return Validators.ValueFailure.InvalidPassword("Password does not match the constraints.")
}