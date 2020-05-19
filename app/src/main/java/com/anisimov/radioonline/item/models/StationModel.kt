package com.anisimov.radioonline.item.models

import com.anisimov.radioonline.item.ITEM_STATION
import com.anisimov.radioonline.item.Item
import com.anisimov.requester.models.Track

data class StationModel(
    val id: Long? = 0,
    var index: Int = 0,
    val name: String = "",
    val imageUrl: String? = null,
    val link: String? = null,
    var track: TrackModel? = null,
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

    fun equalTrack(t: Track?): Boolean {
        if (t == null) return true
        if (track == null) return false
        if (track!!.artist != t.artist) return false
        if (track!!.title != t.title) return false
        return true
    }

    fun setTrack(t: Track?) {
        track = TrackModel(t?.imageUrl?:"", t?.artist?:"", t?.title?:"")
    }
}