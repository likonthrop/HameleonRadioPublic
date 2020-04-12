package com.anisimov.radioonline.item.models

import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.ITEM_STATION

data class StationModel(
    var index: Int = 0,
    val name: String = "",
    val cover: String? = null,
    val url: String? = null,
    var track: TrackModel? = null,
    var current: Boolean = false,
    var enable: Boolean = false,
    var loading: Boolean = false
) : Item() {
    override val objectType: Int
        get() = ITEM_STATION

    fun showButton(): Boolean {
        return !loading && !current
    }

    fun showProgressBar(): Boolean {
        return !loading && current
    }
}