package block3

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Solution for exercise 2 - dispatchers.
 */

fun readConfigurationBlocking(deviceId: String): String {
    Thread.sleep(150)
    return "$deviceId:port=9100"
}

fun checksumBlocking(input: String): Int {
    var result = 0
    repeat(2_000_000) { i ->
        result = (result + input.hashCode() + i) % 1_000_003
    }
    return result
}

/**
 * 2a - the standard wrapper for blocking I/O.
 *
 * `withContext` does not start a new coroutine; it moves the current one
 * to another dispatcher for the duration of the block and back afterwards.
 * The caller notices nothing except that the function suspends.
 *
 * This is where the wrapper belongs: right at the blocking call, not at
 * every call site. The suspend function now keeps its promise of not
 * blocking whoever calls it.
 */
suspend fun readConfiguration(deviceId: String): String =
    withContext(Dispatchers.IO) {
        readConfigurationBlocking(deviceId)
    }

/**
 * 2b - the same async/awaitAll shape as in exercise 1.
 *
 * Worth noticing: nothing here mentions a dispatcher. Because
 * `readConfiguration` already handles that internally, the calling code
 * stays free of dispatcher decisions.
 */
suspend fun readConfigurations(deviceIds: List<String>): List<String> =
    coroutineScope {
        deviceIds
            .map { deviceId -> async { readConfiguration(deviceId) } }
            .awaitAll()
    }

/**
 * 2c - CPU-bound work goes to Default.
 *
 * Technically this would also work on IO. The difference shows up under
 * load: IO may grow to dozens of threads, and dozens of threads doing
 * pure computation on a handful of cores only add context switches.
 */
suspend fun checksum(input: String): Int =
    withContext(Dispatchers.Default) {
        checksumBlocking(input)
    }
