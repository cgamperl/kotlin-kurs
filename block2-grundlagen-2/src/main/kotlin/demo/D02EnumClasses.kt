package demo

/**
 * Demo 2 - Enum classes.
 *
 * Kotlin enums are fully fledged classes: they may have constructor
 * parameters, properties, methods, and even a separate implementation per
 * constant.
 */

/**
 * An enum with constructor parameters and a computed property.
 */
enum class DeviceType(
    val displayName: String,
    val defaultPort: Int,
) {
    CAMERA("Camera", 9100),
    INTERCOM("Intercom panel", 9200),
    ROUTER("Signal router", 9000),
    SENSOR("Sensor", 9300);

    /** An ordinary property - the same for every constant. */
    val isNetworkDevice: Boolean
        get() = this == ROUTER || this == INTERCOM
}

/**
 * An enum that implements an interface and brings its own implementation
 * per constant. That binds behaviour to the constant instead of pushing it
 * out into a `when`.
 */
interface Escalating {
    fun nextLevel(): AlarmLevel
}

enum class AlarmLevel : Escalating {
    INFO {
        override fun nextLevel() = WARNING
    },
    WARNING {
        override fun nextLevel() = CRITICAL
    },
    CRITICAL {
        override fun nextLevel() = CRITICAL   // it does not get worse
    };
}

fun main() {
    // `entries` (since Kotlin 1.9) replaces the old values().
    // The difference: entries is an immutable list and does not allocate a
    // new array on every access.
    println("all types: ${DeviceType.entries}")

    for (type in DeviceType.entries) {
        println(
            "${type.name.padEnd(9)} ${type.displayName.padEnd(16)} " +
                "port ${type.defaultPort}  network: ${type.isNetworkDevice}"
        )
    }

    // Built-in properties of every enum constant:
    println()
    println("name:    ${DeviceType.ROUTER.name}")
    println("ordinal: ${DeviceType.ROUTER.ordinal}")

    // Creating one from text - valueOf throws when the value does not exist.
    println("valueOf: ${DeviceType.valueOf("SENSOR")}")
    // Which is why there is a safe variant:
    println("safe:    ${DeviceType.entries.firstOrNull { it.name == "DOES_NOT_EXIST" }}")

    // A `when` over an enum is exhaustive: no else needed as long as every
    // constant is covered.
    println()
    for (type in DeviceType.entries) {
        val maintenanceInterval = when (type) {
            DeviceType.CAMERA -> "6 months"
            DeviceType.INTERCOM -> "12 months"
            DeviceType.ROUTER -> "3 months"
            DeviceType.SENSOR -> "24 months"
        }
        println("${type.displayName}: $maintenanceInterval")
    }

    // Behaviour on the constant instead of inside a when:
    println()
    var level = AlarmLevel.INFO
    repeat(3) {
        println("$level -> ${level.nextLevel()}")
        level = level.nextLevel()
    }
}
