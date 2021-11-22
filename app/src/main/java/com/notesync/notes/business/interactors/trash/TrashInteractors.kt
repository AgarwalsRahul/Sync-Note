package com.notesync.notes.business.interactors.trash

import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FlowPreview
class TrashInteractors(
    val deleteMultipleTrashNote: DeleteMultipleTrashNote,
    val deleteTrashNote: DeleteTrashNote<TrashViewState>,
    val getTrashNotes: GetTrashNotes,
    val restoreDeletedTrashNote: RestoreDeletedTrashNote,
    val getTrashNumNotes: GetTrashNumNotes,
    val getTrashNotesFromNetwork: GetTrashNotesFromNetwork,
    val emptyTrash: EmptyTrash
) {
}