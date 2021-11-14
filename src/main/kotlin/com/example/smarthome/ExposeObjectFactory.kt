package com.example.smarthome

import com.example.smarthome.expose.BinaryExpose
import com.example.smarthome.expose.EnumExpose
import com.example.smarthome.expose.ExposeObject
import com.example.smarthome.expose.NumericExpose
import com.google.gson.JsonObject

class ExposeObjectFactory {
    companion object {
        fun createExposeObjectFromJson(json: JsonObject) : ExposeObject?
        {
            when (json.get("type").asString)
            {
                null -> return null
                "binary" -> return BinaryExpose(
                        json.get("name").asString,
                        json.get("description").asString,
                    json.get("presets")?.asJsonArray?.map { it.asJsonObject.get("name").asString }?.toList() ?: emptyList(),
                        json.get("value_on").asString,
                        json.get("value_off").asString,
                        getIsSet(json),
                        getIsGet(json)
                )
                "numeric" -> return NumericExpose(
                        json.get("name").asString,
                        json.get("description").asString,
                        json.get("presets")?.asJsonArray?.map { it.asJsonObject.get("name").asString }?.toList() ?: emptyList(),
                        json.get("value_min").asInt,
                        json.get("value_max").asInt,
                        getIsSet(json),
                        getIsGet(json)
                )
                "enum" -> return EnumExpose(
                    json.get("name").asString,
                    json.get("description").asString,
                    json.get("presets")?.asJsonArray?.map { it.asJsonObject.get("name").asString }?.toList() ?: emptyList(),
                    json.get("values").asJsonArray.map { it.asString }.toList(),
                    getIsSet(json),
                    getIsGet(json)
                )
                else -> return null
            }
        }

        private fun getIsSet(json: JsonObject): Boolean
        {
            val access = json.get("access").asInt
            return access == 2 || access == 3 || access == 6 || access == 7
        }

        private fun getIsFoundInPublish(json: JsonObject): Boolean
        {
            val access = json.get("access").asInt
            return access == 1 || access == 3 || access == 7
        }

        private fun getIsGet(json: JsonObject): Boolean
        {
            val access = json.get("access").asInt
            return access == 4 || access == 5 || access == 6 || access == 7
        }
    }
}