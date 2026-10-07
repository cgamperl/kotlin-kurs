package block4

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Reference solution for the capstone project - monitoring dashboard.
 */

data class DeviceStatus(
    val deviceId: String,
    val utilisation: Int,
)

enum class Level { IDLE, NORMAL, HIGH, CRITICAL }

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

interface DeviceGateway {
    suspend fun status(deviceId: String): DeviceStatus
    fun readings(deviceId: String): Flow<Double>
}

class MonitoringService(
    private val gateway: DeviceGateway,
) {

    /**
     * Task 1 - three outcomes, three branches.
     *
     * Note the order: the try/catch sits INSIDE `withTimeoutOrNull`.
     *
     * The catch names `IllegalStateException` deliberately. A blanket
     * `catch (e: Exception)` would also swallow the cancellation that
     * implements the timeout, and the timeout would stop working.
     */
    suspend fun report(deviceId: String, timeoutMillis: Long = 500): DeviceReport {
        val result = withTimeoutOrNull(timeoutMillis.milliseconds) {
            try {
                val status = gateway.status(deviceId)
                DeviceReport.Ok(deviceId, status.utilisation, levelOf(status.utilisation))
            } catch (e: IllegalStateException) {
                DeviceReport.Unreachable(deviceId, e.message ?: "unknown error")
            }
        }

        return result ?: DeviceReport.TimedOut(deviceId, timeoutMillis)
    }

    /**
     * Task 2 - `supervisorScope`, not `coroutineScope`.
     *
     * In this particular solution `report` never throws, so
     * `coroutineScope` would work too. `supervisorScope` is still the
     * honest choice: it states that these devices are independent, and
     * it keeps the dashboard working if `report` ever starts propagating
     * something.
     */
    suspend fun dashboard(
        deviceIds: List<String>,
        timeoutMillis: Long = 500,
    ): List<DeviceReport> = supervisorScope {
        deviceIds
            .map { deviceId -> async { report(deviceId, timeoutMillis) } }
            .awaitAll()
    }

    /**
     * Task 3 - `merge` collects several flows at once.
     *
     * The difference from `flatMapConcat` matters here: `merge` subscribes
     * to all sources simultaneously, so a device that emits early is not
     * held up by a slow one. `flatMapConcat` would drain the first flow
     * completely before even starting the second - and with an endless
     * source it would never get there.
     *
     * Note that the result is unordered by nature: whichever device emits
     * first wins. That is why the test uses shouldContainExactlyInAnyOrder.
     */
    fun alerts(deviceIds: List<String>, threshold: Double): Flow<String> =
        deviceIds
            .map { deviceId ->
                gateway.readings(deviceId)
                    .filter { it >= threshold }
                    .map { "$deviceId: $it" }
            }
            .merge()

    /**
     * Task 4 - exhaustive `when` inside a fold.
     *
     * `count { it is … }` three times would be shorter but would walk the
     * list three times and, more importantly, would not fail to compile
     * when a fourth report type is added. The `when` does.
     */
    fun summarise(reports: List<DeviceReport>): String {
        var ok = 0
        var unreachable = 0
        var timedOut = 0

        for (report in reports) {
            when (report) {
                is DeviceReport.Ok -> ok++
                is DeviceReport.Unreachable -> unreachable++
                is DeviceReport.TimedOut -> timedOut++
            }
        }

        return "${reports.size} devices: $ok ok, $unreachable unreachable, $timedOut timed out"
    }

    companion object {
        /** Task 5 - `when` without an argument reads best for ranges. */
        fun levelOf(utilisation: Int): Level = when {
            utilisation < 10 -> Level.IDLE
            utilisation < 60 -> Level.NORMAL
            utilisation < 90 -> Level.HIGH
            else -> Level.CRITICAL
        }
    }
}
