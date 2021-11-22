package com.notesync.notes.framework.presentation.trash

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.navigation.NavigationView
import com.notesync.notes.R
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.business.interactors.common.DeleteNote
import com.notesync.notes.business.interactors.noteList.DeleteMultipleNotes
import com.notesync.notes.business.interactors.trash.EmptyTrash
import com.notesync.notes.business.interactors.trash.GetTrashNotes
import com.notesync.notes.framework.presentation.MainActivity
import com.notesync.notes.framework.presentation.common.BaseNoteFragment
import com.notesync.notes.framework.presentation.common.gone
import com.notesync.notes.framework.presentation.common.hideKeyboard
import com.notesync.notes.framework.presentation.common.visible
import com.notesync.notes.framework.presentation.notelist.SpacesItemDecoration
import com.notesync.notes.framework.presentation.trash.state.TrashStateEvent
import com.notesync.notes.framework.presentation.trash.state.TrashToolbarState
import com.notesync.notes.framework.presentation.trash.state.TrashViewState
import com.notesync.notes.util.NetworkConnection
import com.notesync.notes.util.TodoCallback
import com.notesync.notes.util.printLogD
import kotlinx.android.synthetic.main.fragment.*
import kotlinx.android.synthetic.main.fragment_trash.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi


const val TRASH_STATE_BUNDLE_KEY = "com.notesync.notes.framework.presentation.trash.state"

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class TrashFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
) : BaseNoteFragment(R.layout.fragment_trash), TrashListAdapter.Interaction,
    ItemTouchHelperAdapter {

    val viewModel: TrashViewModel by viewModels {
        viewModelFactory
    }

    private var listAdapter: TrashListAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    lateinit var mainActivity: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel.setupChannel()
        restoreInstanceState(savedInstanceState)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupRecyclerView()
        setupSwipeRefresh()
        subscribeObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.trash_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.empty_trash->{
               emptyTrash()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearArgs() {
        arguments?.clear()
    }

    private fun emptyTrash(){
        viewModel.setStateEvent(
            TrashStateEvent.CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = EmptyTrash.EMPTY_TRASH_ARE_YOU_SURE,
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object : AreYouSureCallback {
                                override fun proceed() {
                                    viewModel.emptyTrash()
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

    private fun setupToolbar() {
        val mainActivity = mainActivity as MainActivity
        val navigationView: NavigationView = mainActivity.findViewById(R.id.nvView)

        val toolbar = trash_toolbar_content_container
            .findViewById<Toolbar>(R.id.trash_toolbar)

        toolbar?.let {
            mainActivity.setSupportActionBar(toolbar)
            val navController = NavHostFragment.findNavController(this)
            val appBarConfiguration = mainActivity.appBarConfiguration
            NavigationUI.setupActionBarWithNavController(
                mainActivity,
                navController,
                appBarConfiguration
            )

            navigationView.menu[1].isChecked = true


        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun subscribeObservers() {

        viewModel.toolbarState.observe(viewLifecycleOwner, { toolbarState ->

            when (toolbarState) {

                is TrashToolbarState.MultiSelectionState -> {
                    enableMultiSelectToolbarState()
                    disableDefaultToolbarState()
                }

                is TrashToolbarState.DefaultState -> {

                    enableDefaultToolbar()
                    setupToolbar()
                    disableMultiSelectToolbarState()
                }
            }

        })

        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->

            if (viewState != null) {
                viewState.noteList?.let { noteList ->
                    if (viewModel.isPaginationExhausted()
                        && !viewModel.isQueryExhausted()
                    ) {
                        viewModel.setQueryExhausted(true)
                    }

                    listAdapter?.submitList(noteList)

                    listAdapter?.notifyDataSetChanged()
                    if (noteList.isEmpty()) {
                        emptyTrash.visible()
                        recycler_view_trash.gone()

                    } else {
                        emptyTrash.gone()
                        recycler_view_trash.visible()
                    }
                }

//                // a note been inserted or selected
//                viewState.newNote?.let { newNote ->
//                    navigateToDetailFragment(newNote)
//                }

            }
        })

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, {
            printActiveJobs()
            uiController.displayProgressBar(it)
        })



        viewModel.stateMessage.observe(viewLifecycleOwner, { stateMessage ->
            stateMessage?.let { message ->
                when {
                    message.response.message?.equals(DeleteNote.DELETE_SUCCESS) == true -> {
                        showUndoSnackbarDeleteNote()
                    }

                    message.response.message?.equals(GetTrashNotes.NO_NOTES_IN_TRASH) == true -> {
                        Log.d("TrashFragment", "No Notes in Cache Get from network")
                        viewModel.clearStateMessage()
                        val observer = NetworkConnection(requireContext())
                        observer.observe(viewLifecycleOwner, {
                            it?.let { isConnected ->
                                if (isConnected) {
                                    viewModel.setStateEvent(TrashStateEvent.GetAllTrashNotesFromNetwork())
                                    observer.removeObservers(viewLifecycleOwner)
                                }
                            }
                        })
                    }
                    else -> {
                        uiController.onResponseReceived(
                            response = message.response,
                            stateMessageCallback = object : StateMessageCallback {
                                override fun removeMessageFromStack() {
                                    viewModel.clearStateMessage()
                                }
                            }
                        )
                    }
                }
            }
        })
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { inState ->
            (inState[TRASH_STATE_BUNDLE_KEY] as TrashViewState?)?.let { viewState ->

                viewModel.setViewState(viewState)
            }
        }
    }

    private fun setupRecyclerView() {
        recycler_view_trash.apply {

            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

            addItemDecoration(SpacesItemDecoration(15))
//            ( layoutManager as StaggeredGridLayoutManager).gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            itemTouchHelper = ItemTouchHelper(
                TrashItemTouchHelperCallback(
                    this@TrashFragment,
                    viewModel.trashInteractionManager
                )
            )
            listAdapter = TrashListAdapter(
                this@TrashFragment,
                viewLifecycleOwner,
                viewModel.trashInteractionManager.selectedNotes,
                dateUtil,
            )
            itemTouchHelper?.attachToRecyclerView(this)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    var firstVisibleItemPosition = 0
                    val firstVisibleItemPositions =
                        layoutManager.findFirstVisibleItemPositions(null)
                    firstVisibleItemPosition = firstVisibleItemPositions[0]
//                    if (lastPosition[-1] == listAdapter?.itemCount?.minus(1)) {
//                        viewModel.nextPage()
//                    }
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        viewModel.nextPage()
                    }
                }
            })
            adapter = listAdapter
        }
    }

    private fun startNewSearch() {
        viewModel.loadFirstPage()
    }

    private fun setupSwipeRefresh() {
        swipe_refresh_trash.setOnRefreshListener {
            startNewSearch()
            swipe_refresh_trash.isRefreshing = false
        }
    }

    private fun showUndoSnackbarDeleteNote() {
        uiController.onResponseReceived(
            response = Response(
                message = "Delete Pending...",
                uiComponentType = UIComponentType.SnackBar(
                    undoCallback = object : SnackbarUndoCallback {
                        override fun undo() {
                            viewModel.undoDelete()
                        }
                    },
                    onDismissCallback = object : TodoCallback {
                        override fun execute() {
                            // if the note is not restored, clear pending note
                            viewModel.setNotePendingDelete(null)
                        }
                    }
                ),
                messageType = MessageType.Info()
            ),
            stateMessageCallback = object : StateMessageCallback {
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    // for debugging
    private fun printActiveJobs() {

        for ((index, job) in viewModel.getActiveJobs().withIndex()) {
            printLogD(
                "TrashFragment",
                "${index}: $job"
            )
        }
    }

    private fun setupUI() {
        view?.hideKeyboard()

    }

    private fun enableMultiSelectToolbarState() {
        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_multiselection_trash_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setupStatusBar()
            trash_toolbar_content_container.addView(view)
            setupMultiSelectionToolbar(view)
        }
    }



    private fun setupMultiSelectionToolbar(parentView: View) {
        parentView
            .findViewById<ImageView>(R.id.action_exit_multiselect_state)
            .setOnClickListener {
                viewModel.setToolbarState(TrashToolbarState.DefaultState())
            }

        parentView
            .findViewById<ImageView>(R.id.action_delete_notes)
            .setOnClickListener {
                deleteNotes()
            }
    }

    private fun deleteNotes() {
        viewModel.setStateEvent(
            TrashStateEvent.CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = DeleteMultipleNotes.DELETE_NOTES_ARE_YOU_SURE,
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object : AreYouSureCallback {
                                override fun proceed() {
                                    viewModel.deleteNotes()
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


    private fun enableDefaultToolbar() {
        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_trash_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            trash_toolbar_content_container.addView(view)
        }
    }

    private fun disableMultiSelectToolbarState() {
        view?.let {
            val view = trash_toolbar_content_container
                .findViewById<Toolbar>(R.id.multiselect_trash_toolbar)
            clearStatusbar()
            trash_toolbar_content_container.removeView(view)
            viewModel.clearSelectedNotes()
        }
    }

    private fun disableDefaultToolbarState() {
        view?.let {
            val view = trash_toolbar_content_container
                .findViewById<Toolbar>(R.id.trash_toolbar)
            trash_toolbar_content_container.removeView(view)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveNumNotesInCache()
        viewModel.refreshSearchQuery()

    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    private fun saveLayoutManagerState() {
        recycler_view_trash.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }


    // Why didn't I use the "SavedStateHandle" here?
// It sucks and doesn't work for testing
    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value

        //clear the list. Don't want to save a large list to bundle.
        viewState?.noteList = ArrayList()

        outState.putParcelable(
            TRASH_STATE_BUNDLE_KEY,
            viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun inject() {

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mainActivity = it }
    }

    override fun onItemSwiped(position: Int) {
        if (!viewModel.isDeletePending()) {
            listAdapter?.getNote(position)?.let { note ->
                viewModel.beginPendingDelete(note)
            }
        } else {
            listAdapter?.notifyDataSetChanged()
        }
    }

    override fun onItemSelected(position: Int, item: Note) {
        if (isMultiSelectionModeEnabled()) {
            viewModel.addOrRemoveNoteFromSelectedList(item)
        } else {
            viewModel.setNote(item)
        }
    }

    override fun restoreListPosition() {
        viewModel.getLayoutManagerState()?.let { lmState ->
            Log.d("restoreListPosition", "state")
            recycler_view_trash?.layoutManager?.onRestoreInstanceState(lmState)
        }
    }

    override fun isMultiSelectionModeEnabled(): Boolean {
        return viewModel.isMultiSelectionStateActive()
    }

    override fun activateMultiSelectionMode() {
        viewModel.setToolbarState(TrashToolbarState.MultiSelectionState())
    }

    override fun isNoteSelected(note: Note): Boolean {
        return viewModel.isNoteSelected(note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null // can leak memory
        itemTouchHelper = null // can leak memory
    }


}