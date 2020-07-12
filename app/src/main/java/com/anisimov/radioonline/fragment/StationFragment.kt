package com.anisimov.radioonline.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.MainActivity
import com.anisimov.radioonline.R
import com.anisimov.radioonline.databinding.FragmentStationBinding
import com.anisimov.radioonline.item.AGAdapterRV
import com.anisimov.radioonline.item.ITEM_STATION_BANNER
import com.anisimov.radioonline.item.banner.AGBannerAdapter
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
import java.io.Reader
import java.net.URL
import java.util.*

const val STATION_TAG = "stationFragment"

class StationFragment(
    private val service: RadioService,
    private val itemList: ArrayList<Item> = arrayListOf()
) : Fragment(),
    AGAdapterRV.OnItemClickListener, BannerClickListener.OnItemClickListener,
    AGBannerAdapter.OnItemClickListener {

    private lateinit var binding: FragmentStationBinding
    private lateinit var adapter: AGAdapterRV
    private var trackUpdater: Job? = null

    private var bannerAnimation: Job? = null
    private var bannerAdapter: AGBannerAdapter? = null
    private val bannerArray = arrayListOf<BannerModel>()

    private fun runBannerAnimation(): Job {
        return GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                delay(10000)
                binding.banner.apply {
                    if (currentItem < bannerArray.size - 1) currentItem++ else currentItem = 1
                }
            }
        }
    }

    override fun onItemClick(position: Int, v: View?) {
        BannerClickListener.onItemClick(bannerArray[position], position, v)
    }

    fun bindBanner(banner: StationBanner) {
        if (bannerAdapter != null) return
        bannerArray.addAll(banner.bannerArray)
        childFragmentManager.let {
            try {
                bannerAdapter = AGBannerAdapter(it, bannerArray)
                bannerAdapter?.setOnItemClickListener(this)

                binding.banner.apply {
                    visibility = View.VISIBLE
                    adapter = bannerAdapter
                    setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> bannerAnimation?.cancel()
                            MotionEvent.ACTION_UP -> bannerAnimation = runBannerAnimation()
                        }
                        return@setOnTouchListener false
                    }
                }
                bannerAnimation = runBannerAnimation()
            } catch (e: Exception) {
            }
        }
    }


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
                                if (!it.equalTrack(track)) {
                                    it.setTrack(track)
                                    adapter.notifyItemChanged(i, it)
                                }
                            }

                            if (!nowPlay.advertising.isNullOrEmpty()) {
                                val stationBanner =
                                    StationBanner(nowPlay.advertising!!.map { b ->
                                        BannerModel(
                                            b.imageUrl ?: "",
                                            b.description ?: ""
                                        )
                                    })
                                bindBanner(stationBanner)
                                binding.banner.visibility = View.VISIBLE
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
                                            ) as Reader?
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
