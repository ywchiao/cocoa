package com.uxitech.cocoaandroid.view

import android.app.Activity
import android.content.Context
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.view.ViewGroup

import com.uxitech.cocoaandroid.R
import com.uxitech.cocoaandroid.controller.DragListener
import com.uxitech.cocoaandroid.controller.ItemTouchHandler
import com.uxitech.cocoaandroid.model.Brick
import com.uxitech.cocoaandroid.model.BrickNone
import com.uxitech.cocoaandroid.util.UIFactory.BaseAdapter


/**
 * Created by kuanyu on 2016/12/15.
 */

object EditSpaceView {
    val adapter by lazy {
        val tmp = BrickNone(R.drawable.octocat1_80)
        MyAdapter(mutableListOf(tmp))
    }

    private var _isFirstTime = true


    fun init(view: View, activity: Activity) {
       val editSpace = view.findViewById(R.id.edit_space)
                        as RecyclerView

        // Init layout manager
        val layoutManager = createLayoutManager(activity)
        editSpace.layoutManager = layoutManager

        // Add dividers to EditSpace
        val dividerItemDecoration = DividerItemDecoration(
            editSpace.context,
            layoutManager.orientation
        )
        editSpace.addItemDecoration(dividerItemDecoration)

        // Init adapter
        editSpace.adapter = EditSpaceView.adapter

        // Optimize RecyclerView
        editSpace.setHasFixedSize(true)

        // Handle Drag and Drop
        val dragListener = DragListener(activity)
        editSpace.setOnDragListener(dragListener)

        // Handle touch event of item
        val handleItemTouch = ItemTouchHandler(adapter)
        val touchHelper = ItemTouchHelper(handleItemTouch)
        touchHelper.attachToRecyclerView(editSpace)
    }

    private fun createLayoutManager(ctx: Context): GridLayoutManager {
        val MAX_ROW_ITEMS = 8
        val layoutManager = GridLayoutManager(
            ctx,
            MAX_ROW_ITEMS,
            GridLayoutManager.VERTICAL,
            false
        )

        return layoutManager
    }

    /*
     *  Passing function as parameter with form `ClassName::FunctionName`
     *  only valid on Kotlin 1.1, so currently, I moved createViewHolder()
     *  out off the EditSpaceView object.
     *
     */
    class MyAdapter(bricks: MutableList<Brick>)
        : BaseAdapter<Brick>(bricks, ::createViewHolder) {

        init {
            if (_isFirstTime) {
                clearItems()
                _isFirstTime = false
            }
        }
    }  // End MyAdapter
} // End EditSpaceView

private fun createViewHolder(parent: ViewGroup,
                             viewType: Int): BrickViewHolder {
    val viewHolder = BrickViewHolder(parent.context, null)
    return viewHolder
}

