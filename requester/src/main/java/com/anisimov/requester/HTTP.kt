package com.anisimov.requester

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

const val defUrl = "https://zcor.ru/tire/radioonline"

fun getHttpResponse(request: String, callback: HttpResponseCallback) {
//    Log.d("HTTP", "getHttpResponse: request = [$request]")
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val responseString: String = URL("$defUrl$request").openStream().bufferedReader().readLine()
//            Log.d("HTTP", "getHttpResponse: response = [$responseString]")
            callback.onResponse(responseString)
        } catch (e: IOException) {
//            Log.e("HTTP", "Throw(IOException): ${e.localizedMessage}")
            callback.onError(e.localizedMessage)
        }
    }.start()
}