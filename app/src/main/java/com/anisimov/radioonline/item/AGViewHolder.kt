package com.anisimov.radioonline.item

import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.anisimov.radioonline.item.models.Item

open class AGViewHolder : ViewHolder, OnClickListener, OnLongClickListener {
    private var onItemClickListener: AGAdapterRV.OnItemClickListener? = null
    private var onItemLongClickListener: AGAdapterRV.OnItemLongClickListener? = null

    constructor(itemView: View, onItemClickListener: AGAdapterRV.OnItemClickListener?) : super(itemView) {
        this.onItemClickListener = onItemClickListener
        itemView.setOnClickListener(this)
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