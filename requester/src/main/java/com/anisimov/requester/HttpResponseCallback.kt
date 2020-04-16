package com.anisimov.requester

interface HttpResponseCallback {
    fun onResponse(response: String)
    fun onError(e: String?) {}
}