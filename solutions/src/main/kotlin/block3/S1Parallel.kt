package block3

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Solution for exercise 1 - from sequential to parallel.
 */

val sampleDeviceIds = listOf("cam-04", "cam-09", "rtr-01", "rtr-02", "int-07", "sen-12")

suspend fun fetchStatus(deviceId: String): String {
    delay(200.milliseconds)
    return "$deviceId: online"
}

suspend fun fetchUtilisation(deviceId: String): Int {
    delay(200.milliseconds)
    return deviceId.length * 7
}

suspend fun collectStatusesSequentially(deviceIds: List<String>): List<String> =
    deviceIds.map { fetchStatus(it) }

/**
 * 1a - the standard shape for "do all of these at once".
 *
 * `map { async { … } }` starts every request; the list of Deferreds is
 * only awaited afterwards, by `awaitAll()`. Because the Deferreds stay in
 * input order, so do the results - no sorting needed.
 *
 * `coroutineScope` is what makes this safe: if one request fails, the
 * others are cancelled and the exception surfaces here rather than
 * disappearing.
 */
suspend fun collectStatusesInParallel(deviceIds: List<String>): List<String> =
    coroutineScope {
        deviceIds
            .map { deviceId -> async { fetchStatus(deviceId) } }
            .awaitAll()
    }

/**
 * 1b - both `async` calls happen before the first `await()`.
 *
 * Writing it as
 *     val status = async { … }.await()
 *     val utilisation = async { … }.await()
 * would compile and produce the same result, but take twice as long:
 * the first await blocks the progress of the second async.
 */
suspend fun deviceSummary(deviceId: String): String = coroutineScope {
    val status = async { fetchStatus(deviceId) }
    val utilisation = async { fetchUtilisation(deviceId) }

    "${status.await()}, utilisation ${utilisation.await()}"
}

/**
 * 1c - nesting works without any extra thought.
 *
 * Each `deviceSummary` opens its own `coroutineScope` with two children,
 * and all of those scopes are themselves children of this one. Twelve
 * requests run at the same time, and the whole tree is still cancelled
 * as a unit if anything fails.
 */
suspend fun allSummaries(deviceIds: List<String>): List<String> = coroutineScope {
    deviceIds
        .map { deviceId -> async { deviceSummary(deviceId) } }
        .awaitAll()
}
