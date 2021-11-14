package com.example.smarthome

import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttClient
import com.example.smarthome.util.Util
import java.lang.IllegalArgumentException

class DeviceListener() : IMqttMessageListener {

    lateinit var client: MqttClient
    private val clientId = "SmartHomeTest"
    private val newdevices = mutableListOf<Device>()

    fun startListeningForNewDevices() {
        client = MqttClient(Util.mqttBrokerUrl, clientId)
        client.connect()
        client.publish("zigbee2mqtt/bridge/request/permit_join", MqttMessage("{\"value\": true}, \"time\": 5}".toByteArray()))
        if (client.isConnected) waitForDevicesMessage(client, 5000)
    }

    private fun waitForDevicesMessage(client: MqttClient, timeout: Long) {
        client.subscribe(Util.zigbeeDevicesTopic, this)
        println("Waiting for new devices to connect...")
        Thread.sleep(timeout)
        println("Waited $timeout ms. Stop listening for devices...")
        newdevices.forEach { Util.knownDevices.add(it) }
        newdevices.clear()
        client.disconnect()
    }

    override fun messageArrived(topic: String?, message: MqttMessage) {
        when (topic)
        {
            null -> throw IllegalArgumentException("topic may not be null")
            "zigbee2mqtt/bridge/devices" -> {
                newdevices.addAll(Util.parseJsonForZigbee2Mqtt(message.toString()))
            }
        }
    }

}
