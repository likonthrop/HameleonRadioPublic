package com.anisimov.radioonline.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import com.anisimov.radioonline.R
import com.anisimov.radioonline.databinding.FragmentStationBinding
import com.anisimov.radioonline.interfaces.IOnActivityStateChange
import com.anisimov.radioonline.item.AGAdapterRV
import com.anisimov.radioonline.item.ITEM_STATION_BANNER
import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.banner.BannerClickListener
import com.anisimov.radioonline.item.itemhelper.ItemMoveCallback
import com.anisimov.radioonline.item.models.BannerModel
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.item.models.SongModel
import com.anisimov.radioonline.radio.IOnPlayListener
import com.anisimov.radioonline.radio.RadioService
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

private const val STATION_TAG = "stationFragment"

class StationFragment(
    private val service: RadioService,
    private val itemList: ArrayList<Item> = arrayListOf()
) : Fragment(), IOnActivityStateChange,
    AGAdapterRV.OnItemClickListener, BannerClickListener.OnItemClickListener,
    AGAdapterRV.OnRawMoveListener {

    private lateinit var binding: FragmentStationBinding
    private lateinit var adapter: AGAdapterRV
    private var trackUpdater: Job? = null

    private var touchHelper: ItemTouchHelper? = null

    private var stationInfoFragment: StationInfoFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_station, container, false)

            val hasBanner = itemList.any { it.objectType == ITEM_STATION_BANNER }

            val sp = context?.getSharedPreferences("station_position_sp", Context.MODE_PRIVATE)
            val structure = sp?.getString("structure", null)?.split(",")

            structure?.forEachIndexed { i, o ->
                val j = if (hasBanner) i + 1 else i
                if (j < itemList.size) {
                    val position = itemList.indexOfFirst { (it as? StationModel)?.id == o.toLong() }
                    if (position == -1) {
                        structure.drop(j)

                        sp.edit()?.apply {
                            putString("structure", structure.joinToString(","))
                            apply()
                        }
                    } else {
                        if (position != j) Collections.swap(itemList, position, j)
                    }
                }
            }

            itemList.forEachIndexed { i, o -> (o as? StationModel)?.index = i }

            adapter = AGAdapterRV(
                itemList,
                childFragmentManager
            )

            touchHelper = ItemTouchHelper(ItemMoveCallback(adapter))
            touchHelper?.attachToRecyclerView(binding.recycle)

            adapter.setOnItemClickListener(this)
            adapter.setOnRawMove(this)
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
        return GlobalScope.launch(Dispatchers.IO) {
            while (adapter.itemCount > 0) {
                itemList.forEachIndexed { i, item ->
                    (item as? StationModel)?.let { station ->
                        station.url?.let { url ->
                            try {
                                val meta = BufferedReader(
                                    InputStreamReader(
                                        URL("${url.split("?")[0]}.xspf")
                                            .openStream()
                                    )
                                ).readLines().toString().replace("amp;", "")
                                launch(Dispatchers.Main) {
                                    val metaSplit =
                                        meta.substringAfter("<title>").substringBefore("</title>")
                                            .toLowerCase().split(" - ")
                                            .map { it.trim().capitalize() }
                                    if (metaSplit.size == 2) {
                                        if (item.song == null || item.song!!.artistName != metaSplit[0] || item.song!!.trackName != metaSplit[1]) {
                                            item.song = SongModel(
                                                metaSplit[0],
                                                metaSplit[1],
                                                item.getCover()
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
        when (v.id) {
            R.id.playButton -> {
                for ((i, s) in itemList.withIndex()) {
                    s as? StationModel ?: continue
                    if (i != position && s.current) {
                        s.current = false
                        s.enable = false
                        s.loading = false
                        adapter.notifyItemChanged(i, s)
                    }
                }

                item.apply {
                    loading = true
                    current = true
                }

                service.withStation(item)
            }

            R.id.progressButton -> {
                if (item.enable && !item.loading) {
                    item.enable = false
                    service.pause()
                }
            }

            R.id.pauseButton -> {
                if (!item.enable) {
                    item.enable = true
                    service.play()
                }
            }

            R.id.stationRoot -> {
                childFragmentManager.beginTransaction().replace(R.id.stationFragmentHolder, StationInfoFragment(item), STATION_TAG).commit()
            }
        }
    }

    override fun onHide() {
        hideStationInfo()
    }

    override fun onBackPressed() {
        hideStationInfo()
    }

    private fun hideStationInfo() {
        childFragmentManager.apply {
            findFragmentByTag(STATION_TAG)?.let { beginTransaction().remove(it).commit() }
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
            Toast.makeText(context, item.ling, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMove() {
        val sp = context?.getSharedPreferences("station_position_sp", Context.MODE_PRIVATE)
        sp?.edit()?.apply {
            val structure =
                itemList.filterIsInstance<StationModel>().map { it.id }.joinToString(",")
            putString("structure", structure)
            apply()
        }
    }
}
