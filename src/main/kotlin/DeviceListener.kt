import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttMessage
import expose.*
import util.Util
import java.lang.IllegalArgumentException

class DeviceListener(private val knownDevices: MutableMap<String, Device>) : IMqttMessageListener {

    override fun messageArrived(topic: String?, message: MqttMessage) {
        when (topic)
        {
            null -> throw IllegalArgumentException("topic may not be null")
            "zigbee2mqtt/bridge/devices" -> {
                val device = Util.parseJsonForZigbee2Mqtt(message.toString())
                device ?: println("Parsing failed, no device added")
                device?.let { knownDevices[device.getFriendlyName()] = device }
            }
        }
    }
}
