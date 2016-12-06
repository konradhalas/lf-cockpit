package pl.konradhalas.lfcockpit.domain

sealed class Command {
    class ToggleLEDCommand : Command() {
        override fun serialize(): String {
            return "TOGGLE"
        }
    }

    class BatteryReadRequestCommand : Command() {
        override fun serialize(): String {
            return "BATTERY_READ"
        }
    }

    abstract fun serialize(): String
}

sealed class Message {
    class ButtonMessage(val isUp: Boolean) : Message()
    class BatteryMessage(val voltage: Int) : Message()
}

class MessageParseError : Exception()

class MessagesParser {

    companion object {
        fun parse(data: String): Message {
            val tokens = data.split(" ")
            return when (tokens[0]) {
                "BUTTON" -> Message.ButtonMessage(tokens[1] == "UP")
                "BATTERY" -> Message.BatteryMessage(tokens[1].toInt())
                else -> throw MessageParseError()
            }
        }
    }

}

class CommandsSerializer {

    companion object {
        fun serialize(command: Command): ByteArray {
            return command.serialize().padEnd(16).toByteArray()
        }
    }
}
