package com.anisimov.radioonline.item.models

import com.anisimov.radioonline.item.ITEM_SONG
import com.anisimov.radioonline.item.Item
import com.anisimov.requester.models.Song

data class SongModel(
    val artistName: String = "",
    val trackName: String = "",
    val albumCover: String = ""
): Item() {
    constructor(song: Song?) : this(song?.artist?:"", song?.title?:"", song?.art?:"")

    override val objectType: Int
        get() = ITEM_SONG

    fun getTrackString() = "$artistName ${if (artistName.isNotEmpty() && trackName.isNotEmpty()) "-"  else ""} $trackName"
}
