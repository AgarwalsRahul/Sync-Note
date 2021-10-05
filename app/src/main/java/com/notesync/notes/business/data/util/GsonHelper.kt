package com.notesync.notes.business.data.util

import com.google.gson.Gson
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import kotlin.reflect.KClass

object GsonHelper {

    fun serializeToJson(myClass : Note) : String{
        return Gson().toJson(myClass)
    }

    fun serializeToJson(user: User) : String{
        return Gson().toJson(user)
    }

    fun deserializeToNote(json:String) : Note{
        return Gson().fromJson(json,Note::class.java)
    }

    fun deserializeToUser(json:String):User{
        return Gson().fromJson(json,User::class.java)
    }
}