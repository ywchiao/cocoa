package com.uxitech.cocoaandroid.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.util.AttributeSet

import com.uxitech.cocoaandroid.R
import com.uxitech.cocoaandroid.MainActivity


class SketchBoardView(private val _ctx: Context,
                      private val _attrs: AttributeSet?)
                        : RelativeLayout(_ctx, _attrs) {

    init {
        val inflater = LayoutInflater.from(_ctx)

        inflater.inflate(R.layout.sketch_board, this)

        // Setup the view of EditSpace
        EditSpaceView.init(
            this,
            _ctx as MainActivity
        )
    }
}

