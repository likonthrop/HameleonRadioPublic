package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Mount (
    @JsonProperty("path") var path: String? = null,
    @JsonProperty("is_default") var isDefault: Boolean? = null,
    @JsonProperty("id") var id: Long? = null,
    @JsonProperty("name") var name: String? = null,
    @JsonProperty("url") var url: String? = null,
    @JsonProperty("bitrate") var bitrate: Long? = null,
    @JsonProperty("format") var format: String? = null,
    @JsonProperty("listeners") var listeners: Listeners? = null
)