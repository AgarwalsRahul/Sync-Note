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
            emptyView!!.setVisibility(
                if ((adapter == null || adapter!!.itemCount == 0) && viewModel!!.getSearchQuery()
                        .isEmpty()
                ) VISIBLE else GONE
            )
            this@RecyclerViewEmptySupport.setVisibility(
                if ((adapter == null || adapter!!.itemCount == 0) && viewModel!!.getSearchQuery()
                        .isEmpty()
                ) GONE else VISIBLE
            )
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