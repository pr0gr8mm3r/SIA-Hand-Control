package de.sia.wazynski.simon.handcontrol

enum class ArduinoProtocol(name: String) {
    DEFAULT("default"),
    CUSTOM("custom");

    companion object {
        fun from(s: String): ArduinoProtocol? {
            return when (s) {
                "default" -> DEFAULT
                "custom" -> CUSTOM
                else -> null
            }
        }
    }
}