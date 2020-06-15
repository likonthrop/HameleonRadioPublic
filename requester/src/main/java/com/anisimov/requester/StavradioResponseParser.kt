package com.anisimov.requester

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory

@Throws(JsonParseException::class, JsonMappingException::class)
inline fun <reified T> generateMode(response: String): T {
    try {
        return ObjectMapper().readValue(response , T::class.java)
    } catch (e: Exception) {
        throw e
    }
}

@Throws(JsonParseException::class, JsonMappingException::class)
inline fun <reified T> generateModeList(response: String): List<T> {
    try {
        val mapper = ObjectMapper()
        return mapper.readValue(response, TypeFactory.defaultInstance().constructCollectionType(List::class.java, T::class.java))
    } catch (e: Exception) {
        throw e
    }
}