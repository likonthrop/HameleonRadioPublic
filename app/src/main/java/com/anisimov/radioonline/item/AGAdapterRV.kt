package com.anisimov.radioonline.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.anisimov.radioonline.R
import com.anisimov.radioonline.item.models.Item
import com.anisimov.radioonline.item.vh.ItemSongVH
import com.anisimov.radioonline.item.vh.ItemStationVH

const val ITEM_STATION = 100
const val ITEM_SONG = 101
const val ITEM_STATION_BANNER = 999

class AGAdapterRV(private val objects: List<Item>, private val fm: FragmentManager? = null) : Adapter<AGViewHolder>()/*, ItemTouchHelper*/ {

    private var onRawMoveListener: OnRawMoveListener? = null
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AGViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ITEM_STATION -> return ItemStationVH(
                inflater.inflate(
                    R.layout.item_station,
                    parent,
                    false
                ), onItemClickListener
            )
            ITEM_SONG -> return ItemSongVH(
                inflater.inflate(
                    R.layout.item_song,
                    parent,
                    false
                )
            )
        }
        return AGViewHolder(View(parent.context))
    }

    override fun onBindViewHolder(holder: AGViewHolder, position: Int) {
        if (position !in objects.indices) return
        holder.bind(objects[position], position)
    }

    override fun getItemViewType(position: Int): Int {
        return objects[position].objectType
    }

    override fun getItemCount(): Int {
        return objects.size
    }

    override fun onViewRecycled(holder: AGViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, type: Int, v: View?)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int, type: Int, v: View?)
    }

    interface OnRawMoveListener {
        fun onMove()
    }

    fun setOnRawMove(onRawMoveListener: OnRawMoveListener) {
        this.onRawMoveListener = onRawMoveListener
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    /*override fun onRowMoved(from: Int, to: Int) {
        Collections.swap(objects, from, to)
        notifyItemMoved(from, to)
        objects.forEachIndexed { i, o -> (o as? StationModel)?.index = i }
        onRawMoveListener?.onMove()
    }*/
}