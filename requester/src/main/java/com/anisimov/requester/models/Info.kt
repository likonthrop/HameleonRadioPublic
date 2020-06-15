package com.anisimov.requester.models

import com.anisimov.requester.r.models.NowPlayingStation
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class Info {
    @JsonProperty("imageUrl")
    var imageUrl: String? = null
    @JsonProperty("name")
    var name: String? = null
    @JsonProperty("description")
    var description: String? = null
    @JsonProperty("history")
    var history: List<Track> = listOf()
    @JsonProperty("id")
    var id: Long? = null

    fun fromNPS(nps: NowPlayingStation): Info {
        imageUrl = "https://player.stvradio.online/static/icons/production/bage_${nps.station?.shortcode}.jpg"
        name = nps.station?.name
        description = nps.station?.description
        history = nps.songHistory?.map { Track().fromSH(it) }?: listOf()
        id = nps.station?.id

        return this
    }
}