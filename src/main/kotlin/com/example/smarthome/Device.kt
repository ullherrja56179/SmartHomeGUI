package com.example.smarthome

import com.example.smarthome.expose.BinaryExpose
import com.example.smarthome.expose.EnumExpose
import com.example.smarthome.expose.ExposeObject
import com.example.smarthome.expose.NumericExpose
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

data class Device(private val manufacturer: String, private val type: String, private val vendor: String, private val description: String, private val friendlyName: String, private val exposes: List<ExposeObject>) {

    val enumExposes: List<EnumExpose>
        get() = exposes.filter { it is EnumExpose }.map { it as EnumExpose }

    private val setTopic: String
        get() {
            return "zigbee2mqtt/$friendlyName/set"
        }
    private val getTopic: String
        get() {
            return "zigbee2mqtt/$friendlyName/get"
        }
    private var name: String? = null

    private val UID = UUID.randomUUID().toString()

    init {
        println("Created new Device: $this")
    }

    fun handleSet(key: String, value: String): Boolean
    {
        println("Handling set now...")
        val exposeObject = getExposeForKey(key)
        println("GOT EXPOSE: $exposeObject")
        if (exposeObject == null || !exposeObject.canBeSet()) {
            println("key=$key cannot be set!")
            return false
        }
        if (!checkValidityOfInput(exposeObject, value))
        {
            println("Input is wrong: key=$key, value=$value")
            return false
        }
        val mqttClient = MqttClient("tcp://localhost:1592", UID)
        mqttClient.connect()
        if (!mqttClient.isConnected)
        {
            println("Could not connect MQTT-CLient")
            return false
        }
        val message = MqttMessage(value.toByteArray())
        println("... mqttClient publishing now: key=$key value=$value")
        mqttClient.publish("$setTopic/$key", message)
        mqttClient.disconnect()
        return true
    }

    private fun getExposeForKey(key: String): ExposeObject?
    {
        return exposes.firstOrNull { it.name == key }
    }

    fun getSettableExposes(): List<ExposeObject>
    {
        return exposes.filter { it.isSet }
    }

    private fun checkValidityOfInput(exposeObject: ExposeObject?, value: String): Boolean {
        return when (exposeObject) {
            null -> false
            else -> return exposeObject.validateValue(value)
        }
    }

    fun getFriendlyName() : String
    {
        return friendlyName
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getName(): String?
    {
        return name
    }

    fun getDescription(): String
    {
        return description
    }

}