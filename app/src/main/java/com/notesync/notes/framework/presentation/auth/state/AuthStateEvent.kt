package com.notesync.notes.framework.presentation.auth.state

import com.notesync.notes.business.domain.state.StateEvent
import com.notesync.notes.business.domain.state.StateMessage

sealed class AuthStateEvent : StateEvent {
    data class LoginAttemptEvent(
        val email: String,
        val password: String
    ) : AuthStateEvent() {

        override fun errorInfo(): String {
            return "Login attempt failed."
        }

        override fun eventName(): String {
            return "LoginAttemptEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean {
            return true
        }
    }


    data class ForgotPasswordEvent(
        val email: String,

    ) : AuthStateEvent() {

        override fun errorInfo(): String {
            return "Forgot Password is Failed."
        }

        override fun eventName(): String {
            return "ForgotPasswordEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean {
            return true
        }
    }

    data class RegisterAttemptEvent(
        val email: String,
        val password: String
    ) : AuthStateEvent() {

        override fun errorInfo(): String {
            return "Register attempt failed."
        }

        override fun eventName(): String {
            return "RegisterAttemptEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean {
            return true
        }
    }


    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ) : AuthStateEvent() {

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

    class CheckPreviousAuthUser():AuthStateEvent(){
        override fun errorInfo(): String {
           return "Error checking for previously authenticated user."
        }

        override fun eventName(): String {
            return "CheckPreviousAuthUser"
        }

        override fun shouldDisplayProgressBar(): Boolean {
            return false
        }

    }
}