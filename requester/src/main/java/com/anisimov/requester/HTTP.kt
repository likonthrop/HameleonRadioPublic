package com.anisimov.requester

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL
import java.net.URLEncoder

const val defUrl = "https://zcor.ru/tire/radioonline"

fun getHttpResponse(request: String, callback: HttpResponseCallback) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            var newReq = request
            if (request.contains("?request="))
                newReq = request.substringBefore("?request=") + "?request=" + URLEncoder.encode(request.substringAfter("?request="), "UTF-8")
            val responseString: String = URL("$defUrl$newReq").openStream().bufferedReader().readLine()
            callback.onResponse(responseString)
        } catch (e: Exception) {
            callback.onError(e.localizedMessage)
        }
    }.start()
}