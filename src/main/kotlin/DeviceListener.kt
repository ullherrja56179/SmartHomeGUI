import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttClient
import util.Util
import java.lang.IllegalArgumentException

class DeviceListener() : IMqttMessageListener {

    lateinit var client: MqttClient
    val clientId = "SmartHomeTest"
    val newdevices = mutableListOf<Device>()

    fun startListeningForNewDevices() {
        client = MqttClient(Util.mqttBrokerUrl, clientId)
        client.connect()
        if (client.isConnected) waitForDevicesMessage(client, 5000)
    }

    private fun waitForDevicesMessage(client: MqttClient, timeout: Long) {
        client.subscribe(Util.zigbeeDevicesTopic, this)
        println("Waiting for new devices to connect...")
        Thread.sleep(timeout)
        println("Waited $timeout ms. Stop listening for devices...")
        renameNewDevices(newdevices)
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

    private fun renameNewDevices( newDevices: MutableList<Device> )
    {
        if (newDevices.isEmpty())
        {
            println("No new devices found...")
            return
        } else
        {
            newDevices.forEach { device ->
                println("Please enter name for Device $device")
                var name = ""
                while (name.isEmpty())
                {
                    name = readLine()!!
                }
                Util.knownDevices[name] = device
                println("Added Device with name=$name")
            }
        }
    }
}
