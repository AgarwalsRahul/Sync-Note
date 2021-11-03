package com.notesync.notes.framework.presentation.notelist

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View

import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi


@ObsoleteCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class RecyclerViewEmptySupport : RecyclerView {
    private var emptyView: View? = null

    private var viewModel: NoteListViewModel? = null
    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {

        init {
            Log.d("RecyclerView", "${viewModel?.getSearchQuery()}")
        }

        override fun onChanged() {
            super.onChanged()
            initEmptyView()
        }


        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            initEmptyView()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            initEmptyView()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            initEmptyView()
        }
    }

    private fun initEmptyView() {
        if (emptyView != null && viewModel != null) {
            Log.d("RecyclerView", viewModel!!.getSearchQuery())

            this@RecyclerViewEmptySupport.visibility =
                if ((adapter == null || adapter!!.itemCount == 0) && viewModel!!.getSearchQuery()
                    == "" && viewModel!!.getNoteListSize() == 0 && viewModel!!.viewState.value?.numNotesInCache == 0
                ) GONE else VISIBLE
            emptyView!!.visibility =
                if ((adapter == null || adapter!!.itemCount == 0) && viewModel!!.getSearchQuery()
                    == "" && viewModel!!.getNoteListSize() == 0 && viewModel!!.viewState.value?.numNotesInCache == 0
                ) VISIBLE else GONE
        }
    }


    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    override fun setAdapter(adapter: Adapter<*>?) {

        getAdapter()?.unregisterAdapterDataObserver(emptyObserver)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
//        emptyObserver.onChanged()
    }

    fun setEmptyView(emptyView: View?) {
        this.emptyView = emptyView
        initEmptyView()
    }

    fun setViewModel(viewModel: NoteListViewModel) {
        this.viewModel = viewModel
    }
}