package com.example.smarthome.util

import com.example.smarthome.Device
import com.example.smarthome.ExposeObjectFactory
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.example.smarthome.expose.ExposeObject

class Util {
    companion object {
        val knownDevices = mutableListOf<Device>()
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
                    val isKnownDevice = knownDevices.firstOrNull { device -> device.getFriendlyName() == friendlyName }
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

                ExposeObjectFactory.createExposeObjectFromJson(current)?.let { it -> add(it) }
            }
        }

        private fun MutableList<ExposeObject>.addExposesFromFeatures(featuresSection: JsonArray)
        {
            featuresSection.onEach { it ->
                val foundPresets = mutableListOf<String>()
                val current = it.asJsonObject
                val name = current.get("name").asString
                val description = current.get("description").asString

                current.get("presets")?.asJsonArray?.forEach {
                    foundPresets.add(it.asJsonObject.get("name").asString)
                }

                ExposeObjectFactory.createExposeObjectFromJson(current)?.let { it1 -> add(it1) }
            }
        }
    }
}