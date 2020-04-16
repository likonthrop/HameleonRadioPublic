package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Live (
    @JsonProperty("is_live") var isLive: Boolean? = null,
    @JsonProperty("streamer_name") var streamerName: String? = null
)