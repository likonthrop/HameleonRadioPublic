package com.anisimov.requester.models

import com.anisimov.requester.r.models.SongHistory
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Track {
    @JsonProperty("duration")
    var duration: Long? = null
    @JsonProperty("played_at")
    var startPlay: Long? = null
    @JsonProperty("artist")
    var artist: String? = null
    @JsonProperty("imageUrl")
    var imageUrl: String? = null
    @JsonProperty("title")
    var title: String? = null

    fun fromSH(sh: SongHistory): Track {
        duration = sh.duration
        startPlay = sh.playedAt
        artist = sh.song?.artist
        imageUrl = sh.song?.art
        title = sh.song?.title
        return this
    }
}