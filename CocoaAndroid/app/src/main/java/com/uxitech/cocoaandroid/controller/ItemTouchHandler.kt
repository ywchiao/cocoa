package com.uxitech.cocoaandroid.controller

import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.RecyclerView
import com.uxitech.cocoaandroid.view.ItemTouchHelperAdapter


/**
 * Created by kuanyu on 2016/12/15.
 */

class ItemTouchHandler(private val _adapter: ItemTouchHelperAdapter)
    : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(recyclerView: RecyclerView,
                                  viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.START or ItemTouchHelper.END  // or START, END
        val swipeFlags = 0  // disable swipe directions
        return makeMovementFlags(dragFlags, swipeFlags)
    }


    override fun onMove(recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        _adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition())
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        _adapter.onItemDismiss(viewHolder.getAdapterPosition())
    }
}


