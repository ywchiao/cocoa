package com.uxitech.cocoaandroid.view

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout

import com.uxitech.cocoaandroid.R
import com.uxitech.cocoaandroid.MainActivity
import com.uxitech.cocoaandroid.controller.Workspace.Toolbox


/**
 * Created by kuanyu on 2016/12/16.
 */

class ToolboxView(private val _ctx: Context,
                  private val _attrs: AttributeSet?) : RelativeLayout(_ctx, _attrs) {

    init {
        val inflater = LayoutInflater.from(_ctx)
        inflater.inflate(R.layout.toolbox, this)

        setView()
    }

    private fun setView() {
        initFABs()

        // Setup DockView
        DockView.init(
            this,
           _ctx as MainActivity,
           Toolbox.getBrickMaterials()
        )
    }

    /* Init Floating Action Buttons */
    fun initFABs() {
        val fabSwitcher = findViewById(R.id.dock_switcher) as FloatingActionButton
        val fabClear = findViewById(R.id.clear_edit_space) as FloatingActionButton
        val fabEvaluate = findViewById(R.id.evaluate) as FloatingActionButton

        fabSwitcher.setOnClickListener { v: View -> Toolbox.onDockChange(v) }
        fabClear.setOnClickListener { v: View -> Toolbox.onEditSpaceClear(v) }
        fabEvaluate.setOnClickListener { v: View -> Toolbox.onEvalBricks(v) }
    }
}

