package com.notesync.notes.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

const val USER_BUNDLE_KEY = "com.notesync.notes.business.domain.model.USER"
@Parcelize
data class User(
    var email: String,
    var id: String,
    var deviceId: String? = null,
    var sk: String? = null
) : Parcelable