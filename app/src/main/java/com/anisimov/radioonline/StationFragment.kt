package com.anisimov.radioonline

import android.os.Bundle
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
import com.anisimov.radioonline.radio.OnPlayListener
import com.anisimov.radioonline.radio.RadioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StationFragment(
    private val service: RadioService,
    private val itemList: ArrayList<Item> = arrayListOf()
) : Fragment(),
    AGAdapterRV.OnItemClickListener, BannerClickListener.OnItemClickListener {

    private lateinit var binding: FragmentStationBinding
    private lateinit var adapter: AGAdapterRV

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
        BannerClickListener.unsubscribe(this)
    }

    fun showUI(show: Boolean) {
        binding.root.visibility = if (show) View.VISIBLE else View.GONE
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
                adapter.notifyItemChanged(i)
            }
        } else {
            val i = if (hasBanner) 1 else 0
            service.withStation(itemList[i])
            itemList[i].apply {
                if (this !is StationModel) return
                current = true
                enable = true
                adapter.notifyItemChanged(i)
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
                adapter.notifyItemChanged(i)
            }
        } else {
            val i = itemList.size - 1
            service.withStation(itemList[i])
            itemList[i].apply {
                if (this !is StationModel) return
                current = true
                enable = true
                adapter.notifyItemChanged(i)
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
                adapter.notifyItemChanged(i)
            }
        }

        if ((itemList[position] as? StationModel)?.enable == true && (itemList[position] as? StationModel)?.loading == false) {
            (itemList[position] as StationModel).enable = false
            adapter.notifyItemChanged(position)
            service.pause()
        } else {
            itemList[position].apply {
                if (this !is StationModel) return
                loading = true
                current = true
            }
            adapter.notifyItemChanged(position)
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
                    adapter.notifyItemChanged(i)
                }
            }
        }

        override fun onStop() {
            for ((i, s) in itemList.withIndex()) {
                s as? StationModel ?: continue
                if (s.current) {
                    s.current = false
                    s.enable = false
                    adapter.notifyItemChanged(i)
                }
            }
        }

        override fun onLoad(inProgress: Boolean) {
            for ((i, s) in itemList.withIndex()) {
                s as? StationModel ?: continue
                if (s.current || s.loading) {
                    s.loading = inProgress
                    adapter.notifyItemChanged(i)
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
