package com.anisimov.radioonline.item.models

import com.anisimov.radioonline.item.ITEM_STATION_BANNER
import com.anisimov.radioonline.item.Item

data class StationBanner(
    val bannerArray: Array<BannerModel> = arrayOf()
): Item() {
    override val objectType: Int
        get() = ITEM_STATION_BANNER
}