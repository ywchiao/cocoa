package com.uxitech.cocoaandroid.view

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.uxitech.cocoaandroid.model.Brick

import com.uxitech.cocoaandroid.util.UIFactory.BaseAdapter.BaseViewHolder


/**
 * Created by kuanyu on 2016/12/22.
 */


class BrickViewHolder(parentCtx: Context
                , listener: View.OnTouchListener? = null
                , private val myView: ImageView = ImageView(parentCtx))
    : BaseViewHolder<Brick>(myView, listener) {


    override fun setItemView(item: Brick, position: Int) {
        myView.setImageResource(item.imageId)

        // I don't know how to pass object to Drag and Drop callbacks, It seems like
        // ClipData can only hold plain string.
        // So below is a naive way for passing brick info (brickTag, imageId)
        // to OnDropCallback.
        myView.tag = "${item.imageId};${item.tag.v}"

        // If the item is layout:
        //val itemView = LayoutInflater.from(parent.context)
        //            .inflate(R.layout.some_layout, parent, false)
    }
}

