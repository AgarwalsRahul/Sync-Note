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

    fun convertStringDateToFirebaseTimestamp(date: String): Timestamp {
        return Timestamp(dateFormat.parse(date)!!)
    }

    fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }

    fun getMonth(date: String): String {
        return when(date.substring(5, 7)){
            "01"->"Jan"
            "02"->"Feb"
            "03"->"Mar"
            "04"->"Apr"
            "05"->"May"
            "06"->"Jun"
            "07"->"Jul"
            "08"->"Aug"
            "09"->"Sep"
            "10"->"Oct"
            "11"->"Nov"
            else ->"Dec"
        }
    }

    fun getYear(date: String): String {
        return date.substring(0,4)
    }

    fun getDay(date: String): String {
        return date.substring(8, 10)
    }

    fun removeDateFromTimestamp(timeStamp: String): String {
        val time = timeStamp.substring(timeStamp.indexOf(" "))
        return time.substring(0,6) + " "+    time.substring(10).uppercase()
    }

    fun getEditedDateTime(timestamp: String): String {
        return if (removeTimeFromDateString(timestamp) == removeTimeFromDateString(getCurrentTimestamp())) {
            removeDateFromTimestamp(timestamp)
        } else if (getYear(getCurrentTimestamp()) != getYear(
                timestamp
            )
        ) {
            "${getMonth(timestamp)} ${getYear(removeTimeFromDateString(timestamp))}"
        } else {
            "${getMonth(timestamp)} ${getDay(timestamp)}"
        }
    }
}