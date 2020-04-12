package com.anisimov.radioonline.item.banner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.R
import com.anisimov.radioonline.item.models.BannerModel
import com.anisimov.radioonline.setImageFromUrl

class AGBannerFragment(private val model: BannerModel, private val onItemClickListener: AGBannerAdapter.OnItemClickListener?, private val position: Int) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View  {
        val view = inflater.inflate(R.layout.banner_fragment_item, container, false) as ImageView

        view.setImageFromUrl(model.image)

        view.setOnClickListener { onItemClickListener?.onItemClick(position, view) }

        return view
    }

    fun getLink(): String = model.ling
}