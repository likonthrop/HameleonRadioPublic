package com.anisimov.radioonline.item.models

import android.annotation.SuppressLint
import com.anisimov.radioonline.item.ITEM_SONG
import com.anisimov.requester.models.Track
import com.anisimov.requester.r.models.Song
import java.util.*

data class TrackModel(
    var cover: String = "",
    var artist: String = "",
    var title: String = "",
    var startPlay: Long = 0L
): Item() {
    @SuppressLint("DefaultLocale")
    constructor(track: Track?) : this(
        track?.imageUrl?:"",
        track?.artist?.trim()?:"",
        track?.title?.trim()?:"",
        track?.startPlay?.times(1000)?:0
    )

    override val objectType: Int
        get() = ITEM_SONG

    fun getTrackString() = "$artist ${if (artist.isNotEmpty() && title.isNotEmpty()) "-"  else ""} $title"
    fun getTime(): String {

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startPlay

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val min = calendar.get(Calendar.MINUTE)

        return "${if (hour < 10) "0$hour" else "$hour"}:${if (min < 10) "0$min" else "$min"}"
    }

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

    fun fromRSong(song: Song?, startPlay:Long? = 0L): TrackModel {
        cover = song?.art ?:""
        artist = song?.artist?:""
        title = song?.title?:""
        this.startPlay = startPlay?.times(1000)?:0L
        return this
    }

}
