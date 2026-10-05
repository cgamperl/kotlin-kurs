package exercise

/**
 * Exercise 2 – dispatchers: keeping blocking work off the thread of the caller.
 *
 * The functions below are blocking. They stand in for what you really find
 * in a code base: a JDBC driver, a file read, a legacy library, an HTTP
 * client without a suspending API.
 *
 * Calling them straight from a coroutine is the single most common mistake
 * when starting with coroutines: the coroutine no longer suspends, it
 * blocks – and with it the thread, and with that thread everything else
 * scheduled on it.
 *
 * Verify with:
 *     ./gradlew :block3-coroutines-1:exerciseTest --tests "exercise.E2*"
 */

/** Blocking on purpose. Do not change. */
fun readConfigurationBlocking(deviceId: String): String {
    Thread.sleep(150)
    return "$deviceId:port=9100"
}

/** CPU-bound on purpose. Do not change. */
fun checksumBlocking(input: String): Int {
    var result = 0
    repeat(2_000_000) { i ->
        result = (result + input.hashCode() + i) % 1_000_003
    }
    return result
}

/**
 * Exercise 2a
 *
 * Make [readConfigurationBlocking] usable from a coroutine without
 * blocking the calling thread.
 *
 * Which dispatcher fits blocking I/O?
 *
 * Useful: withContext(...)
 */
suspend fun readConfiguration(deviceId: String): String {
    TODO("2a: move the blocking read to a suitable dispatcher")
}

/**
 * Exercise 2b
 *
 * Read the configuration of several devices concurrently.
 * Order of results follows order of input.
 *
 * Twelve devices at 150 ms each must not take 1.8 seconds.
 */
suspend fun readConfigurations(deviceIds: List<String>): List<String> {
    TODO("2b: read all configurations concurrently")
}

/**
 * Exercise 2c
 *
 * Same idea for CPU-bound work – but a different dispatcher.
 *
 * Be aware of what the test can and cannot check: `Dispatchers.IO` and
 * `Dispatchers.Default` share the same thread pool, so the thread name
 * does not tell them apart and no test can verify your choice here. The
 * distinction is a design decision:
 *
 *   - IO may grow far beyond the core count, because its threads are
 *     expected to sit and wait.
 *   - Default is capped at the core count, because more parallel CPU work
 *     than you have cores buys nothing.
 *
 * Putting CPU work on IO lets dozens of threads fight over the same cores.
 * Putting blocking I/O on Default starves everything else.
 */
suspend fun checksum(input: String): Int {
    TODO("2c: move the CPU-bound work to a suitable dispatcher")
}
