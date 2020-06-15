package com.anisimov.radioonline.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.MainActivity
import com.anisimov.radioonline.R
import com.anisimov.radioonline.databinding.FragmentStationBinding
import com.anisimov.radioonline.item.AGAdapterRV
import com.anisimov.radioonline.item.ITEM_STATION_BANNER
import com.anisimov.radioonline.item.banner.BannerClickListener
import com.anisimov.radioonline.item.models.*
import com.anisimov.radioonline.radio.IOnPlayListener
import com.anisimov.radioonline.radio.RadioService
import com.anisimov.requester.HttpResponseCallback
import com.anisimov.requester.generateMode
import com.anisimov.requester.getHttpResponse
import com.anisimov.requester.models.Root
import kotlinx.android.synthetic.main.fragment_station.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

const val STATION_TAG = "stationFragment"

class StationFragment(
    private val service: RadioService,
    private val itemList: ArrayList<Item> = arrayListOf()
) : Fragment(),
    AGAdapterRV.OnItemClickListener, BannerClickListener.OnItemClickListener {

    private lateinit var binding: FragmentStationBinding
    private lateinit var adapter: AGAdapterRV
    private var trackUpdater: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_station, container, false)

            adapter = AGAdapterRV(
                itemList,
                childFragmentManager
            )

            adapter.setOnItemClickListener(this)
            binding.recycle.adapter = adapter
            trackUpdater = makeTrackUpdater()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        service.subscribe(playListener)
        BannerClickListener.subscribe(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        service.unsubscribe(playListener)
        trackUpdater?.cancel()
        BannerClickListener.unsubscribe(this)
    }

    @SuppressLint("DefaultLocale")
    private fun makeTrackUpdater(): Job {
        return GlobalScope.launch(Dispatchers.Main) {
            while (adapter.itemCount > 0) {
                var request = "/nowPlay"
                (activity as? MainActivity)?.sp?.getLong("authorize", 0)?.let { id ->
                    if (id > 0) request += "?request={\"id\":$id}"
                }
                getHttpResponse(request, object : HttpResponseCallback {
                    override fun onResponse(response: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val nowPlay = generateMode<Root>(response).nowplay
                            (itemList).filterIsInstance<StationModel>().forEachIndexed { i, it ->
                                val id = it.id ?: 0
                                val track = nowPlay.getTrack(id)
                                if (track?.artist?.isEmpty() == true && track.title?.isEmpty() == true) {
                                    adapter.notifyItemRemoved(i)
                                    itemList.remove(it)
                                    itemList.forEachIndexed { index, item ->
                                        (item as? StationModel)?.index = index
                                    }
                                    return@forEachIndexed
                                }
                                if (!it.equalTrack(track)) {
                                    it.setTrack(track)
                                    adapter.notifyItemChanged(i, it)
                                }
                            }
                            if (itemList[0].objectType == ITEM_STATION_BANNER) {
                                (itemList[0] as StationBanner).let {
                                    if (it.bannerArray.isEmpty()) {
                                        it.bannerArray = nowPlay.advertising?.map { b ->
                                            BannerModel(
                                                b.imageUrl ?: "",
                                                b.description ?: ""
                                            )
                                        } ?: listOf()
                                        adapter.notifyItemChanged(0, it)
                                    }
                                }
                            } else {
                                if (!nowPlay.advertising.isNullOrEmpty()) {
                                    itemList.add(0, StationBanner(nowPlay.advertising!!.map { b ->
                                        BannerModel(
                                            b.imageUrl ?: "",
                                            b.description ?: ""
                                        )
                                    }))
                                    adapter.notifyItemInserted(0)
                                    itemList.forEachIndexed { index, item ->
                                        (item as? StationModel)?.index = index
                                    }
                                    recycle.scrollToPosition(0)
                                }
                            }
                        }
                    }

                    override fun onError(e: String?) {
                        itemList.forEachIndexed { i, item ->
                            (item as? StationModel)?.let { station ->
                                station.link?.let { url ->
                                    try {
                                        val meta = BufferedReader(
                                            InputStreamReader(
                                                URL("${url.split("?")[0]}.xspf")
                                                    .openStream()
                                            )
                                        ).readLines().toString().replace("amp;", "")
                                        launch(Dispatchers.Main) {
                                            val metaSplit =
                                                meta.substringAfter("<title>")
                                                    .substringBefore("</title>")
                                                    .toLowerCase().split(" - ")
                                                    .map { it.trim().capitalize() }
                                            if (metaSplit.size == 2) {
                                                if (item.track == null || item.track!!.artist != metaSplit[0] || item.track!!.title != metaSplit[1]) {
                                                    item.track = TrackModel(
                                                        item.imageUrl ?: "",
                                                        metaSplit[0],
                                                        metaSplit[1]
                                                    )
                                                    adapter.notifyItemChanged(i, item)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(this::class.java.simpleName, e.localizedMessage ?: "")
                                    }
                                }
                            }
                        }
                    }
                })
                delay(5000)
            }
        }
    }

    fun playForward() {
        val hasBanner = itemList.any { it.objectType == ITEM_STATION_BANNER }
        for ((i, s) in (itemList).withIndex()) {
            s as? StationModel ?: continue
            if (s.current) {
                s.current = false
                s.enable = false
                adapter.notifyItemChanged(i)
            }
        }
        val pos = if (service.station != null) itemList.indexOf(service.station as Item) else 0
        if (pos < itemList.size - 1) {
            val i = pos + 1
            service.withStation(itemList[i])
            itemList[i].apply {
                if (this !is StationModel) return
                current = true
                enable = true
            }
        } else {
            val i = if (hasBanner) 1 else 0
            service.withStation(itemList[i])
            itemList[i].apply {
                if (this !is StationModel) return
                current = true
                enable = true
            }
        }
    }

    fun playBelow() {
        val hasBanner = itemList.any { it.objectType == ITEM_STATION_BANNER }
        for ((i, s) in itemList.withIndex()) {
            s as? StationModel ?: continue
            if (s.current) {
                s.current = false
                s.enable = false
                adapter.notifyItemChanged(i)
            }
        }
        val pos = if (service.station != null) itemList.indexOf(service.station as Item) else 1
        if (pos > if (hasBanner) 1 else 0) {
            val i = pos - 1
            service.withStation(itemList[i])
            itemList[i].apply {
                if (this !is StationModel) return
                current = true
                enable = true
            }
        } else {
            val i = itemList.size - 1
            service.withStation(itemList[i])
            itemList[i].apply {
                if (this !is StationModel) return
                current = true
                enable = true
            }
        }
    }

    override fun onItemClick(position: Int, type: Int, v: View?) {
        if (v == null) return
        val item = itemList[position] as? StationModel ?: return

        for ((i, s) in itemList.withIndex()) {
            s as? StationModel ?: continue
            if (i != position && s.current) {
                s.current = false
                s.enable = false
                s.loading = false
                adapter.notifyItemChanged(i, s)
            }
        }

        if (v.id == R.id.stationRoot) {
            if (item.enable) {
                (activity as? MainActivity)?.showPlayer()
            } else {
                item.apply {
                    loading = true
                    current = true
                }
                adapter.notifyItemChanged(position)
                service.withStation(itemList[position])
                (activity as? MainActivity)?.showPlayer()
            }
        } else {
            if (item.enable && !item.loading) {
                item.enable = false
                adapter.notifyItemChanged(position)
                service.pause()
            } else {
                item.apply {
                    loading = true
                    current = true
                }
                adapter.notifyItemChanged(position)
                service.withStation(itemList[position])
            }
        }
    }

    private val playListener = object : IOnPlayListener {
        override fun onPlay(playState: Boolean) {
            for ((i, s) in itemList.withIndex()) {
                s as? StationModel ?: continue
                if (s.current) {
                    s.enable = playState
                    adapter.notifyItemChanged(i, s)
                }
            }
        }

        override fun onStop() {
            for ((i, s) in itemList.withIndex()) {
                s as? StationModel ?: continue
                if (s.current) {
                    s.current = false
                    s.enable = false
                    adapter.notifyItemChanged(i, s)
                }
            }
        }

        override fun onLoad(inProgress: Boolean) {
            for ((i, s) in itemList.withIndex()) {
                s as? StationModel ?: continue
                if (s.current || s.loading) {
                    s.loading = inProgress
                    adapter.notifyItemChanged(i, s)
                }
            }
        }
    }

    override fun onClick(item: BannerModel, position: Int, v: View?) {
        CoroutineScope(Dispatchers.Main).launch {
            //            Toast.makeText(context, item.ling, Toast.LENGTH_SHORT).show()
        }
    }
}
