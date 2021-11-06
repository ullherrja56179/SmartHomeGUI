import util.Util

fun main() {
    val listener: DeviceListener = DeviceListener()

    while (true)
    {
        val command = readLine()?.split(" ")
        when (command?.get(0)?.lowercase())
        {
            "addnew" -> listener.startListeningForNewDevices()
            "getdevices" -> println(Util.knownDevices)
            "set" -> {
                val deviceName = command[1]
                val key = command[2]
                val value = command[3]
                Util.knownDevices[deviceName]?.handleSet(key, value) ?: continue
            }
            else -> break
        }
    }


}