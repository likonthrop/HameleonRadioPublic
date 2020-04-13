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

class PlayerFragment(private val service: RadioService) : Fragment(),
    SeekBar.OnSeekBarChangeListener, OnKeyDownListener, View.OnTouchListener {

    private lateinit var binding: FragmentPlayerBinding
    private var audio: AudioManager? = null
    private var job: Job? = null
    private var station: StationModel? = null

    private var play: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)
            audio = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            binding.apply {
                likeButton.setOnTouchListener(this@PlayerFragment)
                dislikeButton.setOnTouchListener(this@PlayerFragment)
                backButton.setOnTouchListener(this@PlayerFragment)
                playButton.setOnTouchListener(this@PlayerFragment)
                forwardButton.setOnTouchListener(this@PlayerFragment)
                audio?.let {
                    volumeSeekBar.max = it.getStreamMaxVolume(STREAM_MUSIC)
                }
                volumeSeekBar.setOnSeekBarChangeListener(this@PlayerFragment)
            }
        }
        return binding.root
    }

    fun fillData(station: StationModel?) {
        this.station = station
        binding.stationName.text = station?.name
        play = service.isPlaying
        updateSoundInfo(
            station?.cover ?: "https://s1.tchkcdn.com/g-0wJ5X7jh3PTvfCy5G3rHKQ/1/373756/660x480/f/0/6f2_snimok_ekrana_2019_03_29_v_12.53.48.png",
            "bad guy",
            "Billie Eilish"
        )
        setPlayState()
    }

    fun updateSoundInfo(cover: String?, track: String, artist: String) {
        binding.apply {
            albumCover.setImageFromUrl(cover, blurTo = backGround)
            trackName.text = track
            artistName.text = artist
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
                            binding.likeButton -> showLDButtons(false)
                            binding.dislikeButton -> showLDButtons(false)

                            binding.backButton -> {
                                (activity as MainActivity).playNext(false)
                                fillData(service.station)
                            }
                            binding.playButton -> {
                                if (service.station != null) if (play) service.pause() else service.play()
                                else {
                                    (activity as MainActivity).playNext(true)
                                    fillData(service.station)
                                }

                            }
                            binding.forwardButton -> {
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

    private fun showLDButtons(show: Boolean) {
        GS.launch(D.Main) {
            binding.likeButton.animate().translationX(if (show) 0f else -100f).setDuration(100)
                .start()
            binding.dislikeButton.animate().translationX(if (show) 0f else 100f).setDuration(100)
                .start()
            delay(100)
            binding.likeButton.visibility = if (show) View.VISIBLE else View.INVISIBLE
            binding.dislikeButton.visibility = if (show) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setPlayState() {
        binding.playButton.background = resources.getDrawable(
            if (play) R.drawable.ic_pause else R.drawable.ic_play, null
        )

        if (play) {
            service.station?.let {
                if (it != station) fillData(it)
            }
            job?.cancel()
        }
        else job = makeWhiteAnimation()
    }

    private fun makeWhiteAnimation(): Job {
        return GS.launch(D.Main) {
            val scale = initAnim(binding.playButton, SF().setStiffness(1000f).setDampingRatio(.2f))
            while (!play) {
                delay(10000)
                if (play) break
                binding.playButton.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start()
                delay(100)
                scale.forEach { it.apply { animateToFinalPosition(1f) } }
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onResume() {
        super.onResume()
        service.subscribe(playListener)
        (activity as? OnKeyDownEvent)?.subscribeOnKeyDownEvent(this)

        binding.volumeSeekBar.progress = audio?.getStreamVolume(STREAM_MUSIC) ?: 0
        fillData(service.station)
        showLDButtons(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        service.unsubscribe(playListener)
        (activity as? OnKeyDownEvent)?.unsubscribeOnKeyDownListener(this)
    }

    private fun initAnim(view: View, force: SF) = listOf(
        SA(view, SCALE_X).setSpring(force), SA(view, SCALE_Y).setSpring(force)
    )

    private val playListener = object : OnPlayListener {
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
