package exercise

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Exercise 1 (guided) - from sequential to parallel.
 *
 * `fetchStatus` takes 200 ms per device. Asking six devices one after
 * another therefore takes 1.2 seconds, although the calls do not depend
 * on each other at all.
 *
 * Verify with:
 *     ./gradlew :block3-coroutines-1:exerciseTest --tests "exercise.E1*"
 */

val sampleDeviceIds = listOf("cam-04", "cam-09", "rtr-01", "rtr-02", "int-07", "sen-12")

/**
 * Stands in for a network call. Do not change this function.
 *
 * Note that it SUSPENDS (delay) rather than blocking (Thread.sleep) -
 * that is what exercise 2 is about.
 */
suspend fun fetchStatus(deviceId: String): String {
    delay(200.milliseconds)
    return "$deviceId: online"
}

suspend fun fetchUtilisation(deviceId: String): Int {
    delay(200.milliseconds)
    return deviceId.length * 7
}

/**
 * Given as a reference - this one is already correct, just slow.
 * Six devices, 200 ms each, roughly 1200 ms in total.
 */
suspend fun collectStatusesSequentially(deviceIds: List<String>): List<String> =
    deviceIds.map { fetchStatus(it) }

/**
 * Exercise 1a
 *
 * Same result as [collectStatusesSequentially], but all requests run at
 * the same time. Six devices should take roughly 200 ms, not 1200 ms.
 *
 * The order of the results must match the order of the input.
 *
 * Useful: coroutineScope { }, async { }, awaitAll()
 */
suspend fun collectStatusesInParallel(deviceIds: List<String>): List<String> {
    TODO("1a: run the requests concurrently")
}

/**
 * Exercise 1b
 *
 * Fetches status AND utilisation for a single device - both at the same
 * time - and combines them into one line:
 *
 *     "cam-04: online, utilisation 42"
 *
 * Both calls take 200 ms, so the whole function should take about 200 ms
 * and not 400 ms.
 *
 * Note the common mistake shown in demo 2: both `async` blocks have to be
 * started BEFORE the first `await()`.
 */
suspend fun deviceSummary(deviceId: String): String {
    TODO("1b: fetch status and utilisation concurrently")
}

/**
 * Exercise 1c
 *
 * A summary for every device, again all at once. Six devices, two calls
 * each - still roughly 200 ms in total.
 *
 * Reuse [deviceSummary].
 */
suspend fun allSummaries(deviceIds: List<String>): List<String> {
    TODO("1c: build all summaries concurrently")
}
