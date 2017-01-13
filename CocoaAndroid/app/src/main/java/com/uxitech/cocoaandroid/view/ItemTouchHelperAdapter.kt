package com.uxitech.cocoaandroid.view

/**
 * Created by kuanyu on 2016/12/15.
 */

interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemDismiss(position: Int)
}
