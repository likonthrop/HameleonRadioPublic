package com.anisimov.radioonline

import android.content.*
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import android.view.KeyEvent.KEYCODE_VOLUME_UP
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.databinding.ActivityMainBinding
import com.anisimov.radioonline.fragment.PlayerFragment
import com.anisimov.radioonline.fragment.StationFragment
import com.anisimov.radioonline.interfaces.IOnActivityStateChange
import com.anisimov.radioonline.interfaces.IOnKeyDownEvent
import com.anisimov.radioonline.interfaces.IOnKeyDownListener
import com.anisimov.radioonline.item.models.Item
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.radio.RadioService
import com.anisimov.radioonline.util.EstimateTimer
import com.anisimov.requester.HttpResponseCallback
import com.anisimov.requester.generateMode
import com.anisimov.requester.generateModeList
import com.anisimov.requester.getHttpResponse
import com.anisimov.requester.models.Root
import com.anisimov.requester.models.Station
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt
import com.anisimov.requester.r.getHttpResponse as getRHttpResponse
import com.anisimov.requester.r.models.Station as RStation

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    IOnKeyDownEvent {

    private lateinit var binding: ActivityMainBinding
    private var serviceBound = false
    private lateinit var service: RadioService
    private var audio: AudioManager? = null
    private lateinit var playerFragment: PlayerFragment

    private var timer: Timer? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, s: IBinder?) {
            serviceBound = true
            service = (s as RadioService.LocalBinder).service
            service.let {
                EventBus.getDefault().post(it.status)

                var request = ""
                sp?.getLong("authorize", 0)?.let { id ->
                    if (id > 0) request += "?request={\"id\":$id}"
                }
                getHttpResponse(request, object : HttpResponseCallback {
                    override fun onResponse(response: String) {
                        val stations = generateMode<Root>(response).stations

                        CoroutineScope(Dispatchers.Main).launch {
                            addFragment(
                                StationFragment(
                                    it,
                                    genStations(stations)
                                ), true
                            )
                            playerFragment = PlayerFragment(it)
                            addFragment(
                                playerFragment
                            )
                            binding.progressBar.visibility = View.GONE
                            binding.bottomNavigation.selectedItemId = R.id.navigation_station
                        }
                    }

                    override fun onError(e: String?) {
                        getRHttpResponse("stations", object : HttpResponseCallback {
                            override fun onResponse(response: String) {
                                val stations = generateModeList<RStation>(response)

                                CoroutineScope(Dispatchers.Main).launch {
                                    addFragment(
                                        StationFragment(
                                            it,
                                            genRStations(stations)
                                        ), true
                                    )
                                    playerFragment = PlayerFragment(it)
                                    addFragment(
                                        playerFragment
                                    )
                                    binding.progressBar.visibility = View.GONE
                                    binding.bottomNavigation.selectedItemId =
                                        R.id.navigation_station
                                }
                            }
                        })
                    }
                })
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    private fun genRStations(stationsList: List<RStation>): ArrayList<Item> {
        val list = arrayListOf<Item>()
        val st = stationsList.map {
            StationModel(
                id = it.id,
                name = it.name ?: "",
                imageUrl = "https://player.stvradio.online/static/icons/production/bage_${it.shortcode}.jpg",
                link = it.listenUrl
            )
        }
            .toTypedArray()
        list.addAll(st)
        return list
    }

    fun genStations(stationsList: List<Station>): ArrayList<Item> {
        val list = arrayListOf<Item>()
        val st = stationsList.map {
            StationModel(
                id = it.id,
                name = it.name ?: "",
                imageUrl = it.imageUrl,
                link = it.link
            )
        }
            .toTypedArray()
        list.addAll(st)
        return list
    }

    fun playNext(forward: Boolean) {
        val f =
            supportFragmentManager.findFragmentByTag(StationFragment::class.java.simpleName) as StationFragment
        if (forward) f.playForward() else f.playBelow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)

        //Запускает таймер (30 минут) для отображения диалога с просьбой оценить приложение в маркете
        timer = EstimateTimer(this, TimeUnit.MINUTES.toMillis(30))
    }

    var sp: SharedPreferences? = null

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, RadioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        service.stop()
        service.stopNotify()
        timer?.cancel()
    }

    private fun addFragment(fragment: Fragment, _show: Boolean = false) {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentHolder, fragment, fragment.javaClass.simpleName)
            if (_show) show(fragment) else hide(fragment)
            commit()
        }
    }

    private var backPressTime = 0L
    private lateinit var backPressToast: Toast
    override fun onBackPressed() {
        if (playerFragment.showInfo) {
            supportFragmentManager.fragments.forEach { (it as? IOnActivityStateChange)?.onBackPressed() }
            return
        }
        if (backPressTime + 2000 > System.currentTimeMillis()) {
            backPressToast.cancel()
            super.onBackPressed()
        } else {
            backPressToast = Toast.makeText(this, "Нажмите еще раз для выхода", Toast.LENGTH_SHORT)
            backPressToast.show()
        }
        backPressTime = System.currentTimeMillis()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        when (item.itemId) {
            R.id.navigation_station -> changeFragment(StationFragment::class.java)
            R.id.navigation_player -> changeFragment(PlayerFragment::class.java)
//            R.id.navigation_more -> changeFragment(MoreFragment::class.java)
        }
        return false
    }

    fun showPlayer() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_player
        changeFragment(PlayerFragment::class.java)
    }

    fun <T> changeFragment(fragment: T) {
        supportFragmentManager.apply {
            fragments.forEach {
                if (it::class.java == fragment) beginTransaction().show(it).commit()
                else beginTransaction().hide(it).commit()
                (it as? IOnActivityStateChange)?.onHide()
            }
        }
    }

    private val keyDownListenerPool = arrayListOf<IOnKeyDownListener>()

    override fun subscribeOnKeyDownEvent(onKeyDownListener: IOnKeyDownListener) {
        if (!keyDownListenerPool.contains(onKeyDownListener)) keyDownListenerPool.add(
            onKeyDownListener
        )
    }

    override fun unsubscribeOnKeyDownListener(onKeyDownListener: IOnKeyDownListener) {
        if (keyDownListenerPool.contains(onKeyDownListener)) keyDownListenerPool.remove(
            onKeyDownListener
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KEYCODE_VOLUME_UP, KEYCODE_VOLUME_DOWN -> {
                when (keyCode) {
                    KEYCODE_VOLUME_UP -> audio?.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, 0
                    )
                    KEYCODE_VOLUME_DOWN -> audio?.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, 0
                    )
                }
                keyDownListenerPool.forEach {
                    it.onKeyDown(keyCode, event)
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
