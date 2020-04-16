package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Listeners(
    @JsonProperty("current") var current: Long? = null,
    @JsonProperty("unique") var unique: Long? = null,
    @JsonProperty("total") var total: Long? = null
)