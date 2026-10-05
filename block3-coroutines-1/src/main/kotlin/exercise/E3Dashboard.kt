package exercise

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Exercise 3 (open) – a monitoring dashboard.
 *
 * Everything from block 3 in one piece: several concurrent requests, a
 * scope that owns them, and a source that is allowed to be too slow.
 *
 * The result type is a sealed interface from block 2 – a device snapshot
 * is either complete or partial, and each variant carries exactly the data
 * that belongs to it.
 *
 * Verify with:
 *     ./gradlew :block3-coroutines-1:exerciseTest --tests "exercise.E3*"
 */

/** The three sources. Do not change them. */
suspend fun fetchDeviceStatus(deviceId: String): String {
    delay(150.milliseconds)
    return "online"
}

suspend fun fetchDeviceUtilisation(deviceId: String): Int {
    delay(200.milliseconds)
    return deviceId.length * 7
}

/**
 * The temperature sensor is the unreliable one: devices whose id starts
 * with "slow-" take a very long time. Deliberately deterministic, so the
 * tests do not flake.
 */
suspend fun fetchDeviceTemperature(deviceId: String): Double {
    if (deviceId.startsWith("slow-")) delay(2.seconds) else delay(180.milliseconds)
    return 21.5
}

sealed interface DeviceSnapshot {
    val deviceId: String

    /** All three values arrived in time. */
    data class Complete(
        override val deviceId: String,
        val status: String,
        val utilisation: Int,
        val temperature: Double,
    ) : DeviceSnapshot

    /** The temperature did not arrive in time; the rest did. */
    data class Partial(
        override val deviceId: String,
        val status: String,
        val utilisation: Int,
    ) : DeviceSnapshot
}

/**
 * Exercise 3a
 *
 * Collects a snapshot for one device.
 *
 *  - All three sources are queried AT THE SAME TIME. The slowest of them
 *    determines the duration, not their sum.
 *  - The temperature only gets `temperatureTimeoutMillis` to answer.
 *    If it takes longer, return a [DeviceSnapshot.Partial] – status and
 *    utilisation are still delivered.
 *
 * Useful: coroutineScope, async, withTimeoutOrNull
 *
 * Note the following pitfall. Written as
 *
 *     val temperature = async { fetchDeviceTemperature(deviceId) }
 *     val value = withTimeoutOrNull(timeout) { temperature.await() }
 *
 * the timeout cancels the WAITING, but not the request. The `async` is
 * still a child of the scope, and `coroutineScope` does not return until
 * all its children are done – so your function would still take the full
 * two seconds, and one of the tests will tell you so.
 *
 * Think about where the timeout really belongs.
 */
suspend fun deviceSnapshot(
    deviceId: String,
    temperatureTimeoutMillis: Long = 500,
): DeviceSnapshot {
    TODO("3a: collect one snapshot with a timeout on the temperature")
}

/**
 * Exercise 3b
 *
 * A snapshot for every device – again all at the same time.
 * Order of results follows order of input.
 */
suspend fun dashboard(
    deviceIds: List<String>,
    temperatureTimeoutMillis: Long = 500,
): List<DeviceSnapshot> {
    TODO("3b: collect all snapshots concurrently")
}

/**
 * Exercise 3c
 *
 * One line per snapshot, using an exhaustive `when` without `else`:
 *
 *     "cam-04: online, 42 %, 21.5 °C"
 *     "slow-01: online, 49 %, temperature unavailable"
 */
fun renderLine(snapshot: DeviceSnapshot): String {
    TODO("3c: render a snapshot as one line")
}

/**
 * Exercise 3d
 *
 * Counts how many snapshots are incomplete. Useful for a health indicator
 * on the dashboard.
 */
fun countIncomplete(snapshots: List<DeviceSnapshot>): Int {
    TODO("3d: count the partial snapshots")
}
