package com.anisimov.radioonline.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.R
import com.anisimov.radioonline.databinding.FragmentStationInfoBinding
import com.anisimov.radioonline.item.AGAdapterRV
import com.anisimov.radioonline.item.models.SongModel
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.util.setImageFromUrl
import com.anisimov.requester.HttpResponseCallback
import com.anisimov.requester.generateMode
import com.anisimov.requester.getHttpResponse
import com.anisimov.requester.models.NowPlayingStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StationInfoFragment(private val station: StationModel) : Fragment() {

    private lateinit var binding: FragmentStationInfoBinding

    private lateinit var adapter: AGAdapterRV

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_station_info, container, false)

        binding.apply {
            model = null
            recycle.adapter = null
            cover.setImageDrawable(null)
            invalidateAll()
            try {
                getHttpResponse("nowplaying/${station.id}", object : HttpResponseCallback {
                    override fun onResponse(response: String) {
                        val nowPlayingStation = generateMode<NowPlayingStation>(response)
                        CoroutineScope(Dispatchers.Main).launch {
                            nowPlayingStation.let {
                                model = it
                                cover.setImageFromUrl(station.getCover())
                                val itemList = arrayListOf(SongModel(it.nowPlaying?.song))
                                itemList.addAll(it.songHistory?.map { s -> SongModel(s.song) }?.toTypedArray()?: arrayOf())
                                adapter = AGAdapterRV(itemList)
                                recycle.adapter = adapter
                                invalidateAll()
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                onDestroy()
            }
        }

        return binding.root
    }
}
