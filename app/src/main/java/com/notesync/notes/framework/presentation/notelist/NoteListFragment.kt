package com.notesync.notes.framework.presentation.notelist

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.notesync.notes.R
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.business.interactors.common.DeleteNote.Companion.DELETE_SUCCESS
import com.notesync.notes.business.interactors.noteList.DeleteMultipleNotes.*
import com.notesync.notes.business.interactors.noteList.DeleteMultipleNotes.Companion.DELETE_NOTES_ARE_YOU_SURE
import com.notesync.notes.business.interactors.noteList.SearchNotes
import com.notesync.notes.framework.dataSource.cache.database.NOTE_FILTER_DATE_CREATED
import com.notesync.notes.framework.dataSource.cache.database.NOTE_FILTER_TITLE
import com.notesync.notes.framework.dataSource.cache.database.NOTE_ORDER_ASC
import com.notesync.notes.framework.dataSource.cache.database.NOTE_ORDER_DESC
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.framework.presentation.common.*
import com.notesync.notes.framework.presentation.notedetail.NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY
import com.notesync.notes.framework.presentation.notelist.state.NoteListStateEvent.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListToolbarState.*
import com.notesync.notes.framework.presentation.notelist.state.NoteListViewState
import com.notesync.notes.util.Constants.DARK_THEME
import com.notesync.notes.util.Constants.LIGHT_THEME
import com.notesync.notes.util.NetworkConnection
import com.notesync.notes.util.TodoCallback
import com.notesync.notes.util.printLogD
import kotlinx.android.synthetic.main.fragment.*
import kotlinx.android.synthetic.main.layout_searchview_toolbar.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

