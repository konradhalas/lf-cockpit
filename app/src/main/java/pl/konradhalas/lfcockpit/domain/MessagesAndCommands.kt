package pl.konradhalas.lfcockpit.domain

sealed class Command {
    class StartStopCommand : Command() {
        override fun serialize(): String {
            return "T"
        }
    }

    class CalibrateCommand : Command() {
        override fun serialize(): String {
            return "C"
        }
    }

    abstract fun serialize(): String
}

data class SensorValue(val number: Int, val value: Int)

sealed class Message {
    class BatteryMessage(val voltage: Int) : Message()
    class SensorsMessage(val values: List<SensorValue>) : Message()
    class UnknownMessage(val data: String) : Message()
}

class MessagesParser {

    companion object {
        fun parse(data: String): Message {
            val tokens = data.split(" ")
            return when (tokens[0]) {
                "B" -> parseBatteryMessage(tokens)
                "S" -> parseSensorsMessage(tokens)
                else -> Message.UnknownMessage(data)
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
