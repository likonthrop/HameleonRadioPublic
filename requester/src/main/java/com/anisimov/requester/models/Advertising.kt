package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Advertising {
    @JsonProperty("imageUrl")
    var imageUrl: String? = null
    @JsonProperty("description")
    var description: String? = null
    @JsonProperty("id")
    var id: Long? = null
}