package com.notesync.notes.business.domain.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateUtil @Inject constructor(private val dateFormat: SimpleDateFormat) {
    // Date Format: "2021-09-21 HH:mm:ss"


    fun removeTimeFromDateString(dateTime:String) : String{
        return dateTime.substring(0,dateTime.indexOf(" "))
    }

    fun covertFirebaseTimeStampToStringDate(timestamp:Timestamp):String{
        return dateFormat.format(timestamp.toDate())
    }

    fun convertStringDateToFirebaseTimestamp(date:String):Timestamp{
        return Timestamp(dateFormat.parse(date)!!)
    }

    fun getCurrentTimestamp():String{
        return dateFormat.format(Date())
    }
}