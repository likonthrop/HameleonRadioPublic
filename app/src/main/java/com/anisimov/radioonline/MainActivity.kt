package com.anisimov.radioonline

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import android.view.KeyEvent.KEYCODE_VOLUME_UP
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.anisimov.radioonline.databinding.ActivityMainBinding
import com.anisimov.radioonline.fragment.MoreFragment
import com.anisimov.radioonline.fragment.PlayerFragment
import com.anisimov.radioonline.fragment.StationFragment
import com.anisimov.radioonline.interfaces.IOnActivityStateChange
import com.anisimov.radioonline.interfaces.IOnKeyDownEvent
import com.anisimov.radioonline.interfaces.IOnKeyDownListener
import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.models.StationBanner
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.radio.RadioService
import com.anisimov.requester.HttpResponseCallback
import com.anisimov.requester.generateMode
import com.anisimov.requester.getHttpResponse
import com.anisimov.requester.models.Root
import com.anisimov.requester.models.Station
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    IOnKeyDownEvent {

    private lateinit var binding: ActivityMainBinding
    private var serviceBound = false
    private lateinit var service: RadioService
    private var audio: AudioManager? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, s: IBinder?) {
            serviceBound = true
            service = (s as RadioService.LocalBinder).service
            service.let {
                EventBus.getDefault().post(it.status)

                getHttpResponse("", object : HttpResponseCallback {
                    override fun onResponse(response: String) {
                        val stations = generateMode<Root>(response).stations

                        CoroutineScope(Dispatchers.Main).launch {
                            addFragment(
                                StationFragment(
                                    it,
                                    genStations(stations)
                                ), true)
                            addFragment(
                                PlayerFragment(
                                    it
                                )
                            )
                            addFragment(MoreFragment())

                            binding.bottomNavigation.selectedItemId = R.id.navigation_station
                        }
                    }
                })
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    fun genStations(stationsList: List<Station>): ArrayList<Item> {
        val list = arrayListOf<Item>()
        val st = stationsList.map { StationModel(id = it.id, name = it.name ?: "", imageUrl = it.imageUrl, link = it.link) }
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
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, RadioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun addFragment(fragment: Fragment, _show: Boolean = false) {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentHolder, fragment, fragment.javaClass.simpleName)
            if (_show) show(fragment) else hide(fragment)
            commit()
        }
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach { (it as? IOnActivityStateChange)?.onBackPressed() }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        when (item.itemId) {
            R.id.navigation_station -> changeFragment(StationFragment::class.java)
            R.id.navigation_player -> changeFragment(PlayerFragment::class.java)
            R.id.navigation_more -> changeFragment(MoreFragment::class.java)
        }
        return false
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
