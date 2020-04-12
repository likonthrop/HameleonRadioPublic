package com.anisimov.radioonline

import android.view.KeyEvent

interface OnKeyDownListener {
        fun onKeyDown(keyCode: Int, event: KeyEvent?)
    }