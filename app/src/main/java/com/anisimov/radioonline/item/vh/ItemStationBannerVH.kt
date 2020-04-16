package com.anisimov.radioonline.item.vh

import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.anisimov.radioonline.item.AGViewHolder
import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.banner.AGBannerAdapter
import com.anisimov.radioonline.item.banner.BannerClickListener
import com.anisimov.radioonline.item.models.BannerModel
import com.anisimov.radioonline.item.models.StationBanner
import kotlinx.android.synthetic.main.item_station_banner.view.*
import kotlinx.coroutines.*

class ItemStationBannerVH(val view: View, private val fm: FragmentManager?) : AGViewHolder(view),
    AGBannerAdapter.OnItemClickListener {

    private var bannerAnimation: Job? = null
    private lateinit var bannerAdapter: AGBannerAdapter
    private lateinit var banner: ViewPager
    private val bannerArray = arrayListOf<BannerModel>()

    override fun bind(item: Item, position: Int) {
        bannerArray.addAll((item as StationBanner).bannerArray)
        banner = view.banner

        fm?.let {
            bannerAdapter = AGBannerAdapter(it, bannerArray)
            bannerAdapter.setOnItemClickListener(this)

            banner.apply {
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
        }
    }

    private fun runBannerAnimation(): Job {
        return GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                delay(10000)
                banner.apply {
                    if (currentItem < childCount - 1) currentItem++ else currentItem = 1
                }
            }
        }
    }

    override fun onItemClick(position: Int, v: View?) {
        BannerClickListener.onItemClick(bannerArray[position], position, v)
    }
}