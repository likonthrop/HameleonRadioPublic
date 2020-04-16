package com.anisimov.radioonline.interfaces

interface IOnKeyDownEvent {
    fun subscribeOnKeyDownEvent(onKeyDownListener: IOnKeyDownListener)
    fun unsubscribeOnKeyDownListener(onKeyDownListener: IOnKeyDownListener)
}