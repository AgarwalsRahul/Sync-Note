package com.notesync.notes.framework.presentation.common

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesync.notes.business.domain.state.DialogInputCaptureCallback
import com.notesync.notes.business.domain.state.SessionManager
import com.notesync.notes.business.interactors.noteList.NoteListInteractors
import com.notesync.notes.business.interactors.splash.SyncDeletedNotes
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@FlowPreview
class MainViewModel(
    private val syncDeletedNotes: SyncDeletedNotes,
    private val noteListInteractors: NoteListInteractors,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _hasSyncBeenExecuted: MutableLiveData<Boolean> = MutableLiveData(false)

    val hasSyncBeenExecuted: LiveData<Boolean>
        get() = _hasSyncBeenExecuted



    private var dialogInputCaptureCallback:DialogInputCaptureCallback?=null



   fun init(){
        printLogD("MainViewModel","getUpdateNotes is launched")
        viewModelScope.launch {
            sessionManager.cachedUser.value?.let {
                syncDeletedNotes.syncDeletedNotes(it)
            }
        }
        viewModelScope.launch {
            sessionManager.cachedUser.value?.let {
                printLogD("MainViewModel","getUpdateNotes is launched")
                noteListInteractors.getUpdatedNotes.getUpdatedNotes(it)
            }
        }
       GlobalScope.launch(Main){
           _hasSyncBeenExecuted.value=true
       }
    }

    fun getDialogInputCaptureCallback() : DialogInputCaptureCallback?{
        return dialogInputCaptureCallback
    }

    fun setDialogInputCaptureCallback(value:DialogInputCaptureCallback){
        dialogInputCaptureCallback = value
    }

}