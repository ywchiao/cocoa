package com.uxitech.cocoaandroid.util

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.widget.Toast

import com.uxitech.cocoaandroid.view.ItemTouchHelperAdapter

/**
 * Created by kuanyu on 2016/12/16.
 */


object UIFactory {

    /*  General RecyclerView adapter (Damn! Too complex [>_<])
     *
     *  Generics:
     *      @. `E` is the generic type of list element(item).
     *
     *  Class parameters:
     *      @ items: List items with type `E`.
     *
     *
     *  Callbacks:
     *      @ OnItemMove() and onItemDismiss() is only effective when you
     *        register the ItemTouchHelper. (i.e. itemTouchHelper.attachToRecyclerView(someView))
     *
     * * */
    abstract class BaseAdapter<E>(private var _items: MutableList<E>,
                                  private val createVH: (ViewGroup, Int) -> BaseViewHolder<E>)
        : ItemTouchHelperAdapter,
        RecyclerView.Adapter<BaseAdapter.BaseViewHolder<E>>() {

        override
        fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<E> {
            val viewHolder = createVH(parent, viewType)

            return viewHolder
        }

        override
        fun onBindViewHolder(viewHolder: BaseViewHolder<E>, position: Int) {
            val item = _items[position]
            viewHolder.setItemView(item, position)
        }

        override fun getItemCount(): Int = _items.size

        override
        fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            val fromElement = _items.removeAt(fromPosition)

            _items.add(toPosition, fromElement)
            notifyItemMoved(fromPosition, toPosition)

            return true
        }

        override
        fun onItemDismiss(position: Int) {
            _items.removeAt(position)
            notifyItemRemoved(position)
        }

        fun getItems(): MutableList<E> {
            return _items
        }

        fun setItems(items: MutableList<E>) {
            _items = items
            notifyDataSetChanged()
        }

        fun clearItems() {
            _items.clear()
            notifyDataSetChanged()
        }

        fun addItem(item: E) {
            _items.add(item)
            notifyItemInserted(_items.size - 1)
        }


        /* General View Holder
         *
         *  Params:
         *     @ myItemView: Don't change variable name to `itemView`, RecyclerView.ViewHolder
         *          already have that member.
         *     @ listener: Use for handling touch events
         *
         *  Abstract member:
         *      @ setItem(item, position):
         *          This function basically used to setup sub-views in
         *        view holder of RecyclerView (e.g. myImageView.setImageResources(id)). Due to
         *        that we may reuse RecyclerView with custom view holders, please override
         *        it to complete custom view initiation.
         *        p.s. Being invoked in onBindViewHolder of BaseAdapter.
         *
         */
        abstract class BaseViewHolder<in E>(val myItemView: View,
                                            listener: View.OnTouchListener? = null)
            : RecyclerView.ViewHolder(myItemView) {

            init {
                if (listener != null)
                    myItemView.setOnTouchListener(listener)
            }

            /*
                Params:
                    @item: Item of RecyclerView
                    @position: position of this @item
             */
            abstract fun setItemView(item: E, position: Int)
        }
    }


    data class DialogOptions(
       val title: String,
       val message: String?,
       val items: Array<String>?,
       val onItemClick: ((DialogInterface, Int) -> Unit)?
    )


    fun showDialog(options: DialogOptions, activity: FragmentActivity, tag: String) {
        val dialog = MyDialogFragment(
            options.title,
            options.message,
            options.items,
            options.onItemClick)
        val manager = activity.supportFragmentManager

        dialog.show(manager, tag)
    }

    class MyDialogFragment(
        private val _title: String,
        private val _message: String?,
        private val _items: Array<String>?,
        private val _onItemClick: ((DialogInterface, Int) -> Unit)? = null
    ): DialogFragment() {

        override
        fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)

            builder.setTitle(_title)

            if (_items != null && _items.isNotEmpty()) {
                builder.setItems(_items, _onItemClick)
            } else {
                builder.setMessage(_message)
            }

            builder.setNegativeButton("Cancel") { dialog, id -> dialog.cancel() }

            // return super.onCreateDialog(savedInstanceState)
            return builder.create()
        }
    }

    fun showLongToast(ctx: Context, msg: String) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
    }

    fun showShortToast(ctx: Context, msg: String) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }


    fun isViewInBounds(view: View, x: Int, y: Int): Boolean {
        val bounds = Rect()
        val location = IntArray(2)

        view.getDrawingRect(bounds)
        view.getLocationOnScreen(location)
        bounds.offset(location[0], location[1])

        return bounds.contains(x, y)
    }
}