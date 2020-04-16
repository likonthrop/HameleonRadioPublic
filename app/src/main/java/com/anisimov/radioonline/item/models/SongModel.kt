package com.anisimov.radioonline.item.models

import android.annotation.SuppressLint
import com.anisimov.radioonline.item.ITEM_SONG
import com.anisimov.radioonline.item.Item
import com.anisimov.requester.models.Song

data class SongModel(
    val artistName: String = "",
    val trackName: String = "",
    val albumCover: String = ""
): Item() {
    @SuppressLint("DefaultLocale")
    constructor(song: Song?) : this(song?.artist?.toLowerCase()?.trim()?.capitalize()?:"",
        song?.title?.toLowerCase()?.trim()?.capitalize()?:"", song?.art?:"")

    override val objectType: Int
        get() = ITEM_SONG

    fun getTrackString() = "$artistName ${if (artistName.isNotEmpty() && trackName.isNotEmpty()) "-"  else ""} $trackName"
}
