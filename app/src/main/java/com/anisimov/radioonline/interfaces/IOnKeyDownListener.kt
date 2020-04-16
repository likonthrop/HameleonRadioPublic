package com.anisimov.radioonline.interfaces

import android.view.KeyEvent

interface IOnKeyDownListener {
        fun onKeyDown(keyCode: Int, event: KeyEvent?)
    }