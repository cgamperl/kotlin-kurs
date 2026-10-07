package block4

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Solution for exercise 1 - resilience.
 */

class DeviceUnreachableException(deviceId: String) :
    IllegalStateException("device $deviceId is unreachable")

class FlakyDeviceClient(
    private val failuresBeforeSuccess: Int = 2,
    private val latencyMillis: Long = 100,
) {
    var callCount = 0
        private set

    suspend fun readStatus(deviceId: String): String {
        callCount++
        delay(latencyMillis.milliseconds)

        if (callCount <= failuresBeforeSuccess) {
            throw DeviceUnreachableException(deviceId)
        }
        return "$deviceId: online"
    }
}

/**
 * 1a - retry with exponential backoff.
 *
 * Two details matter:
 *
 *  - The catch is on `DeviceUnreachableException`, not on `Exception`.
 *    A broad catch would also swallow `CancellationException` and make
 *    this function impossible to cancel.
 *  - There is no backoff after the LAST attempt. Waiting before giving
 *    up helps nobody and shows up directly in the timing of the test.
 */
suspend fun readStatusWithRetry(
    client: FlakyDeviceClient,
    deviceId: String,
    maxAttempts: Int = 3,
    backoffMillis: Long = 100,
): String {
    var wait = backoffMillis.milliseconds

    repeat(maxAttempts - 1) {
        try {
            return client.readStatus(deviceId)
        } catch (e: DeviceUnreachableException) {
            delay(wait)
            wait *= 2
        }
    }

    // The final attempt is outside the loop: whatever it throws is what
    // the caller gets.
    return client.readStatus(deviceId)
}

/**
 * 1b - a timeout around the entire retry sequence.
 *
 * `withTimeoutOrNull` cancels the block from the outside. Because
 * `delay` and the suspension points of the client all cooperate, the
 * cancellation takes effect immediately - the test sees it stop at
 * exactly 1000 ms.
 */
suspend fun readStatusOrNull(
    client: FlakyDeviceClient,
    deviceId: String,
    maxAttempts: Int = 5,
    backoffMillis: Long = 100,
    timeoutMillis: Long = 1_000,
): String? = withTimeoutOrNull(timeoutMillis.milliseconds) {
    try {
        readStatusWithRetry(client, deviceId, maxAttempts, backoffMillis)
    } catch (e: DeviceUnreachableException) {
        null
    }
}

/**
 * 1c - `supervisorScope` instead of `coroutineScope`.
 *
 * This is the essential part of the exercise. With `coroutineScope`, the
 * first failing device would cancel all its siblings and the function
 * would return nothing at all. `supervisorScope` isolates the children,
 * so the try/catch around `await()` can turn a single failure into a
 * null entry.
 */
suspend fun readAllStatuses(
    clients: Map<String, FlakyDeviceClient>,
    maxAttempts: Int = 3,
    backoffMillis: Long = 100,
): Map<String, String?> = supervisorScope {
    clients
        .map { (deviceId, client) ->
            async {
                deviceId to try {
                    readStatusWithRetry(client, deviceId, maxAttempts, backoffMillis)
                } catch (e: DeviceUnreachableException) {
                    null
                }
            }
        }
        .awaitAll()
        .toMap()
}

/**
 * 1d - cleanup that survives cancellation.
 *
 * The `finally` block runs while the coroutine is already cancelled, and
 * in that state every suspending call fails immediately. `NonCancellable`
 * carves out a small area in which suspension is allowed again.
 *
 * Use it only for cleanup, and keep it short - code inside it genuinely
 * cannot be cancelled any more.
 */
suspend fun pollUntilCancelled(log: MutableList<String>, intervalMillis: Long = 100) {
    try {
        while (true) {
            delay(intervalMillis.milliseconds)
            log += "poll"
        }
    } finally {
        withContext(NonCancellable) {
            delay(10.milliseconds)          // stands in for closing a connection
            log += "closed"
        }
    }
}
