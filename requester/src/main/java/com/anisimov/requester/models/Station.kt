package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Station  {
    @JsonProperty("imageUrl")
    var imageUrl: String? = null
    @JsonProperty("link")
    var link: String? = null
    @JsonProperty("name")
    var name: String? = null
    @JsonProperty("id")
    var id: Long? = null
    @JsonIgnore
    var track: Track? = null
}