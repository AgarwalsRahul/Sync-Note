package com.notesync.notes.framework.presentation.notedetail

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.material.appbar.AppBarLayout
import com.notesync.notes.R
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.interactors.common.DeleteNote.Companion.DELETE_SUCCESS
import com.notesync.notes.business.interactors.noteDetail.UpdateNote.*
import com.notesync.notes.business.interactors.noteDetail.UpdateNote.Companion.UPDATE_NOTE_FAILED_PK
import com.notesync.notes.business.interactors.noteDetail.UpdateNote.Companion.UPDATE_NOTE_SUCCESS
import com.notesync.notes.framework.presentation.common.*
import com.notesync.notes.framework.presentation.notedetail.state.CollapsingToolbarState
import com.notesync.notes.framework.presentation.notedetail.state.CollapsingToolbarState.*
import com.notesync.notes.framework.presentation.notedetail.state.NoteDetailStateEvent.*
import com.notesync.notes.framework.presentation.notedetail.state.NoteDetailViewState
import com.notesync.notes.framework.presentation.notedetail.state.NoteInteractionState
import com.notesync.notes.framework.presentation.notedetail.state.NoteInteractionState.*
import com.notesync.notes.framework.presentation.notelist.NOTE_PENDING_DELETE_BUNDLE_KEY
import com.yydcdut.markdown.MarkdownProcessor
import com.yydcdut.markdown.syntax.edit.EditFactory
import kotlinx.android.synthetic.main.fragment_note_detail.*
import kotlinx.android.synthetic.main.layout_note_detail_toolbar.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi


