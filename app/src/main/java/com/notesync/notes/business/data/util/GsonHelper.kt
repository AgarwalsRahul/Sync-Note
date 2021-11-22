package com.notesync.notes.business.data.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.model.User
import java.lang.reflect.Type
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

    fun serializeToNotes(notes:List<Note>):String{
        val listOfTestObject = object : TypeToken<List<Note>>() {}.type
        return Gson().toJson(notes,listOfTestObject)
    }

    fun deserializeToNotes(json:String):List<Note>{
        val listOfTestObject = object : TypeToken<List<Note>>() {}.type
        return Gson().fromJson(json,listOfTestObject)
    }
}