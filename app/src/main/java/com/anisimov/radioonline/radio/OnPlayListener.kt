package com.anisimov.radioonline.radio

import com.anisimov.radioonline.item.models.StationModel

interface OnPlayListener {
        fun onPlay(playState: Boolean)
        fun onStop()
        fun onStationChange(station: StationModel) {}
        fun onLoad(inProgress: Boolean) {}
    }