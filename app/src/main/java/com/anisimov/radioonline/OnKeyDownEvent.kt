package com.anisimov.radioonline

interface OnKeyDownEvent {
    fun subscribeOnKeyDownEvent(onKeyDownListener: OnKeyDownListener)
    fun unsubscribeOnKeyDownListener(onKeyDownListener: OnKeyDownListener)
}