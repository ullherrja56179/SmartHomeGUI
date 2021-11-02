import org.eclipse.paho.client.mqttv3.MqttClient

fun main() {
    val topic = "zigbee2mqtt/bridge/devices"
    val brokerAddr = "tcp://localhost:1592"
    var client: MqttClient? = null
    val knownDevices: MutableMap<String, Device> = mutableMapOf()

    client = MqttClient(brokerAddr, "ClientTestId")
    client.connect()
    val listener = DeviceListener(knownDevices)
    client.subscribe(topic, listener)
    println("Set up client for adding devices: Doing stuff now...")

    Thread.sleep(10000)
    knownDevices["0x0c4314fffe11ff41"]?.handleSet("state", "off")
    Thread.sleep(10000)
    knownDevices["0x0c4314fffe11ff41"]?.handleSet("state", "on")
    Thread.sleep(10000)
    knownDevices["0x0c4314fffe11ff41"]?.handleSet("brightness", "250")
    Thread.sleep(10000)
    knownDevices["0x0c4314fffe11ff41"]?.handleSet("brightness", "80")
    Thread.sleep(10000)
    knownDevices["0x0c4314fffe11ff41"]?.handleSet("color_temp", "warmest")
    client.disconnect()
}
