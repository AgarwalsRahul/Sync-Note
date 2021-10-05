package com.notesync.notes.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var email: String,
    var id: String,
    var deviceId: String? = null,
    var sk: String? = null
) : Parcelable