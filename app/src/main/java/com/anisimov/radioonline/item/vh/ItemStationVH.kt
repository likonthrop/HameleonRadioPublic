package com.anisimov.radioonline.item.vh

import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.anisimov.radioonline.databinding.ItemStationBinding
import com.anisimov.radioonline.item.AGAdapterRV
import com.anisimov.radioonline.item.AGViewHolder
import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.setImageFromUrl

class ItemStationVH(
    val view: View,
    onItemClickListener: AGAdapterRV.OnItemClickListener?) : AGViewHolder(view, onItemClickListener) {

    private val binding = DataBindingUtil.bind<ItemStationBinding>(view)!!

    override fun bind(item: Item, position: Int) {
        item as StationModel
        binding.model = item
        binding.progressBar.enable(item.enable)
        item.cover?.let { binding.cover.setImageFromUrl(it)}
    }
}