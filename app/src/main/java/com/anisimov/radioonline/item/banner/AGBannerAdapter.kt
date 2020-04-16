package com.anisimov.radioonline.item.banner

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.anisimov.radioonline.item.models.BannerModel

class AGBannerAdapter(fm: FragmentManager, private val itemArray: ArrayList<BannerModel>) :
    FragmentStatePagerAdapter(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var onItemClickListener: OnItemClickListener? = null

    override fun getCount(): Int = itemArray.size

    override fun getItem(position: Int): Fragment {
        return AGBannerFragment(itemArray[position], onItemClickListener, position)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, v: View?)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }
}