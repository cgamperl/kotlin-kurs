package exercise

import kotlinx.coroutines.flow.Flow

/**
 * Exercise 4 - capstone project: the monitoring dashboard.
 *
 * This is the project the block ends with. It pulls together everything:
 * concurrency, timeouts, partial failure, sealed state, flows, and a test
 * suite that uses virtual time.
 *
 * The full brief, the acceptance criteria and the optional extensions are
 * in `praxisprojekt/README.md` next to this module.
 *
 * Verify with:
 *     ./gradlew :block4-coroutines-2:exerciseTest --tests "exercise.E4*"
 */

data class DeviceStatus(
    val deviceId: String,
    val utilisation: Int,
)

enum class Level { IDLE, NORMAL, HIGH, CRITICAL }

/**
 * The outcome of asking one device. Three things can happen, and each
 * carries its own data - that is why this is a sealed interface and not
 * a nullable DeviceStatus with a side channel for errors.
 */
sealed interface DeviceReport {
    val deviceId: String

    data class Ok(
        override val deviceId: String,
        val utilisation: Int,
        val level: Level,
    ) : DeviceReport

    data class Unreachable(
        override val deviceId: String,
        val reason: String,
    ) : DeviceReport

    data class TimedOut(
        override val deviceId: String,
        val afterMillis: Long,
    ) : DeviceReport
}

/**
 * The outside world. In the tests it is replaced by a fake; in reality
 * it would be an HTTP or field-bus client.
 */
interface DeviceGateway {
    /** May throw, and may take arbitrarily long. */
    suspend fun status(deviceId: String): DeviceStatus

    /** A continuous stream of readings. May never end. */
    fun readings(deviceId: String): Flow<Double>
}

class MonitoringService(
    private val gateway: DeviceGateway,
) {

    /**
     * Task 1
     *
     * Asks a single device and turns the three possible outcomes into a
     * [DeviceReport]:
     *
     *   answered in time      -> Ok, with the level derived from the
     *                            utilisation (see [levelOf])
     *   threw an exception    -> Unreachable, with the exception message
     *                            (use "unknown error" if the message is null)
     *   took too long         -> TimedOut, with timeoutMillis
     *
     * Do not let a `CancellationException` be turned into `Unreachable` -
     * catch the exception types you actually expect.
     */
    suspend fun report(deviceId: String, timeoutMillis: Long = 500): DeviceReport {
        TODO("task 1: turn one device query into a report")
    }

    /**
     * Task 2
     *
     * Asks all devices CONCURRENTLY. A device that fails or times out
     * must not affect the others.
     *
     * Order of results follows order of input.
     */
    suspend fun dashboard(
        deviceIds: List<String>,
        timeoutMillis: Long = 500,
    ): List<DeviceReport> {
        TODO("task 2: build the whole dashboard concurrently")
    }

    /**
     * Task 3
     *
     * Merges the reading streams of all devices into ONE stream of alert
     * messages. A reading at or above [threshold] becomes:
     *
     *     "cam-04: 91.0"
     *
     * All devices are observed at the same time - an alert from a device
     * that emits early must not have to wait for a slower one.
     *
     * Useful: kotlinx.coroutines.flow.merge
     */
    fun alerts(deviceIds: List<String>, threshold: Double): Flow<String> {
        TODO("task 3: merge the streams into one alert stream")
    }

    /**
     * Task 4
     *
     * A single line summarising the dashboard:
     *
     *     "5 devices: 3 ok, 1 unreachable, 1 timed out"
     *
     * Use an exhaustive `when` without `else`.
     */
    fun summarise(reports: List<DeviceReport>): String {
        TODO("task 4: summarise the dashboard")
    }

    companion object {
        /**
         * Task 5
         *
         * The level thresholds:
         *
         *   below 10   IDLE
         *   10 .. 59   NORMAL
         *   60 .. 89   HIGH
         *   90 and up  CRITICAL
         */
        fun levelOf(utilisation: Int): Level {
            TODO("task 5: map a utilisation to a level")
        }
    }
}
