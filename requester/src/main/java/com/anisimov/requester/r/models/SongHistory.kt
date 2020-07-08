package com.anisimov.requester.r.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SongHistory (
    @JsonProperty("sh_id") var shId: Long? = null,
    @JsonProperty("played_at") var playedAt: Long? = null,
    @JsonProperty("duration") var duration: Long? = null,
    @JsonProperty("playlist") var playlist: String? = null,
    @JsonProperty("streamer") var streamer: String? = null,
    @JsonProperty("is_request") var isRequest: Boolean? = null,
    @JsonProperty("song") var song: Song? = null
)