package com.notesync.notes.util


/**
 *@author
 *https://betterprogramming.pub/how-to-monitor-internet-connection-in-android-using-kotlin-and-livedata-135de9447796
 */
sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
}