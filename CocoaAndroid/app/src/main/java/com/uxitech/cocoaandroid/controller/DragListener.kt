package com.uxitech.cocoaandroid.controller

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.DragEvent
import android.view.View

import com.uxitech.cocoaandroid.R
import com.uxitech.cocoaandroid.controller.Workspace.Toolbox.createBrick
import com.uxitech.cocoaandroid.util.UIFactory.showShortToast
import com.uxitech.cocoaandroid.view.EditSpaceView

/**
 * Created by kuanyu on 2016/12/30.
 *
 * TODO: Decouple object _ctx, _activity from DragListener
 */

class DragListener(private val _activity: Activity,
                   private val _ctx: Context = _activity) : View.OnDragListener {

    override fun onDrag(view: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> { }

            DragEvent.ACTION_DRAG_ENTERED -> {
                view.setBackgroundColor(
                    ContextCompat.getColor(_ctx, R.color.md_red_200)
                )
            }

            DragEvent.ACTION_DRAG_LOCATION -> { }

            DragEvent.ACTION_DRAG_EXITED -> {
                view.setBackgroundColor(
                    ContextCompat.getColor(_ctx, R.color.md_amber_200)
                )
            }

            DragEvent.ACTION_DROP -> OnDropCallback(view, event)

            DragEvent.ACTION_DRAG_ENDED -> OnDropEndedCallback(event)

            else -> {
                showShortToast(_ctx, "Unknown drag-action type received!")
                return false
            }
        }

        return true
    }

    private fun OnDropCallback(view: View, event: DragEvent): Boolean {
        val myView = { viewId: Int -> _activity.findViewById(viewId) }

        when(view) {
            myView(R.id.dock) -> {
                // still on source area
            }

            myView(R.id.edit_space) -> {
                val destArea = EditSpaceView.adapter

                val clipItem = event.clipData.getItemAt(0)
                val brickInfo = clipItem.text.toString().split(";")
                Log.d("OnDropCallback", brickInfo.toString())

                val imageId = Integer.parseInt(brickInfo[0])
                val brickTag = Integer.parseInt(brickInfo[1])

                val brick = createBrick(brickTag, imageId)
                destArea.addItem(brick)
            }

            else -> return false
        }

        view.setBackgroundColor(ContextCompat.getColor(_ctx, R.color.md_grey_300))

        return true
    }

    private fun OnDropEndedCallback(event: DragEvent): Boolean {
        if (event.result) {
            //val numBricks = EditSpaceView.adapter.getItems().size
            //showShortToast(_ctx, "Number of bricks on EditSpace: $numBricks")
        } else {
            showShortToast(_ctx, "Action drop did not work")
        }

        return true
    }
}