const val NOTE_LIST_STATE_BUNDLE_KEY = "com.notesync.notes.framework.presentation.notelist.state"

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class NoteListFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
) : BaseNoteFragment(R.layout.fragment),
    NoteListAdapter.Interaction,
    ItemTouchHelperAdapter {

    val viewModel: NoteListViewModel by viewModels {
        viewModelFactory
    }


    private var listAdapter: NoteListAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var dialog: MaterialDialog? = null


    @Inject
    lateinit var themeManager: ThemeManager


    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()
        viewModel.retrieveNumNotesInCache()
        arguments?.let { args ->
            args.getParcelable<Note>(NOTE_PENDING_DELETE_BUNDLE_KEY)?.let { note ->
                viewModel.setNotePendingDelete(note)
                showUndoSnackbarDeleteNote()
                clearArgs()
            }
        }

//

    }

    private fun clearArgs() {
        arguments?.clear()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        note_list_fragment_container.setOnClickListener {
            view.hideKeyboard()
            search_view?.clearFocus()
        }
        restoreInstanceState(savedInstanceState)
        setupUI()

        setupRecyclerView()
        setupSwipeRefresh()
        setupFAB()
        subscribeObservers()
        night_mode.setOnClickListener {
            themeManager.setTheme()
        }


    }


    @SuppressLint("NotifyDataSetChanged")
    private fun subscribeObservers() {

        viewModel.toolbarState.observe(viewLifecycleOwner, { toolbarState ->

            when (toolbarState) {

                is MultiSelectionState -> {
                    enableMultiSelectToolbarState()
                    disableSearchViewToolbarState()
                }

                is SearchViewState -> {
                    enableSearchViewToolbarState()
                    disableMultiSelectToolbarState()
                }
            }
        })



        themeManager.themeMode.observe(viewLifecycleOwner, {
            it?.let { value ->
                setNightThemeImageColor(value)
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
                    if (noteList.isEmpty() && viewModel.getSearchQuery().isNotEmpty()) {

                        search_image.visible()
                        search_result_textView.visible()
                    } else {
                        search_image.gone()
                        search_result_textView.gone()


                    }


                }

//                // a note been inserted or selected
                viewState.newNote?.let { newNote ->
                    navigateToDetailFragment(newNote)
                }

            }
        })

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, {
            printActiveJobs()
            uiController.displayProgressBar(it)
        })



        viewModel.stateMessage.observe(viewLifecycleOwner, { stateMessage ->
            stateMessage?.let { message ->
                when {
                    message.response.message?.equals(DELETE_SUCCESS) == true -> {
                        showUndoSnackbarDeleteNote()
                    }

                    message.response.message?.equals(SearchNotes.NO_NOTES_IN_CACHE) == true -> {
                        Log.d("NoteListFragment", "No Notes in Cache Get from network")
                        viewModel.clearStateMessage()
                        val observer = NetworkConnection(requireContext())
                        observer.observe(viewLifecycleOwner, {
                            it?.let { isConnected ->
                                if (isConnected) {
                                    viewModel.setStateEvent(GetAllNotesFromNetwork())
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


    private fun setNightThemeImageColor(themeMode: Int) {
        when (themeMode) {
            DARK_THEME -> {
                night_mode.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimary
                    )
                )
            }
            LIGHT_THEME -> {
                night_mode.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary_text_color
                    )
                )
            }
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { inState ->
            (inState[NOTE_LIST_STATE_BUNDLE_KEY] as NoteListViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
        if (viewModel.getFilterDialogShowing())
            showFilterDialog()
    }


    private fun setupRecyclerView() {
        recycler_view.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

            addItemDecoration(SpacesItemDecoration(15))
//            ( layoutManager as StaggeredGridLayoutManager).gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            itemTouchHelper = ItemTouchHelper(
                NoteItemTouchHelperCallback(
                    this@NoteListFragment,
                    viewModel.noteListInteractionManager
                )
            )
            listAdapter = NoteListAdapter(
                this@NoteListFragment,
                viewLifecycleOwner,
                viewModel.noteListInteractionManager.selectedNotes,
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
            setViewModel(viewModel)
            setEmptyView(emptyView)
            setAdapter(listAdapter)

        }
    }

    private fun setupSearchView() {

        val searchViewToolbar: Toolbar? = toolbar_content_container
            .findViewById(R.id.searchview_toolbar)

        searchViewToolbar?.let { toolbar ->

            val searchView = toolbar.findViewById<SearchView>(R.id.search_view)

            CoroutineScope(Main).launch {
                searchView.getQueryTextChangeStateFlow()
                    .debounce(300)
                    .distinctUntilChanged()

                    .flowOn(Dispatchers.Default).collect {
                        Log.d("NoteListFragment", searchView.query.toString())
                        viewModel.setQuery(it)
                        startNewSearch()
                    }
            }

//
//            val searchPlate: SearchView.SearchAutoComplete? =
//                searchView.findViewById(androidx.appcompat.R.id.search_src_text)
//
//            searchPlate.s
//
//            searchPlate?.setOnEditorActionListener { v, actionId, _ ->
//                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
//                    || actionId == EditorInfo.IME_ACTION_SEARCH
//                ) {
//                    val searchQuery = v.text.toString()
//                    viewModel.setQuery(searchQuery)
//                    startNewSearch()
//                }
//                true
//            }
        }
    }

    private fun startNewSearch() {
//        viewModel.clearList()
        viewModel.loadFirstPage()
    }

    private fun showNoInternetConnectionSnackBar() {
        uiController.onResponseReceived(
            response = Response(
                message = "No internet connection.",
                uiComponentType = UIComponentType.SnackBar(),
                messageType = MessageType.Info()
            ),
            stateMessageCallback = object : StateMessageCallback {
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    private fun setupSwipeRefresh() {
        swipe_refresh.setOnRefreshListener {
            startNewSearch()
            swipe_refresh.isRefreshing = false
        }
    }

    private fun setupFAB() {
        add_new_note_fab.setOnClickListener {
            uiController.displayInputCaptureDialog(
                getString(R.string.text_enter_a_title),
                object : DialogInputCaptureCallback {
                    override fun onTextCaptured(text: String) {
                        val newNote = viewModel.createNewNote(title = text)
                        viewModel.setStateEvent(
                            InsertNewNoteEvent(
                                title = newNote.title
                            )
                        )
                    }
                }
            )
            printLogD("NoteListFragment", "FAB IS CLICKED")

//            viewModel.setStateEvent(
//                InsertNewNoteEvent(
//                    title = UUID.randomUUID().toString().substring(0, 10)
//                )
//            )

        }
    }

    private fun showFilterDialog() {

        activity?.let {
            dialog = MaterialDialog(it).cornerRadius(16.0f)
                .noAutoDismiss()
                .customView(R.layout.layout_filter)

            val view = dialog?.getCustomView()

            val filter = viewModel.getFilter()
            val order = viewModel.getOrder()

            view?.findViewById<RadioGroup>(R.id.filter_group)?.apply {
                when (filter) {
                    NOTE_FILTER_DATE_CREATED -> check(R.id.filter_date)
                    NOTE_FILTER_TITLE -> check(R.id.filter_title)
                }
            }

            view?.findViewById<RadioGroup>(R.id.order_group)?.apply {
                when (order) {
                    NOTE_ORDER_ASC -> check(R.id.filter_asc)
                    NOTE_ORDER_DESC -> check(R.id.filter_desc)
                }
            }

            view?.findViewById<TextView>(R.id.positive_button)?.setOnClickListener {

                val newFilter =
                    when (view?.findViewById<RadioGroup>(R.id.filter_group)?.checkedRadioButtonId) {
                        R.id.filter_title -> NOTE_FILTER_TITLE
                        R.id.filter_date -> NOTE_FILTER_DATE_CREATED
                        else -> NOTE_FILTER_DATE_CREATED
                    }

                val newOrder =
                    when (view?.findViewById<RadioGroup>(R.id.order_group)?.checkedRadioButtonId) {
                        R.id.filter_desc -> "-"
                        else -> ""
                    }

                viewModel.apply {
                    saveFilterOptions(newFilter, newOrder)
                    setNoteFilter(newFilter)
                    setNoteOrder(newOrder)
                }

                startNewSearch()
                dialog?.dismiss()
            }

            view?.findViewById<TextView>(R.id.negative_button)?.setOnClickListener {
                dialog?.dismiss()
            }

            dialog?.show()
        }
    }


    private fun setupFilterButton() {
        val searchViewToolbar: Toolbar? = toolbar_content_container
            .findViewById(R.id.searchview_toolbar)
        searchViewToolbar?.findViewById<ImageView>(R.id.action_filter)?.setOnClickListener {
            showFilterDialog()
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
                "NoteList",
                "${index}: $job"
            )
        }
    }

    private fun navigateToDetailFragment(selectedNote: Note) {
        val bundle = bundleOf(NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY to selectedNote)
        findNavController(this).navigate(
            R.id.action_note_list_fragment_to_noteDetailFragment,
            bundle
        )
        viewModel.setNote(null)
    }

    private fun setupUI() {
        view?.hideKeyboard()

    }

    private fun saveLayoutManagerState() {
        recycler_view.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }


    private fun enableMultiSelectToolbarState() {
        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_multiselection_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            toolbar_content_container.addView(view)
            setupMultiSelectionToolbar(view)
        }
    }

    private fun setupMultiSelectionToolbar(parentView: View) {
        parentView
            .findViewById<ImageView>(R.id.action_exit_multiselect_state)
            .setOnClickListener {
                viewModel.setToolbarState(SearchViewState())
            }

        parentView
            .findViewById<ImageView>(R.id.action_delete_notes)
            .setOnClickListener {
                deleteNotes()
            }
    }


    private fun enableSearchViewToolbarState() {
        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_searchview_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            toolbar_content_container.addView(view)
            setupSearchView()
            setupFilterButton()
        }
    }

    private fun disableMultiSelectToolbarState() {
        view?.let {
            val view = toolbar_content_container
                .findViewById<Toolbar>(R.id.multiselect_toolbar)
            toolbar_content_container.removeView(view)
            viewModel.clearSelectedNotes()
        }
    }

    private fun disableSearchViewToolbarState() {
        view?.let {
            val view = toolbar_content_container
                .findViewById<Toolbar>(R.id.searchview_toolbar)
            toolbar_content_container.removeView(view)
        }
    }

    override fun inject() {
        (activity?.application as BaseApplication).appComponent
            .inject(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveNumNotesInCache()
//        viewModel.clearList()

        viewModel.refreshSearchQuery()

    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
        viewModel.setFilterDialogShowing(dialog?.isShowing ?: false)
        dialog?.dismiss()
        dialog?.dismiss()
    }

    // Why didn't I use the "SavedStateHandle" here?
// It sucks and doesn't work for testing
    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value

        //clear the list. Don't want to save a large list to bundle.
        viewState?.noteList = ArrayList()

        outState.putParcelable(
            NOTE_LIST_STATE_BUNDLE_KEY,
            viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun restoreListPosition() {
        viewModel.getLayoutManagerState()?.let { lmState ->
            recycler_view?.layoutManager?.onRestoreInstanceState(lmState)
        }
    }

    override fun isMultiSelectionModeEnabled() = viewModel.isMultiSelectionStateActive()

    override fun activateMultiSelectionMode() = viewModel.setToolbarState(MultiSelectionState())

    override fun onItemSelected(position: Int, item: Note) {
        if (isMultiSelectionModeEnabled()) {
            viewModel.addOrRemoveNoteFromSelectedList(item)
        } else {
            viewModel.setNote(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null // can leak memory
        itemTouchHelper = null // can leak memory
    }

    override fun isNoteSelected(note: Note): Boolean {
        return viewModel.isNoteSelected(note)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemSwiped(position: Int) {
        if (!viewModel.isDeletePending()) {
            listAdapter?.getNote(position)?.let { note ->
                viewModel.beginPendingDelete(note)
            }
        } else {
            listAdapter?.notifyDataSetChanged()
        }
    }

    private fun deleteNotes() {
        viewModel.setStateEvent(
            CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = DELETE_NOTES_ARE_YOU_SURE,
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


}