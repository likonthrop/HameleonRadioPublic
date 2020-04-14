package com.anisimov.radioonline

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import com.anisimov.radioonline.databinding.FragmentStationBinding
import com.anisimov.radioonline.item.AGAdapterRV
import com.anisimov.radioonline.item.ITEM_STATION_BANNER
import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.banner.AGBannerAdapter
import com.anisimov.radioonline.item.banner.BannerClickListener
import com.anisimov.radioonline.item.itemhelper.ItemMoveCallback
import com.anisimov.radioonline.item.models.BannerModel
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.item.models.TrackModel
import com.anisimov.radioonline.radio.OnPlayListener
import com.anisimov.radioonline.radio.RadioService
import kotlinx.android.synthetic.main.fragment_station.*
import kotlinx.android.synthetic.main.item_station.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class StationFragment(
    private val service: RadioService,
    private val itemList: ArrayList<Item> = arrayListOf()
) : Fragment(),
    AGAdapterRV.OnItemClickListener, BannerClickListener.OnItemClickListener {

    private lateinit var binding: FragmentStationBinding
    private lateinit var adapter: AGAdapterRV
    private var trackUpdater: Job? = null

    private var touchHelper: ItemTouchHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_station, container, false)
            adapter = AGAdapterRV(
                itemList,
                childFragmentManager,
                itemList.any { it.objectType == ITEM_STATION_BANNER })

            touchHelper = ItemTouchHelper(ItemMoveCallback(adapter))
            touchHelper?.attachToRecyclerView(binding.recycle)

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

    private fun makeTrackUpdater(): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            while (adapter.itemCount > 0) {
                itemList.forEachIndexed() { i, item ->
                    (item as? StationModel)?.let { station ->
                        station.url?.let { url ->
                            try {
                                val meta = BufferedReader(InputStreamReader(URL(
                                    "$url.xspf")
                                    .openStream())).readLines().toString().replace("amp;", "")
                                launch(Dispatchers.Main) {
                                    val metaSplit = meta.substringAfter("<title>").substringBefore("</title>")
                                        .toLowerCase().split(" - ").map { it.capitalize() }
                                    if (metaSplit.size == 2) {
                                        if (item.track == null || item.track!!.artistName != metaSplit[0] || item.track!!.trackName != metaSplit[1]) {
                                            item.track = TrackModel(metaSplit[0], metaSplit[1], item.cover ?: "")
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
//                adapter.notifyItemChanged(i)
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
//                adapter.notifyItemChanged(i)
            }
        } else {
            val i = if (hasBanner) 1 else 0
            service.withStation(itemList[i])
            itemList[i].apply {
                if (this !is StationModel) return
                current = true
                enable = true
//                adapter.notifyItemChanged(i)
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
//                adapter.notifyItemChanged(i)
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
//                adapter.notifyItemChanged(i)
            }
        } else {
            val i = itemList.size - 1
            service.withStation(itemList[i])
            itemList[i].apply {
                if (this !is StationModel) return
                current = true
                enable = true
//                adapter.notifyItemChanged(i)
            }
        }
    }

    override fun onItemClick(position: Int, type: Int, v: View?) {
        for ((i, s) in itemList.withIndex()) {
            s as? StationModel ?: continue
            if (i != position && s.current) {
                s.current = false
                s.enable = false
                s.loading = false
                adapter.notifyItemChanged(i, s)
            }
        }

        if ((itemList[position] as? StationModel)?.enable == true && (itemList[position] as? StationModel)?.loading == false) {
            (itemList[position] as StationModel).enable = false
//            adapter.notifyItemChanged(position)
            service.pause()
        } else {
            itemList[position].apply {
                if (this !is StationModel) return
                loading = true
                current = true
            }
//            adapter.notifyItemChanged(position)
            service.withStation(itemList[position])
        }

        (activity as MainActivity).showPlayer()
    }

    private val playListener = object : OnPlayListener {
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
            Toast.makeText(context, item.ling, Toast.LENGTH_SHORT).show()
        }
    }

}
