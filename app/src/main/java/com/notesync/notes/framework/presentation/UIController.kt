package com.notesync.notes.framework.presentation

import androidx.annotation.LayoutRes
import com.notesync.notes.business.domain.state.DialogInputCaptureCallback
import com.notesync.notes.business.domain.state.Response
import com.notesync.notes.business.domain.state.StateMessageCallback

interface UIController {

    fun displayProgressBar(isDisplayed: Boolean)

    fun hideSoftKeyboard()

    fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback)

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )



}