const val NOTE_DETAIL_STATE_BUNDLE_KEY =
    "com.notesync.notes.framework.presentation.notedetail.state"

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class NoteDetailFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseNoteFragment(R.layout.fragment_note_detail) {


    val viewModel: NoteDetailViewModel by viewModels {
        viewModelFactory
    }

    lateinit var markdownProcessor: MarkdownProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        restoreInstanceState(savedInstanceState)
        setupUI()
        setupOnBackPressDispatcher()

        subscribeObservers()


        note_title.setOnClickListener {
            onClick_noteTitle()
        }

        note_body.setOnClickListener {
            onClick_noteBody()
        }

        setupMarkdown()
        getSelectedNoteFromPreviousFragment(savedInstanceState)

    }

    private fun onErrorRetrievingNoteFromPreviousFragment() {
        viewModel.setStateEvent(
            CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = NOTE_DETAIL_ERROR_RETRIEVEING_SELECTED_NOTE,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    )
                )
            )
        )
    }

    private fun setupMarkdown() {
        activity?.run {
            markdownProcessor = MarkdownProcessor(this)
            markdownProcessor.factory(EditFactory.create())
            note_body?.let {
                markdownProcessor.live(it)
            }
        }
    }

    private fun onClick_noteTitle() {
        if (!viewModel.isEditingTitle()) {
            updateBodyInViewModel()
            updateNote()
            viewModel.setNoteInteractionTitleState(EditState())
        }
    }

    private fun onClick_noteBody() {
        if (!viewModel.isEditingBody()) {
            updateTitleInViewModel()
            updateNote()
            viewModel.setNoteInteractionBodyState(EditState())
        }
    }

    private fun onBackPressed() {
        view?.hideKeyboard()
        if (viewModel.checkEditState()) {
            updateBodyInViewModel()
            updateTitleInViewModel()
            updateNote()
            viewModel.exitEditState()
            displayDefaultToolbar()
        } else {
            findNavController(this).popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        updateTitleInViewModel()
        updateBodyInViewModel()
        updateNote()
    }


    private fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->

            if (viewState != null) {

                viewState.note?.let { note ->
                    Log.d("NoteDetailFragment", "Set Body ${note.body}")
                    setNoteTitle(note.title)
                    setNoteBody(note.body)
                }
            }
        })

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            stateMessage?.response?.let { response ->

                when (response.message) {

                    UPDATE_NOTE_SUCCESS -> {
                        viewModel.setIsUpdatePending(false)
                        viewModel.clearStateMessage()
                    }

                    DELETE_SUCCESS -> {
                        viewModel.clearStateMessage()
                        onDeleteSuccess()
                    }

                    else -> {
                        uiController.onResponseReceived(
                            response = stateMessage.response,
                            stateMessageCallback = object : StateMessageCallback {
                                override fun removeMessageFromStack() {
                                    viewModel.clearStateMessage()
                                }
                            }
                        )
                        when (response.message) {

                            UPDATE_NOTE_FAILED_PK -> {
                                findNavController(this).popBackStack()
                            }

                            NOTE_DETAIL_ERROR_RETRIEVEING_SELECTED_NOTE -> {
                                findNavController(this).popBackStack()
                            }

                            else -> {
                                // do nothing
                            }
                        }
                    }
                }
            }

        })

        viewModel.collapsingToolbarState.observe(viewLifecycleOwner, Observer { state ->

            when (state) {

                is Expanded -> {
                    transitionToExpandedMode()
                }

                is Collapsed -> {
                    transitionToCollapsedMode()
                }
            }
        })

        viewModel.noteTitleInteractionState.observe(viewLifecycleOwner, Observer { state ->

            when (state) {

                is EditState -> {
                    note_title.enableContentInteraction()
                    view?.showKeyboard()
                    displayEditStateToolbar()
                    viewModel.setIsUpdatePending(true)
                }

                is DefaultState -> {
                    note_title.disableContentInteraction()
                }
            }
        })

        viewModel.noteBodyInteractionState.observe(viewLifecycleOwner, Observer { state ->

            when (state) {

                is EditState -> {
                    note_body.enableContentInteraction()
                    view?.showKeyboard()
                    displayEditStateToolbar()
                    viewModel.setIsUpdatePending(true)
                }

                is DefaultState -> {
                    note_body.disableContentInteraction()
                }
            }
        })
    }

    private fun displayDefaultToolbar() {
        activity?.let { a ->
            toolbar_primary_icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_arrow_back_grey_24dp,
                    a.application.theme
                )
            )
            toolbar_secondary_icon.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, a.application.theme)
            )
        }
    }

    private fun displayEditStateToolbar() {
        activity?.let { a ->
            toolbar_primary_icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_close_grey_24dp,
                    a.application.theme
                )
            )
            toolbar_secondary_icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.check_mark_update,
                    a.application.theme
                )
            )
        }
    }

    private fun setNoteTitle(title: String) {
        note_title.setText(title)
    }

    private fun getNoteTitle(): String {
        return note_title.text.toString()
    }

    private fun getNoteBody(): String {
        Log.d("NoteDetailFragment", note_body.text.toString())
        return note_body.text.toString()
    }

    private fun setNoteBody(body: String?) {
        note_body.setText(body)
    }

    private fun getSelectedNoteFromPreviousFragment(savedInstanceState: Bundle?) {
        arguments?.let { args ->
            (args.getParcelable(NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY) as Note?)?.let { selectedNote ->
                if (!restoreInstanceState(savedInstanceState)) {
                    viewModel.setNote(selectedNote)
                }

            } ?: onErrorRetrievingNoteFromPreviousFragment()
        }

    }

    private fun restoreInstanceState(savedInstanceState: Bundle?): Boolean {
        savedInstanceState?.let { instate ->
            (instate[NOTE_DETAIL_STATE_BUNDLE_KEY] as NoteDetailViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
                Log.d("NoteDetailFragment", "Restore")
                Log.d("NoteDetailFragmentRestore", viewState.note?.body ?: "null")
                // One-time check after rotation
                if (viewModel.isToolbarCollapsed()) {
                    app_bar.setExpanded(false)
                    transitionToCollapsedMode()
                } else {
                    app_bar.setExpanded(true)
                    transitionToExpandedMode()
                }
                return true
            }
            return false
        }
        return false
    }

    private fun updateTitleInViewModel() {
        if (viewModel.isEditingTitle()) {
            viewModel.updateNoteTitle(getNoteTitle())
        }
    }

    private fun updateBodyInViewModel() {
        if (viewModel.isEditingBody()) {
            Log.d("NoteDetailFragment", "Update Body")
            viewModel.updateNoteBody(getNoteBody())
        }
    }

    private fun setupUI() {
        note_title.disableContentInteraction()
        note_body.disableContentInteraction()
        displayDefaultToolbar()
        transitionToExpandedMode()

        app_bar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, offset ->

                if (offset < COLLAPSING_TOOLBAR_VISIBILITY_THRESHOLD) {
                    updateTitleInViewModel()
                    if (viewModel.isEditingTitle()) {
                        viewModel.exitEditState()
                        displayDefaultToolbar()
                        updateNote()
                    }
                    viewModel.setCollapsingToolbarState(Collapsed())
                } else {
                    viewModel.setCollapsingToolbarState(Expanded())
                }
            })

        toolbar_primary_icon.setOnClickListener {
            if (viewModel.checkEditState()) {
                view?.hideKeyboard()
                viewModel.triggerNoteObservers()
                viewModel.exitEditState()
                displayDefaultToolbar()
            } else {
                onBackPressed()
            }
        }

        toolbar_secondary_icon.setOnClickListener {
            if (viewModel.checkEditState()) {
                view?.hideKeyboard()
                updateTitleInViewModel()
                updateBodyInViewModel()
                updateNote()
                viewModel.exitEditState()
                displayDefaultToolbar()
            } else {
                deleteNote()
            }
        }
    }

    private fun deleteNote() {
        viewModel.setStateEvent(
            CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = "Are you sure to Delete",
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object : AreYouSureCallback {
                                override fun proceed() {
                                    viewModel.getNote()?.let { note ->
                                        initiateDeleteTransaction(note)
                                    }
                                }

                                override fun cancel() {
                                    // do nothing
                                }
                            }
                        ),
                        messageType = MessageType.Info()
                    )
                )
            )
        )
    }

    private fun initiateDeleteTransaction(note: Note) {
        viewModel.beginPendingDelete(note)
    }

    private fun onDeleteSuccess() {
        val bundle = bundleOf(NOTE_PENDING_DELETE_BUNDLE_KEY to viewModel.getNote())
        viewModel.setNote(null) // clear note from ViewState
        viewModel.setIsUpdatePending(false) // prevent update onPause
        findNavController(this).navigate(
            R.id.action_note_detail_fragment_to_noteListFragment,
            bundle
        )
    }

    private fun setupOnBackPressDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }


    private fun updateNote() {
        if (viewModel.getIsUpdatePending()) {
            viewModel.setStateEvent(
                UpdateNoteEvent()
            )
        }
    }

    private fun transitionToCollapsedMode() {
        note_title.fadeOut()
        displayToolbarTitle(tool_bar_title, getNoteTitle(), true)
    }

    private fun transitionToExpandedMode() {
        note_title.fadeIn()
        displayToolbarTitle(tool_bar_title, null, true)
    }

    override fun inject() {

    }


    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.getCurrentViewStateOrNew()
        Log.d("NoteDetailFragmentViewState", viewState.note?.body ?: "null")
        outState.putParcelable(NOTE_DETAIL_STATE_BUNDLE_KEY, viewState)
        super.onSaveInstanceState(outState)
    }


}