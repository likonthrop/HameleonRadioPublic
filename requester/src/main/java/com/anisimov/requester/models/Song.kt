package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Song (
    @JsonProperty("id") var id: String? = null,
    @JsonProperty("text") var text: String? = null,
    @JsonProperty("artist") var artist: String? = null,
    @JsonProperty("title") var title: String? = null,
    @JsonProperty("album") var album: String? = null,
    @JsonProperty("lyrics") var lyrics: String? = null,
    @JsonProperty("art") var art: String? = null,
    @JsonProperty("custom_fields") var customFields: List<Any>? = null
)