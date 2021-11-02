import expose.BinaryExpose
import expose.ExposeObject
import expose.NumericExpose
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

data class Device(private val manufacturer: String, private val type: String, private val vendor: String, private val description: String, private val friendlyName: String, private val exposes: List<ExposeObject>) {

    private val setTopic: String
        get() {
            return "zigbee2mqtt/$friendlyName/set"
        }
    private val getTopic: String
        get() {
            return "zigbee2mqtt/$friendlyName/get"
        }

    private val UID = UUID.randomUUID().toString()

    init {
        println("Created new Device: $this")
    }

    fun handleSet(key: String, value: String): Boolean
    {
        if (!checkValidityOfInput(key, value))
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

    private fun checkValidityOfInput(key: String, value: String): Boolean {
        println(exposes.firstOrNull { it.name == key })
        return when (val expose: ExposeObject? = exposes.firstOrNull { it.name == key }) {
            null -> false
            is BinaryExpose -> return expose.validateValue(value)
            is NumericExpose -> return expose.validateValue(value)
            else -> false
        }
    }

    fun getFriendlyName() : String
    {
        return friendlyName
    }

}