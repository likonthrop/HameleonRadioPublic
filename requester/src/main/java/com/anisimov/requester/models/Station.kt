package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Station (
    @JsonProperty("id") var id: Long? = null,
    @JsonProperty("name") var name: String? = null,
    @JsonProperty("shortcode") var shortcode: String? = null,
    @JsonProperty("description") var description: String? = null,
    @JsonProperty("frontend") var frontend: String? = null,
    @JsonProperty("backend") var backend: String? = null,
    @JsonProperty("listen_url") var listenUrl: String? = null,
    @JsonProperty("is_public") var isPublic: Boolean? = null,
    @JsonProperty("mounts") var mounts: List<Mount>? = null,
    @JsonProperty("remotes") var remotes: List<Any>? = null
)