package com.anisimov.radioonline.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.anisimov.radioonline.R
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.item.vh.ItemStationVH
import com.anisimov.radioonline.item.itemhelper.ItemTouchHelper
import com.anisimov.radioonline.item.vh.ItemStationBannerVH
import java.util.*

const val ITEM_STATION = 100
const val ITEM_STATION_BANNER = 999

class AGAdapterRV(
    private val objects: List<Item>,
    private val fm: FragmentManager? = null,
    private val hasBanner: Boolean = false
) : Adapter<AGViewHolder>(), ItemTouchHelper {

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AGViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ITEM_STATION_BANNER -> {
                return ItemStationBannerVH(
                    inflater.inflate(
                        R.layout.item_station_banner,
                        parent,
                        false
                    ), fm
                )
            }
            ITEM_STATION -> return ItemStationVH(
                inflater.inflate(
                    R.layout.item_station,
                    parent,
                    false
                ), onItemClickListener
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

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener?) {
        this.onItemLongClickListener = onItemLongClickListener
    }

    override fun onRowDismiss(position: Int) {
    }

    override fun onRowMoved(from: Int, to: Int) {
        Collections.swap(objects, from, to)
        notifyItemMoved(from, to)
        objects.forEachIndexed { i, o -> (o as? StationModel)?.index = i }
    }
}