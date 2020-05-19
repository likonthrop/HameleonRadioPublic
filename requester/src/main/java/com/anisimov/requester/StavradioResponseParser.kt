package com.anisimov.requester

import android.util.Log
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory

@Throws(JsonParseException::class, JsonMappingException::class)
inline fun <reified T> generateMode(response: String): T {
    try {
        val nowPlayingStation = ObjectMapper().readValue(response , T::class.java)
//        Log.d("StavradioResponseParser", "generateNowPlayingStation: complete")
        return nowPlayingStation
    } catch (e: JsonParseException) {
//        Log.e("StavradioResponseParser", "Throw(JsonParseException): ${e.localizedMessage}")
        throw e
    } catch (e: JsonMappingException) {
//        Log.e("StavradioResponseParser", "Throw(JsonMappingException): ${e.localizedMessage}")
        throw e
    }
}

@Throws(JsonParseException::class, JsonMappingException::class)
inline fun <reified T> generateModeList(response: String): List<T> {
    try {
        val mapper = ObjectMapper()
        val nowPlayingStation = mapper.readValue<List<T>>(response, TypeFactory.defaultInstance().constructCollectionType(List::class.java, T::class.java))
//        Log.d("StavradioResponseParser", "generateModeList: complete")
        return nowPlayingStation
    } catch (e: JsonParseException) {
//        Log.e("StavradioResponseParser", "generateModeList - Throw(JsonParseException): ${e.localizedMessage}")
        throw e
    } catch (e: JsonMappingException) {
//        Log.e("StavradioResponseParser", "generateModeList - Throw(JsonMappingException): ${e.localizedMessage}")
        throw e
    }
}