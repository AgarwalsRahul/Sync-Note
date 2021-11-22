package com.notesync.notes.framework.presentation.notelist

import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.view.View
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class SpacesItemDecoration(private val spacing: Int) : ItemDecoration() {



    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val lm = parent.layoutManager as StaggeredGridLayoutManager
        val lp = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

        val spanCount = lm.spanCount
        val spanIndex = lp.spanIndex
        val positionLayout = lp.viewLayoutPosition
        val itemCount = lm.itemCount
        val position = parent.getChildAdapterPosition(view)

        outRect.right = spacing / 2
        outRect.left = spacing / 2
        outRect.top = spacing / 2
        outRect.bottom = spacing / 2

        if (spanIndex == 0) outRect.left = spacing

        if (position < spanCount) outRect.top = spacing

        if (spanIndex == (spanCount - 1)) outRect.right = spacing

        if (positionLayout > (itemCount - spanCount)) outRect.bottom = spacing
    }
}