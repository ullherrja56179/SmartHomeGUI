package util

import Device
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import expose.BinaryExpose
import expose.EnumExpose
import expose.ExposeObject
import expose.NumericExpose

class Util {
    companion object {
        val knownDevices: MutableMap<String, Device> = mutableMapOf()
        const val mqttBrokerUrl = "tcp://localhost:1592"
        const val zigbeeDevicesTopic = "zigbee2mqtt/bridge/devices"

        fun parseJsonForZigbee2Mqtt(json: String): MutableList<Device> {
            val newDevices = mutableListOf<Device>()
            val jsonIterator = JsonParser.parseString(json).asJsonArray.iterator()
            iterateOverJsonAndCreateNewDevices(jsonIterator, newDevices)
            return newDevices
        }

        private fun iterateOverJsonAndCreateNewDevices( jsonIterator: MutableIterator<JsonElement>, newDevices: MutableList<Device>)
        {
            while (jsonIterator.hasNext()) {
                val exposes: MutableList<ExposeObject> = mutableListOf()
                val currentJson = jsonIterator.next().asJsonObject

                if (deviceIsSupported(currentJson)) {
                    val friendlyName = getStringFromJson(currentJson, "friendly_name") ?: break
                    val isKnownDevice =
                        knownDevices.values.firstOrNull { device -> device.getFriendlyName().equals(friendlyName) }
                    if (isKnownDevice != null) break
                    val manufacturer = getStringFromJson(currentJson, "manufacturer") ?: "unknown"
                    val deviceDefinition = currentJson.get("definition")
                    val deviceInfo = deviceDefinition.asJsonObject.get("description").asString
                    val vendor = deviceDefinition.asJsonObject.get("vendor").asString
                    val exposesSection = deviceDefinition.asJsonObject.get("exposes").asJsonArray
                    exposes.addExposesFromExposes(exposesSection)
                    val deviceType =
                        deviceDefinition.asJsonObject.get("exposes")?.asJsonArray?.get(0)?.asJsonObject?.get("type")?.asString
                            ?: "unknown"

                    val device = Device(manufacturer, deviceType, vendor, deviceInfo, friendlyName, exposes)
                    newDevices.add(device)
                }
            }
        }

        private fun deviceIsSupported(currentJson: JsonObject) =
            !currentJson.get("supported").asString.equals("false")

        private fun getStringFromJson(json: JsonObject, key: String): String?
        {
            return json.get(key)?.asString
        }

        private fun MutableList<ExposeObject>.addExposesFromExposes(exposesSection: JsonArray)
        {
            val exposesIterator = exposesSection.iterator()
            while (exposesIterator.hasNext())
            {
                val current = exposesIterator.next().asJsonObject
                if (current.has("features")) {
                    addExposesFromFeatures(current.get("features").asJsonArray)
                    continue
                }

                when (current.get("type").asString)
                {
                    "binary" -> add(
                        BinaryExpose(
                            current.get("name").asString,
                            current.get("description").asString,
                            createPresets(current.get("presets").asJsonArray),
                            current.get("value_on").asString,
                            current.get("value_off").asString
                        )
                    )
                    "numeric" -> add(
                        NumericExpose(
                            current.get("name").asString,
                            current.get("description").asString,
                            createPresets(current.get("presets")?.asJsonArray),
                            current.get("value_min").asInt,
                            current.get("value_max").asInt
                        )
                    )
                    "enum" -> add (
                        EnumExpose(
                            "name",
                            "description",
                            mutableListOf("1","2"),
                            mutableListOf("1","2")
                        )
                    )
                    else -> continue
                }
            }
        }

        fun createPresets(presets: JsonArray?): MutableList<String>
        {
            val list = mutableListOf<String>()
            presets?.forEach { list.add(it.asJsonObject.get("name").asString) }
            return list
        }

        fun MutableList<ExposeObject>.addExposesFromFeatures(featuresSection: JsonArray?)
        {
            featuresSection?.onEach { it ->
                val foundPresets = mutableListOf<String>()
                val current = it.asJsonObject
                val name = current.get("name").asString
                val description = current.get("description").asString

                current.get("presets")?.asJsonArray?.forEach {
                    foundPresets.add(it.asJsonObject.get("name").asString)
                }

                when (current.get("type").asString) {
                    "binary" -> add(
                        BinaryExpose(
                            name,
                            description,
                            foundPresets,
                            current.get("value_on").asString,
                            current.get("value_off").asString
                        )
                    )
                    "numeric" -> add(
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
        }

        private fun constructExposeOptionsFromFeatures(featuresSection: JsonArray?) : List<ExposeObject>{
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