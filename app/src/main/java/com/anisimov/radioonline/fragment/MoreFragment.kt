package com.anisimov.radioonline.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.R
import com.anisimov.radioonline.databinding.FragmentMoreBinding

class MoreFragment : Fragment(){

    private lateinit var binding: FragmentMoreBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_more, container, false)
        }
        return binding.root
    }
}
