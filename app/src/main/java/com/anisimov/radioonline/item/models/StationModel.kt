package com.anisimov.radioonline.item.models

import com.anisimov.radioonline.item.ITEM_STATION
import com.anisimov.radioonline.item.Item

data class StationModel(
    val id: Long? = 0,
    var index: Int = 0,
    val name: String = "",
    val shortcode: String? = null,
    val url: String? = null,
    var description: String? = null,
    var isPublic: Boolean? = null,
    var song: SongModel? = null,
    var current: Boolean = false,
    var enable: Boolean = false,
    var loading: Boolean = false
) : Item() {
    override val objectType: Int
        get() = ITEM_STATION

    fun showButton(): Boolean {
        return loading && !current || !current
    }

    fun showProgressBar(): Boolean {
        return !loading && current && enable
    }

    fun showPauseButton(): Boolean {
        return loading && current || current && !enable
    }

    fun getCover(): String {
        return "https://player.stvradio.online/static/icons/production/bage_$shortcode.jpg"
    }
}