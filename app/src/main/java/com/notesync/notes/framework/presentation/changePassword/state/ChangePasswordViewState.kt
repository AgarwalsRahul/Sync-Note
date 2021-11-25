package com.notesync.notes.framework.presentation.changePassword.state

import android.os.Parcelable
import com.notesync.notes.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangePasswordViewState(
    var oldPassword:String?=null,
    var newPassword:String?=null
):Parcelable, ViewState