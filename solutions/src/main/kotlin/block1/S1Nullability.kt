package block1

/**
 * Solution for exercise 1 - nullability.
 *
 * The guiding idea: the chain of safe call, `...OrNull` and Elvis replaces
 * nested null checks. Each of the four functions is a single expression.
 */

val sampleRawData: Map<String, String?> = mapOf(
    "name" to "  Camera Studio B  ",
    "temperature" to "42.5",
    "port" to "9100",
    "location" to null,
)

/**
 * `rawData["temperature"]` is already `String?` - once because the key may
 * be absent, and once because the value itself may be null. The same safe
 * call handles both. `toDoubleOrNull()` covers the third case: present but
 * nonsensical text.
 */
fun readTemperature(rawData: Map<String, String?>): Double? =
    rawData["temperature"]?.toDoubleOrNull()

/**
 * `takeIf` turns "blank" back into null and thereby joins the same chain;
 * the Elvis operator supplies the default at the end.
 *
 * The return type is `String`, not `String?`: the function guarantees that
 * something usable always comes out - that is part of its contract.
 */
fun readDeviceName(rawData: Map<String, String?>): String =
    rawData["name"]?.trim()?.takeIf { it.isNotEmpty() } ?: "unknown device"

/**
 * The same pattern plus a range check. `takeIf` checks the range here and
 * turns an invalid port back into null, so the Elvis operator takes over.
 */
fun readPort(rawData: Map<String, String?>, default: Int = 9000): Int =
    rawData["port"]?.toIntOrNull()?.takeIf { it in 1..65535 } ?: default

/**
 * Putting it together. The temperature is the only value allowed to be
 * missing, so there is one more Elvis here - this time on the formatted text.
 */
fun describe(rawData: Map<String, String?>): String {
    val name = readDeviceName(rawData)
    val port = readPort(rawData)
    val reading = readTemperature(rawData)?.let { "$it °C" } ?: "no reading"

    return "$name (port $port) - $reading"
}

fun main() {
    println(describe(sampleRawData))
}
