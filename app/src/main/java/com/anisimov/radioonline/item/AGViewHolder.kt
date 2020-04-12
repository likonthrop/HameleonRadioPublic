package com.anisimov.radioonline.item

import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import androidx.recyclerview.widget.RecyclerView.ViewHolder

open class AGViewHolder : ViewHolder, OnClickListener, OnLongClickListener {
    private var onItemClickListener: AGAdapterRV.OnItemClickListener? = null
    private var onItemLongClickListener: AGAdapterRV.OnItemLongClickListener? = null

    constructor(itemView: View, onItemClickListener: AGAdapterRV.OnItemClickListener?) : super(itemView) {
        this.onItemClickListener = onItemClickListener
        itemView.setOnClickListener(this)
    }

    constructor(itemView: View, onItemClickListener: AGAdapterRV.OnItemLongClickListener?) : super(itemView) {
        onItemLongClickListener = onItemClickListener
        itemView.setOnLongClickListener(this)
    }

    constructor(itemView: View, onItemClickListener: AGAdapterRV.OnItemClickListener?, onItemLongClickListener: AGAdapterRV.OnItemLongClickListener?) : super(itemView) {
        this.onItemClickListener = onItemClickListener
        this.onItemLongClickListener = onItemLongClickListener
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    constructor(itemView: View?) : super(itemView!!)

    open fun bind(item: Item, position: Int) {}
    override fun onClick(view: View) {
        if (onItemClickListener != null) {
            onItemClickListener!!.onItemClick(adapterPosition, itemViewType, view)
        }
    }

    fun onViewRecycled() {}
    override fun onLongClick(v: View): Boolean {
        if (onItemLongClickListener != null) {
            onItemLongClickListener!!.onItemLongClick(adapterPosition, itemViewType, v)
        }
        return false
    }
}