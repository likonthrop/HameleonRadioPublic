package com.anisimov.radioonline.item.vh

import android.view.View
import androidx.databinding.DataBindingUtil
import com.anisimov.radioonline.databinding.ItemSongBinding
import com.anisimov.radioonline.item.AGViewHolder
import com.anisimov.radioonline.item.models.Item
import com.anisimov.radioonline.item.models.TrackModel
import com.anisimov.radioonline.util.setImageFromUrl

class ItemSongVH(val view: View) : AGViewHolder(view) {

    private val binding = DataBindingUtil.bind<ItemSongBinding>(view)!!

    override fun bind(item: Item, position: Int) {
        item as TrackModel
        binding.apply {
            model = item
            cover.setImageFromUrl(item.cover)
        }
    }
}