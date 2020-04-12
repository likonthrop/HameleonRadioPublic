package com.anisimov.radioonline

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.*
import android.os.Bundle
import android.view.*
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.dynamicanimation.animation.DynamicAnimation.SCALE_X
import androidx.dynamicanimation.animation.DynamicAnimation.SCALE_Y
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.databinding.FragmentMoreBinding
import com.anisimov.radioonline.databinding.FragmentPlayerBinding
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.radio.OnPlayListener
import com.anisimov.radioonline.radio.RadioService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.dynamicanimation.animation.SpringAnimation as SA
import androidx.dynamicanimation.animation.SpringForce as SF
import kotlinx.coroutines.Dispatchers as D
import kotlinx.coroutines.GlobalScope as GS

class MoreFragment() : Fragment(){

    private lateinit var binding: FragmentMoreBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_more, container, false)
        }
        return binding.root
    }

    fun fillData(station: StationModel?) {
    }
}
