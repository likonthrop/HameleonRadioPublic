package com.anisimov.radioonline.item.models

import com.anisimov.radioonline.R
import com.anisimov.radioonline.item.ITEM_STATION_BANNER

data class StationBanner(
    var bannerArray: List<BannerModel> = ArrayList(),
    var imageResource: Int = R.drawable.ic_banner_image
): Item() {

    override val objectType: Int
        get() = ITEM_STATION_BANNER
}