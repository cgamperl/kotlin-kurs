package block3

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Solution for exercise 3 - monitoring dashboard.
 */

suspend fun fetchDeviceStatus(deviceId: String): String {
    delay(150.milliseconds)
    return "online"
}

suspend fun fetchDeviceUtilisation(deviceId: String): Int {
    delay(200.milliseconds)
    return deviceId.length * 7
}

suspend fun fetchDeviceTemperature(deviceId: String): Double {
    if (deviceId.startsWith("slow-")) delay(2.seconds) else delay(180.milliseconds)
    return 21.5
}

sealed interface DeviceSnapshot {
    val deviceId: String

    data class Complete(
        override val deviceId: String,
        val status: String,
        val utilisation: Int,
        val temperature: Double,
    ) : DeviceSnapshot

    data class Partial(
        override val deviceId: String,
        val status: String,
        val utilisation: Int,
    ) : DeviceSnapshot
}

/**
 * 3a - the timeout belongs INSIDE the async.
 *
 * That is the essential part of the exercise. Written this way,
 * `withTimeoutOrNull` cancels `fetchDeviceTemperature` itself. The child
 * coroutine then completes - with null - and `coroutineScope` can return.
 *
 * With the timeout around `await()` instead, only the waiting would be
 * cancelled. The request would keep running as a child of the scope, and
 * the scope would wait the full two seconds before returning.
 *
 * Structured concurrency enforces this deliberately: a request that is no
 * longer awaited is not left running unnoticed.
 */
suspend fun deviceSnapshot(
    deviceId: String,
    temperatureTimeoutMillis: Long = 500,
): DeviceSnapshot = coroutineScope {

    // All three start immediately - the slowest one sets the pace.
    val status = async { fetchDeviceStatus(deviceId) }
    val utilisation = async { fetchDeviceUtilisation(deviceId) }
    val temperature = async {
        withTimeoutOrNull(temperatureTimeoutMillis.milliseconds) {
            fetchDeviceTemperature(deviceId)
        }
    }

    val temperatureValue = temperature.await()

    if (temperatureValue == null) {
        DeviceSnapshot.Partial(deviceId, status.await(), utilisation.await())
    } else {
        DeviceSnapshot.Complete(deviceId, status.await(), utilisation.await(), temperatureValue)
    }
}

/**
 * 3b - the familiar shape once more.
 *
 * Each `deviceSnapshot` brings its own scope with three children, so a
 * slow device only delays its own subtree. The sibling devices are
 * unaffected.
 */
suspend fun dashboard(
    deviceIds: List<String>,
    temperatureTimeoutMillis: Long = 500,
): List<DeviceSnapshot> = coroutineScope {
    deviceIds
        .map { deviceId -> async { deviceSnapshot(deviceId, temperatureTimeoutMillis) } }
        .awaitAll()
}

/**
 * 3c - exhaustive `when`, no `else`.
 *
 * `deviceId` can be read before the branch because it is declared on the
 * sealed interface itself.
 */
fun renderLine(snapshot: DeviceSnapshot): String = when (snapshot) {
    is DeviceSnapshot.Complete ->
        "${snapshot.deviceId}: ${snapshot.status}, ${snapshot.utilisation} %, ${snapshot.temperature} °C"

    is DeviceSnapshot.Partial ->
        "${snapshot.deviceId}: ${snapshot.status}, ${snapshot.utilisation} %, temperature unavailable"
}

/** 3d - `count` with a type check as the predicate. */
fun countIncomplete(snapshots: List<DeviceSnapshot>): Int =
    snapshots.count { it is DeviceSnapshot.Partial }
