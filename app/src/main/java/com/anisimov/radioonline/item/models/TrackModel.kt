package com.anisimov.radioonline.item.models

data class TrackModel(
    val artistName: String = "",
    val trackName: String = "",
    val albumCover: String = ""
) {
    fun getTrackString() = "$artistName - $trackName"
}
