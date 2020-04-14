package com.anisimov.radioonline.radio

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaMetadataRetriever
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.Surface
import com.anisimov.radioonline.BuildConfig
import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.radio.PlaybackStatus.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.lang.Exception

const val ACTION_PLAY = "com.anisimov.radioonline.player.ACTION_PLAY"
const val ACTION_PAUSE = "com.anisimov.radioonline.player.ACTION_PAUSE"
const val ACTION_STOP = "com.anisimov.radioonline.player.ACTION_STOP"

class RadioService : Service(), EventListener, OnAudioFocusChangeListener {

    private val iBinder: IBinder = LocalBinder()
    var isPlaying = false
        private set
    private var onGoingCall = false

    private lateinit var exoPlayer: SimpleExoPlayer
    var station: StationModel? = null
        private set
    private lateinit var wifiLock: WifiManager.WifiLock
    private lateinit var handler: Handler

    private var audioManager: AudioManager? = null
    private var strAppName: String? = null
    private var strLiveBroadcast: String? = null
    private var telephonyManager: TelephonyManager? = null
    private var notificationManager: MediaNotificationManager? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    lateinit var mediaSession: MediaSessionCompat

    var status: String? = null

    inner class LocalBinder : Binder() {
        val service: RadioService = this@RadioService
    }

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pause()
        }
    }

    private val phoneStateListener: PhoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == TelephonyManager.CALL_STATE_OFFHOOK
                || state == TelephonyManager.CALL_STATE_RINGING
            ) {
                if (!isPlaying) return
                onGoingCall = true
                stop()
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (!onGoingCall) return
                onGoingCall = false
                play()
            }
        }
    }

    private val mediasSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            override fun onPause() {
                super.onPause()
                pause()
            }

            override fun onStop() {
                super.onStop()
                stop()
                notificationManager?.cancelNotify()
            }

            override fun onPlay() {
                super.onPlay()
                play()
            }
        }

    override fun onCreate() {
        super.onCreate()

        onGoingCall = false

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        notificationManager = MediaNotificationManager(this)

        wifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, "mcScPAmpLock")

        mediaSession = MediaSessionCompat(this, javaClass.simpleName)
        transportControls = mediaSession.controller.transportControls
        mediaSession.isActive = true
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "...")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, strAppName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, strLiveBroadcast)
                .build()
        )
        mediaSession.setCallback(mediasSessionCallback)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)

        handler = Handler()
        val bandwidthMeter = DefaultBandwidthMeter()
        val trackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(trackSelectionFactory)

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        exoPlayer.addListener(this)
        registerReceiver(becomingNoisyReceiver, IntentFilter(ACTION_AUDIO_BECOMING_NOISY))

        status = IDLE
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            if (TextUtils.isEmpty(action)) return START_NOT_STICKY

            audioManager?.requestAudioFocus(this@RadioService, STREAM_MUSIC, AUDIOFOCUS_GAIN)
                ?.let { if (it != AUDIOFOCUS_REQUEST_GRANTED) stop(); return@let START_NOT_STICKY }

            when (action) {
                ACTION_PLAY -> transportControls?.play()
                ACTION_PAUSE -> transportControls?.pause()
                ACTION_STOP -> transportControls?.stop()
            }
        }

        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (status.equals(IDLE)) stopSelf()
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
    }

    override fun onDestroy() {
        pause()

        exoPlayer.release()
        exoPlayer.removeListener(this)

        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)

        notificationManager?.cancelNotify()

        mediaSession.release()

        unregisterReceiver(becomingNoisyReceiver)

        super.onDestroy()
    }

    fun play() {
        if (!::exoPlayer.isInitialized && station == null) return

        isPlaying = true
        val uri = Uri.parse(station?.url)
        val mediaSource = extractorMediaSource(uri)



        exoPlayer.apply {
            prepare(mediaSource)
            playWhenReady = true
        }
    }

    fun pause() {
        if (!::exoPlayer.isInitialized) return
        isPlaying = false
        exoPlayer.apply {
            playWhenReady = false
            playbackState
        }
        audioManager?.abandonAudioFocus(this)
        wifiLockRelease()
    }

    fun stop() {
//        if (!::exoPlayer.isInitialized) return
//        isPlaying = false
//        exoPlayer.release()
//        mediaSession.release()
//        audioManager?.abandonAudioFocus(this)
//        wifiLockRelease()
//        subscribes.forEach { it.onStop() }
        pause()
    }

    private fun extractorMediaSource(uri: Uri?): ExtractorMediaSource {
        val userAgent = Util.getUserAgent(this, BuildConfig.APPLICATION_ID)
        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, userAgent))
            .createMediaSource(uri)
    }

    private val subscribes = arrayListOf<OnPlayListener>()

    fun subscribe(listenerOn: OnPlayListener): RadioService {
        if (!subscribes.contains(listenerOn)) subscribes.add(listenerOn)
        return this
    }

    fun unsubscribe(listenerOn: OnPlayListener) {
        if (subscribes.contains(listenerOn)) subscribes.remove(listenerOn)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AUDIOFOCUS_GAIN -> {
                exoPlayer.volume = 0.8f
                play()
            }
            AUDIOFOCUS_LOSS -> stop()
            AUDIOFOCUS_LOSS_TRANSIENT -> if (isPlaying) pause()
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (isPlaying) exoPlayer.volume = 0.1f
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        status = when (playbackState) {
            STATE_BUFFERING -> {
                subscribes.forEach { it.onLoad(true) }
                LOADING
            }
            STATE_ENDED -> {
                subscribes.forEach { it.onLoad(false) }
                STOPPED
            }
            STATE_IDLE -> {
                subscribes.forEach { it.onLoad(false) }
                IDLE
            }
            STATE_READY -> {
                subscribes.forEach { it.onPlay(isPlaying) }
                subscribes.forEach { it.onLoad(false) }
                if (playWhenReady) PLAYING else PAUSED
            }
            else -> {
                subscribes.forEach { it.onLoad(false) }
                IDLE
            }
        }
        if (status != IDLE) {
            notificationManager!!.startNotify(status!!)
        } else transportControls?.stop()

        EventBus.getDefault().post(status)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}
    override fun onSeekProcessed() {}
    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
        println("trackGroups = [${trackGroups}], trackSelections = [${trackSelections}]")
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        EventBus.getDefault().post(PlaybackStatus.ERROR)
        subscribes.forEach { it.onStop() }
    }

    override fun onLoadingChanged(isLoading: Boolean) {}
    override fun onPositionDiscontinuity(reason: Int) {}
    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}
    override fun onBind(intent: Intent?): IBinder? {
        return iBinder
    }


    private fun wifiLockRelease() {
        if (wifiLock.isHeld) wifiLock.release()
    }

    fun withStation(station: Item) {
        this.station = station as? StationModel ?: return
        play()
    }

}