package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Root {
    @JsonProperty("stations")
    var stations: List<Station> = listOf()
    @JsonProperty("info")
    var info: Info? = null
    @JsonProperty("nowplay")
    var nowplay: Nowplay = Nowplay()
}