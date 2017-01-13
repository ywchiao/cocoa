package com.uxitech.cocoaandroid.controller

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.view.MotionEvent
import android.view.View

/**
 * Created by kuanyu on 2016/12/30.
 */

class TouchListener : View.OnTouchListener {

    override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> actionDownCallback(view)

            MotionEvent.ACTION_UP -> actionUpCallback(view, motionEvent)

            else -> return false
        }

        return true
    }

    fun actionDownCallback(view: View?) {

        val viewTag = view?.tag.toString()
        val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
        val clipItem = ClipData.Item(viewTag)
        val extraInfo = ClipData(viewTag, mimeTypes, clipItem)

        val shadowBuilder = View.DragShadowBuilder(view)
        val NO_FLAGS = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view?.startDragAndDrop(extraInfo, shadowBuilder, view, NO_FLAGS)
        } else {
            view?.startDrag(extraInfo, shadowBuilder, view, NO_FLAGS)
        }

        //view.visibility = (View.INVISIBLE)
    }

    fun actionUpCallback(view: View?, motionEvent: MotionEvent) {
        val x = motionEvent.rawX.toInt()
        val y = motionEvent.rawY.toInt()
    }
}
