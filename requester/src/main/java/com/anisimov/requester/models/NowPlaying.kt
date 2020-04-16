package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NowPlaying (
    @JsonProperty("elapsed") var elapsed: Long? = null,
    @JsonProperty("remaining") var remaining: Long? = null,
    @JsonProperty("sh_id") var shId: Long? = null,
    @JsonProperty("played_at") var playedAt: Long? = null,
    @JsonProperty("duration") var duration: Long? = null,
    @JsonProperty("playlist") var playlist: String? = null,
    @JsonProperty("streamer") var streamer: String? = null,
    @JsonProperty("is_request") var isRequest: Boolean? = null,
    @JsonProperty("song") var song: Song? = null
)