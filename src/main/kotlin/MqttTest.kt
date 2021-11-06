import util.Util

fun main() {
    val listener: DeviceListener = DeviceListener()

    while (true) {
        val command = readLine()?.split(" ")
        when (command?.get(0)?.lowercase()) {
            "addnew" -> {
                listener.startListeningForNewDevices()
                println("----- DONE -----")
            }
            "getdevices" -> {
                println(Util.knownDevices)
                println("----- DONE -----")

            }
            "set" -> {
                val deviceName = command[1]
                val key = command[2]
                val value = command[3]
                Util.knownDevices[deviceName]?.handleSet(key, value) ?: continue
                println("----- DONE -----")
            }
            "remove" -> {
                Util.knownDevices.remove(command[1])
                println("----- DONE -----")
            }
            "exit" -> break
            else -> println("Unknown Command")
        }
    }
}