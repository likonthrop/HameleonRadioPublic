package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class NowPlayingStation (
    @JsonProperty("station") var station: Station? = null,
    @JsonProperty("listeners") var listeners: Listeners? = null,
    @JsonProperty("live") var live: Live? = null,
    @JsonProperty("now_playing") var nowPlaying: NowPlaying? = null,
    @JsonProperty("playing_next") var playingNext: PlayingNext? = null,
    @JsonProperty("song_history") var songHistory: List<SongHistory>? = null,
    @JsonProperty("cache") var cache: String? = null
) {
    fun hasDescription(): Boolean {
        return !station?.description.isNullOrEmpty()
    }
}