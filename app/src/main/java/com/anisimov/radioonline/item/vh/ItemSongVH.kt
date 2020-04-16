package com.anisimov.radioonline.item.vh

import android.view.View
import androidx.databinding.DataBindingUtil
import com.anisimov.radioonline.databinding.ItemSongBinding
import com.anisimov.radioonline.item.AGViewHolder
import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.models.SongModel
import com.anisimov.radioonline.util.setImageFromUrl

class ItemSongVH(val view: View) : AGViewHolder(view) {

    private val binding = DataBindingUtil.bind<ItemSongBinding>(view)!!

    override fun bind(item: Item, position: Int) {
        item as SongModel
        binding.apply {
            model = item
            item.albumCover.let { cover.setImageFromUrl(it)}
        }
    }
}