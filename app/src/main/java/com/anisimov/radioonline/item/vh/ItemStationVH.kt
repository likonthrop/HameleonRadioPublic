package com.anisimov.radioonline.item.vh

import android.view.View
import androidx.databinding.DataBindingUtil
import com.anisimov.radioonline.databinding.ItemStationBinding
import com.anisimov.radioonline.item.AGAdapterRV
import com.anisimov.radioonline.item.AGViewHolder
import com.anisimov.radioonline.item.models.Item
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.util.setBlurredImageFromUrl
import com.anisimov.radioonline.util.setImageFromUrl

class ItemStationVH(
    val view: View,
    private val onItemClickListener: AGAdapterRV.OnItemClickListener?
) : AGViewHolder(view, onItemClickListener) {

    private val binding = DataBindingUtil.bind<ItemStationBinding>(view)!!

    override fun bind(item: Item, position: Int) {
        item as StationModel
        binding.apply {
            model = item
            playButton.setOnClickListener {
                onItemClickListener?.onItemClick(item.index, item.objectType, it)
            }
            pauseButton.setOnClickListener {
                onItemClickListener?.onItemClick(item.index, item.objectType, it)
            }
            if (item.current) backGround.setBlurredImageFromUrl(item.track?.cover)
            else backGround.setImageDrawable(null)
            trackName.isSelected = true
            cover.setImageFromUrl(item.imageUrl)
        }
    }
}