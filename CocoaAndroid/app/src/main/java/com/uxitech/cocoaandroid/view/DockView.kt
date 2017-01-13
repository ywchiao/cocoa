package com.uxitech.cocoaandroid.view

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import com.uxitech.cocoaandroid.R
import com.uxitech.cocoaandroid.controller.DragListener
import com.uxitech.cocoaandroid.controller.TouchListener
import com.uxitech.cocoaandroid.model.Brick
import com.uxitech.cocoaandroid.util.UIFactory.BaseAdapter


/**
 * Created by kuanyu on 2016/12/13.
 */

object DockView {

    lateinit var adapter: BaseAdapter<Brick>

    fun init(view: View, activity: Activity, bricks: List<Brick>) {
        adapter = MyAdapter(bricks as MutableList<Brick>)

        val dock = view.findViewById(R.id.dock) as RecyclerView
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        val dragListener = DragListener(activity)

        dock.layoutManager = layoutManager
        dock.adapter = DockView.adapter
        dock.setHasFixedSize(true)
        dock.setOnDragListener(dragListener)
    }

    class MyAdapter(bricks: MutableList<Brick>)
        : BaseAdapter<Brick>(bricks, ::createViewHolder)

}

private fun createViewHolder(parent: ViewGroup, viewType: Int): BrickViewHolder {
    val listener = TouchListener()
    return BrickViewHolder(parent.context, listener)
}
