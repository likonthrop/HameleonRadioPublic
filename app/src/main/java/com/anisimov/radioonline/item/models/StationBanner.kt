package com.anisimov.radioonline.item.models

import com.anisimov.radioonline.item.ITEM_STATION_BANNER

data class StationBanner(
    var bannerArray: List<BannerModel> = ArrayList()
): Item() {

    override val objectType: Int
        get() = ITEM_STATION_BANNER
}