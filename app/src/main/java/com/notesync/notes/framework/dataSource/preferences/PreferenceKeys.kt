package com.notesync.notes.framework.dataSource.preferences

class PreferenceKeys {

    companion object {

        // Shared Preference Files:
        const val NOTE_PREFERENCES: String = "com.notesync.notes"

        const val USER_PREFERENCES: String = "com.notesync.user"

        const val DEVICE_PREFERENCES: String = "com.notesync.deviceID"

        const val ENCRYPT_DECRYPT_PREFERENCES: String = "com.notesync.secretKey"

        // Shared Preference Keys
        val NOTE_FILTER: String = "${NOTE_PREFERENCES}.NOTE_FILTER"
        val NOTE_ORDER: String = "${NOTE_PREFERENCES}.NOTE_ORDER"

    }
}
