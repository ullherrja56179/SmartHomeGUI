import util.Util

fun main() {
    val listener: DeviceListener = DeviceListener()

    while (true) {
        val command = readLine()?.split(" ")
        when (command?.get(0)?.lowercase()) {
            "help" -> {
                println("Commands:" +
                        "\n - addnew: Waits 5 seconds, adds new devices and lets you rename them with a meaningful name afterwards" +
                        "\n - getdevices: returns a map of all known devices" +
                        "\n - set <meaningful_name> <key> <value>: lets you set the value to a given key. if the input is wrong nothing happens."
                )
            }
            "addnew" -> {
                listener.startListeningForNewDevices()
                println("----- DONE -----")
            }
            "getdevices" -> {
                println(Util.knownDevices)
                println("----- DONE -----")

            }
            "set" -> {
                if ( command.size != 4 ) continue
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