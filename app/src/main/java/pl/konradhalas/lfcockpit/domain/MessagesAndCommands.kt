package pl.konradhalas.lfcockpit.domain

sealed class Command {
    class ToggleLEDCommand : Command() {
        override fun serialize(): String {
            return "TOGGLE"
        }
    }

    class ReadSensorsRequestCommand : Command() {
        override fun serialize(): String {
            return "READ_SENSORS"
        }
    }


    abstract fun serialize(): String
}

data class SensorValue(val number: Int, val value: Int)

sealed class Message {
    class BatteryMessage(val voltage: Int) : Message()
    class SensorsMessage(val values: List<SensorValue>) : Message()
}

class MessageParseError : Exception()

class MessagesParser {

    companion object {
        fun parse(data: String): Message {
            val tokens = data.split(" ")
            return when (tokens[0]) {
                "BATTERY" -> parseBatteryMessage(tokens)
                "SENSORS" -> parseSensorsMessage(tokens)
                else -> throw MessageParseError()
            }
        }

        private fun parseBatteryMessage(tokens: List<String>) = Message.BatteryMessage(tokens[1].toInt())

        private fun parseSensorsMessage(tokens: List<String>): Message.SensorsMessage {
            return Message.SensorsMessage((1..tokens.size - 1).map {
                index ->
                SensorValue(index, tokens[index].toInt())
            })
        }
    }

}

class CommandsSerializer {

    companion object {
        private val BLE_MTU = 20
        fun serialize(command: Command): ByteArray {
            return command.serialize().padEnd(BLE_MTU).toByteArray()
        }
    }
}
