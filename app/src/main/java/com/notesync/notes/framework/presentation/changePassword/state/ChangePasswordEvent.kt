package com.notesync.notes.framework.presentation.changePassword.state

import com.notesync.notes.business.domain.state.StateEvent

class ChangePasswordEvent(var oldPassword:String,var newPassword:String):StateEvent {
    override fun errorInfo(): String {
        return "Error in changing password."
    }

    override fun eventName()="ChangePasswordEvent"

    override fun shouldDisplayProgressBar(): Boolean {
        return true
    }
}