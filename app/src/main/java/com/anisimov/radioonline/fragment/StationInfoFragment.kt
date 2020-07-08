package com.anisimov.radioonline.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.MainActivity
import com.anisimov.radioonline.R
import com.anisimov.radioonline.databinding.FragmentStationInfoBinding
import com.anisimov.radioonline.item.AGAdapterRV
import com.anisimov.radioonline.item.models.TrackModel
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.radio.RadioService
import com.anisimov.radioonline.util.setImageFromUrl
import com.anisimov.requester.HttpResponseCallback
import com.anisimov.requester.generateMode
import com.anisimov.requester.getHttpResponse
import com.anisimov.requester.getSrcFromEntityCoverImage
import com.anisimov.requester.models.Info
import com.anisimov.requester.r.getHttpResponse as getRHttpResponse
import com.anisimov.requester.models.Root
import com.anisimov.requester.r.models.NowPlayingStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StationInfoFragment(private val station: StationModel, private val service: RadioService) : Fragment() {

    private lateinit var binding: FragmentStationInfoBinding

    private lateinit var adapter: AGAdapterRV

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_station_info, container, false)

        binding.apply {
            model = null
            recycle.adapter = null
            cover.setImageDrawable(null)

            binding.playButton.setOnClickListener {
                if (service.isPlaying) {
                    service.pause()
                    binding.playButton.setImageDrawable(resources.getDrawable(R.drawable.ic_play_station, null))
                } else {
                    service.play()
                    binding.playButton.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_station, null))
                }
            }

            binding.trackName.text = station.track?.getTrackString()?: station.name

            invalidateAll()
            try {
                var request = "/info/${station.id}"
                (activity as? MainActivity)?.sp?.getLong("authorize", 0)?.let { id ->
                    if (id > 0) request += "?request={\"id\":$id}"
                }
                getHttpResponse(request, object : HttpResponseCallback {
                    override fun onResponse(response: String) {
                        val nowPlayingStation = generateMode<Root>(response).info
                        CoroutineScope(Dispatchers.Main).launch {
                            nowPlayingStation?.let {
                                model = it
                                cover.setImageFromUrl(station.imageUrl)
                                val itemList = it.history.map { s -> TrackModel(s) }
                                adapter = AGAdapterRV(itemList)
                                recycle.adapter = adapter
                                invalidateAll()
                            }
                        }
                    }

                    override fun onError(e: String?) {
                        getRHttpResponse("nowplaying/${station.id}", object : HttpResponseCallback {
                            override fun onResponse(response: String) {
                                val nowPlayingStation = generateMode<NowPlayingStation>(response)
                                CoroutineScope(Dispatchers.IO).launch {
                                    nowPlayingStation.let {
                                        val itemList = arrayListOf(TrackModel().fromRSong(it.nowPlaying?.song, it.nowPlaying?.playedAt))
                                        itemList.addAll(it.songHistory?.map { s -> TrackModel().fromRSong(s.song, s.playedAt) }?.toTypedArray()?: arrayOf())

                                        launch(Dispatchers.Main) {
                                            model = Info().fromNPS(it)
                                            cover.setImageFromUrl(station.imageUrl)
                                            adapter = AGAdapterRV(itemList)
                                            recycle.adapter = adapter
                                            invalidateAll()
                                        }

                                        itemList.forEachIndexed { i, t ->
                                            t.cover = getSrcFromEntityCoverImage("${t.artist} ${t.title}" ,false)
                                            launch(Dispatchers.Main) {
                                                adapter.notifyItemChanged(i, it)
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                })
            } catch (e: Exception) {
                onDestroy()
            }
        }

        return binding.root
    }
}
