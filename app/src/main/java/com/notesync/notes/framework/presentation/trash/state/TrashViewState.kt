package com.notesync.notes.framework.presentation.trash.state


import android.os.Parcelable
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize


@Parcelize
data class TrashViewState(
    var newNote: Note? = null,
    var noteList: ArrayList<Note>? = null,
    var notePendingDelete: NotePendingDelete? = null, // set when delete is pending (can be undone)
    var notePendingRestore: NotePendingRestore? = null, // set when restore is pending (can be undone)
    var page: Int? = null,
    var isQueryExhausted: Boolean? = null,
    var layoutManagerState: Parcelable? = null,
    var numNotesInCache: Int? = null

) : Parcelable, ViewState {

    @Parcelize
    data class NotePendingDelete(
        var note: Note? = null,
        var listPosition: Int? = null
    ) : Parcelable

    @Parcelize
    data class NotePendingRestore(
        var note: Note? = null,
        var listPosition: Int? = null
    ) : Parcelable
}