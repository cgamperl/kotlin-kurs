package exercise

/**
 * Exercise 1 - Nullability.
 *
 * The starting point: a device reports its telemetry as loose text values.
 * Fields may be missing, may be null, or may contain nonsense. Your job is
 * to turn that into reliable values without letting the application crash.
 *
 * Rules for this exercise:
 *  - No `!!`. One of the tests checks the source file for it.
 *  - No try/catch around number conversions. Use the `...OrNull` variants.
 *
 * Fill in the four TODOs. Verify with:
 *     ./gradlew :block1-grundlagen-1:exerciseTest
 */

/** Sample data as it might arrive from a device. */
val sampleRawData: Map<String, String?> = mapOf(
    "name" to "  Camera Studio B  ",
    "temperature" to "42.5",
    "port" to "9100",
    "location" to null,
)

/**
 * Reads "temperature" as a Double.
 *
 * The result is null when the field is missing, is null, or does not
 * contain a valid number.
 *
 * Useful: String.toDoubleOrNull()
 */
fun readTemperature(rawData: Map<String, String?>): Double? {
    TODO("1a: read the temperature safely")
}

/**
 * Reads "name" and strips surrounding whitespace.
 *
 * When the field is missing, is null, or consists only of whitespace,
 * "unknown device" is returned. The return type is deliberately NOT
 * nullable - the caller always gets something usable.
 *
 * Useful: trim(), takeIf { ... }, the Elvis operator
 */
fun readDeviceName(rawData: Map<String, String?>): String {
    TODO("1b: read the name with a default value")
}

/**
 * Reads "port" as an Int. When the field is absent or not a valid number,
 * `default` is returned.
 *
 * Only ports within 1..65535 are valid - anything else counts as invalid
 * as well.
 */
fun readPort(rawData: Map<String, String?>, default: Int = 9000): Int {
    TODO("1c: read the port with a range check and a default")
}

/**
 * Builds a one-line description of this shape:
 *
 *     Camera Studio B (port 9100) - 42.5 °C
 *
 * When the temperature is missing, it says "no reading" instead:
 *
 *     Camera Studio B (port 9100) - no reading
 *
 * Use the three functions above.
 */
fun describe(rawData: Map<String, String?>): String {
    TODO("1d: assemble the description")
}

fun main() {
    println(describe(sampleRawData))
}
