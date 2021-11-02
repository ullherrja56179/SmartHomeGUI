package util

import Device
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import expose.BinaryExpose
import expose.ExposeObject
import expose.NumericExpose

class Util {
    companion object {

        fun parseJsonForZigbee2Mqtt(json: String) : Device? {

            var device: Device? = null
            val jsonIterator = JsonParser.parseString(json).asJsonArray.iterator()
            while (jsonIterator.hasNext()) {
                val currentJson = jsonIterator.next().asJsonObject

                if (!currentJson.get("supported").asString.equals("false"))
                {
                    val friendlyName = getStringFromJson(currentJson, "friendly_name") ?: break
                    val manufacturer = getStringFromJson(currentJson, "manufacturer") ?: "unknown"
                    val deviceDefinition = currentJson.get("definition")
                    val deviceInfo = deviceDefinition.asJsonObject.get("description").asString
                    val vendor = deviceDefinition.asJsonObject.get("vendor").asString
                    val featuresSection = deviceDefinition.asJsonObject.get("exposes")?.asJsonArray?.get(0)?.asJsonObject?.get("features")?.asJsonArray
                    val exposes = constructExposeOptions(featuresSection)
                    val deviceType = deviceDefinition.asJsonObject.get("exposes")?.asJsonArray?.get(0)?.asJsonObject?.get("type")?.asString ?: "unknown"

                    device = Device(manufacturer, deviceType, vendor, deviceInfo, friendlyName, exposes)
                }
            }
            return device
        }

        private fun getStringFromJson(json: JsonObject, key: String): String?
        {
            return json.get(key)?.asString
        }

        private fun constructExposeOptions(featuresSection: JsonArray?) : List<ExposeObject>{
            val exposes = mutableListOf<ExposeObject>()
            featuresSection?.onEach { it ->
                val foundPresets = mutableListOf<String>()
                val current = it.asJsonObject
                val name = current.get("name").asString
                val description = current.get("description").asString

                current.get("presets")?.asJsonArray?.forEach {
                    foundPresets.add(it.asJsonObject.get("name").asString)
                }

                when (current.get("type").asString) {
                    "binary" -> exposes.add(
                        BinaryExpose(
                        name,
                        description,
                        foundPresets,
                        current.get("value_on").asString,
                        current.get("value_off").asString
                    )
                    )
                    "numeric" -> exposes.add(
                        NumericExpose(
                        name,
                        description,
                        foundPresets,
                        current.get("value_min").asInt,
                        current.get("value_max").asInt
                    )
                    )
                    else -> println("Unkown Expose-Type")
                }
            }
            return exposes
        }

    }
}