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
import com.anisimov.radioonline.item.Item
import com.anisimov.radioonline.item.models.BannerModel
import com.anisimov.radioonline.item.models.StationBanner
import com.anisimov.radioonline.item.models.StationModel
import com.anisimov.radioonline.radio.RadioService
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    OnKeyDownEvent {

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
                addFragment(StationFragment(it, generateStationList()), true)
                addFragment(PlayerFragment(it))
                addFragment(MoreFragment())
            }

            binding.bottomNavigation.selectedItemId = R.id.navigation_station
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    fun showPlayer() {
        binding.bottomNavigation.menu.getItem(1).isEnabled = true
    }

    fun generateStationList(): ArrayList<Item> {
        val link = "https://stream.stvradio.online:8010/newradiostv.aac"
        return arrayListOf(
            StationBanner(generateBannerArray()),
            StationModel(
                1,
                "Авто радио",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/avtoradio.png.webp",
                link
            ),
            StationModel(
                2,
                "BBC radio",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/bbc.png.webp",
                link
            ),
            StationModel(
                3,
                "ESPA radio",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/ESPA.png.webp",
                link
            ),
            StationModel(
                4,
                "HD radio",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/radio.png.webp",
                link
            ),
            StationModel(
                5,
                "Radio Olavide",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/radiolavide.png.webp",
                link
            ),
            StationModel(
                6,
                "RoundHouse radio",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/radio98-3.png.webp",
                link
            ),
            StationModel(
                7,
                "Русское радио",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/russkoyeradio.png.webp",
                link
            ),
            StationModel(
                8,
                "Радио Шансон",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/shanson.png.webp",
                link
            ),
            StationModel(
                9,
                "Vision radio",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/vision.png.webp",
                link
            ),
            StationModel(
                10,
                "Webmaster radio",
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/webmasterradio.png.webp",
                link
            )
        )
    }

    private fun generateBannerArray(): Array<BannerModel> {
        return arrayOf(
            BannerModel(
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/avtoradio.png.webp",
                "Авто радио"
            ),
            BannerModel(
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/bbc.png.webp",
                "BBC radio"
            ),
            BannerModel(
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/ESPA.png.webp",
                "ESPA radio"
            ),
            BannerModel(
                "https://turbologo.ru/blog/wp-content/uploads/2019/04/radio.png.webp",
                "HD radio"
            ),
            BannerModel(
                "https://www.proreklamu.com/media/upload/news/52138/15582.jpg",
                "Radio Olavide"
            )
        )
    }

    fun playNext(forward: Boolean) {
        val f = supportFragmentManager.findFragmentByTag(StationFragment::class.java.simpleName) as StationFragment
        if (forward) f.playForward() else f.playBelow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, RadioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
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
        supportFragmentManager.apply {
            when (backStackEntryCount) {
                1 -> finish()
                2 -> binding.bottomNavigation.selectedItemId = R.id.navigation_station
            }
        }
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

    private fun <T> changeFragment(fragment: T) {
        supportFragmentManager.apply {
            fragments.forEach {
                if (it::class.java == fragment) beginTransaction().show(it).commit()
                else beginTransaction().hide(it).commit()
            }
        }
    }

    private val keyDownListenerPool = arrayListOf<OnKeyDownListener>()

    override fun subscribeOnKeyDownEvent(onKeyDownListener: OnKeyDownListener) {
        if (!keyDownListenerPool.contains(onKeyDownListener)) keyDownListenerPool.add(
            onKeyDownListener
        )
    }

    override fun unsubscribeOnKeyDownListener(onKeyDownListener: OnKeyDownListener) {
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
