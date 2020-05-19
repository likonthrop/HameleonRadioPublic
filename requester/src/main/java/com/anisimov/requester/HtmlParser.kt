package com.anisimov.requester

import org.jsoup.Jsoup
import org.jsoup.nodes.Document


fun getSrcFromEntityCoverImage(trackName: String?, small: Boolean = true): String {
    if (trackName.isNullOrEmpty()) return ""
    val html: Document = Jsoup.connect("https://music.yandex.ru/search?text=$trackName").get()
    val coverTag = html.body().getElementsByClass("entity-cover__image")
    if (coverTag.isEmpty()) return ""
    return "https:${
    if(small) coverTag[0].attr("srcset").split(", ")[0] 
    else coverTag[0].attr("srcset").split(", ")[1].substringBefore(" 2x")}"
}