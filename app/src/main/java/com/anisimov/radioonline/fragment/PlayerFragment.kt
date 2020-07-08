package com.anisimov.radioonline.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.os.Bundle
import android.view.*
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.MainActivity
import com.anisimov.radioonline.R
import com.anisimov.radioonline.databinding.FragmentPlayerBinding
import com.anisimov.radioonline.interfaces.IOnActivityStateChange
import com.anisimov.radioonline.interfaces.IOnKeyDownEvent
import com.anisimov.radioonline.interfaces.IOnKeyDownListener
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.item.models.TrackModel
import com.anisimov.radioonline.radio.IOnPlayListener
import com.anisimov.radioonline.radio.RadioService
import com.anisimov.radioonline.util.setImageFromUrl
import com.anisimov.requester.getSrcFromEntityCoverImage
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers as D
import kotlinx.coroutines.GlobalScope as GS

class PlayerFragment(private val service: RadioService) : Fragment(),
    SeekBar.OnSeekBarChangeListener, IOnActivityStateChange,
    IOnKeyDownListener, View.OnTouchListener {

    private lateinit var binding: FragmentPlayerBinding
    private var audio: AudioManager? = null
    private var station: StationModel? = null
    private var trackUpdater: Job? = null
    private var destroy = false

    private var play: Boolean = false

    var showInfo = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_player, container, false
            )
            audio = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            binding.apply {
                backButtonS.setOnTouchListener(this@PlayerFragment)
                playButtonS.setOnTouchListener(this@PlayerFragment)
                forwardButtonS.setOnTouchListener(this@PlayerFragment)
                playlistButton.setOnClickListener {
                    station?.let {
                        childFragmentManager.beginTransaction().replace(
                            R.id.stationFragmentHolder,
                            StationInfoFragment(it, service),
                            STATION_TAG
                        ).commit()
                        showInfo = true
                    }
                }
                audio?.let {
                    volumeSeekBar.max = it.getStreamMaxVolume(STREAM_MUSIC)
                }
                volumeSeekBar.setOnSeekBarChangeListener(this@PlayerFragment)
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_placeholder
                )?.let {
                    albumCover.setImageBitmap(it)
                    Blurry.with(context).radius(10).sampling(8)
                        .color(Color.argb(100, 100, 100, 100))
                        .async().from(it).into(backGround)
                }
            }
            trackUpdater = makeTrackUpdater()
        }
        return binding.root
    }

    var lastStation: TrackModel? = null

    private fun makeTrackUpdater(): Job {
        return GS.launch(D.IO) {
            while (!destroy) {
                station?.let {
                    if (lastStation?.artist != it.track?.artist || lastStation?.cover != it.track?.cover || lastStation?.title != it.track?.title) {
                        if (it.track?.cover?.contains("200x200") != true && it.track?.cover?.contains("400x400") != true && it.track?.cover?.contains("bage") != true) {
                            it.track?.cover = getSrcFromEntityCoverImage("${it.track?.artist} ${it.track?.title}", false)
                        }
                        launch(D.Main) {
                            updateSoundInfo(it.track)
                            lastStation = it.track
                        }
                    }
                }
                delay(5000)
            }
        }
    }

    fun fillData(station: StationModel?) {
        this.station = station
        if (!station?.name.isNullOrEmpty()) binding.stationName.text = station?.name
        play = service.isPlaying

        station?.let { updateSoundInfo(it.track) }

        setPlayState()
    }

    private fun updateSoundInfo(track: TrackModel?) {
        binding.apply {
            track?.let {
                albumCover.setImageFromUrl(
                    station?.imageUrl,
                    blurTo = backGround)
                albumCover.setImageFromUrl(
                    it.cover.replace("200x200", "400x400"),
                    blurTo = backGround
                )
                trackName.text = it.title
                artistName.text = it.artist
            }
        }
    }

    override fun onBackPressed() {
        hideStationInfo()
    }

    override fun onHide() {
        hideStationInfo()
    }

    private fun hideStationInfo() {
        childFragmentManager.apply {
            findFragmentByTag(STATION_TAG)?.let { beginTransaction().remove(it).commit() }
            showInfo = false
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        audio?.setStreamVolume(STREAM_MUSIC, progress, 0)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?) {
        binding.volumeSeekBar.progress = audio?.getStreamVolume(STREAM_MUSIC) ?: 0
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let { e ->
            v?.apply {
                when (e.action) {
                    ACTION_DOWN ->
                        v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start()
                    ACTION_UP -> {
                        when (v) {
                            binding.backButtonS -> {
                                (activity as MainActivity).playNext(false)
                                fillData(service.station)
                            }
                            binding.playButtonS -> {
                                if (service.station != null) if (play) service.pause() else service.play()
                                else {
                                    (activity as MainActivity).playNext(true)
                                    fillData(service.station)
                                }

                            }
                            binding.forwardButtonS -> {
                                (activity as MainActivity).playNext(true)
                                fillData(service.station)
                            }
                        }
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    }
                }
            }
        }
        return true
    }

    private fun setPlayState() {
        binding.playButtonS.foreground = resources.getDrawable(
            if (play) R.drawable.ic_pause else R.drawable.ic_play, null
        )

        if (play) {
            service.station?.let {
                if (it != station) fillData(it)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onResume() {
        super.onResume()
        service.subscribe(playListener)
        (activity as? IOnKeyDownEvent)?.subscribeOnKeyDownEvent(this)

        binding.volumeSeekBar.progress = audio?.getStreamVolume(STREAM_MUSIC) ?: 0
        fillData(service.station)
    }

    override fun onDestroy() {
        super.onDestroy()
        service.unsubscribe(playListener)
        destroy = true
        trackUpdater?.cancel()
        (activity as? IOnKeyDownEvent)?.unsubscribeOnKeyDownListener(this)
    }

    private val playListener = object : IOnPlayListener {
        override fun onPlay(playState: Boolean) {
            play = playState
            setPlayState()
        }

        override fun onStop() {
            play = false
            setPlayState()
        }

        override fun onStationChange(station: StationModel) {
            fillData(station)
        }
    }
}
