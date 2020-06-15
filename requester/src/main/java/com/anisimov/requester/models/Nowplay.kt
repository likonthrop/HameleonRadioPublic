package com.anisimov.requester.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Nowplay {
    @JsonProperty("1")
    var st1: Track? = null
    @JsonProperty("2")
    var st2: Track? = null
    @JsonProperty("3")
    var st3: Track? = null
    @JsonProperty("4")
    var st4: Track? = null
    @JsonProperty("5")
    var st5: Track? = null
    @JsonProperty("6")
    var st6: Track? = null
    @JsonProperty("7")
    var st7: Track? = null
    @JsonProperty("8")
    var st8: Track? = null
    @JsonProperty("9")
    var st9: Track? = null
    @JsonProperty("10")
    var st10: Track? = null
    @JsonProperty("11")
    var st11: Track? = null
    @JsonProperty("12")
    var st12: Track? = null
    @JsonProperty("13")
    var st13: Track? = null
    @JsonProperty("14")
    var st14: Track? = null
    @JsonProperty("15")
    var st15: Track? = null
    @JsonProperty("advertising")
    var advertising: List<Advertising>? = null

    fun getTrack(id: Long): Track? {
        return mapOf(
            Pair(2L, st2),
            Pair(3L, st3),
            Pair(4L, st4),
            Pair(5L, st5),
            Pair(6L, st6),
            Pair(7L, st7),
            Pair(8L, st8),
            Pair(9L, st9),
            Pair(10L, st10),
            Pair(11L, st11),
            Pair(12L, st12),
            Pair(13L, st13),
            Pair(14L, st14),
            Pair(15L, st15)
        ).filterNot { it.value == null }[id]
    }
}