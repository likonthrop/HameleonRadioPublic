package com.anisimov.radioonline.item.banner

import android.view.View
import com.anisimov.radioonline.item.models.BannerModel

object BannerClickListener {
    private val subscribes = arrayListOf<OnItemClickListener>()

    fun onItemClick(item: BannerModel, position: Int, v: View?){
        subscribes.forEach { it.onClick(item, position, v) }
    }

    fun subscribe(listenerOn: OnItemClickListener) {
        if (!subscribes.contains(listenerOn)) subscribes.add(listenerOn)
    }

    fun unsubscribe(listenerOn: OnItemClickListener) {
        if (subscribes.contains(listenerOn)) subscribes.remove(listenerOn)
    }

    interface OnItemClickListener {
        fun onClick(item: BannerModel, position: Int, v: View?)
    }
}