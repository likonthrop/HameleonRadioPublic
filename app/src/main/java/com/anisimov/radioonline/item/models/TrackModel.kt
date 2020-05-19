package com.anisimov.radioonline.item.models

import android.annotation.SuppressLint
import com.anisimov.radioonline.item.ITEM_SONG
import com.anisimov.radioonline.item.Item
import com.anisimov.requester.models.Track

data class TrackModel(
    val cover: String = "",
    val artist: String = "",
    val title: String = ""
): Item() {
    @SuppressLint("DefaultLocale")
    constructor(track: Track?) : this(
        track?.imageUrl?:"",
        track?.artist?.trim()?:"",
        track?.title?.trim()?:""
    )

    override val objectType: Int
        get() = ITEM_SONG

    fun getTrackString() = "$artist ${if (artist.isNotEmpty() && title.isNotEmpty()) "-"  else ""} $title"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackModel

        if (artist != other.artist) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = artist.hashCode()
        result = 31 * result + title.hashCode()
        return result
    }

}
