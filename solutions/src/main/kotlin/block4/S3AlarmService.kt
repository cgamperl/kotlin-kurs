package block4

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Subject under test for exercise 3. Identical to the exercise version -
 * the solution is the test suite in src/test/kotlin/block4.
 */

enum class Severity { WARNING, CRITICAL }

data class Alarm(
    val deviceId: String,
    val severity: Severity,
    val message: String,
)

/** A dependency worth mocking: it may or may not know a device. */
interface DeviceLookup {
    suspend fun nameOf(deviceId: String): String?
}

/** A dependency worth verifying: did we publish, and what exactly? */
interface AlarmSink {
    suspend fun publish(alarm: Alarm)
}

class AlarmService(
    private val lookup: DeviceLookup,
    private val sink: AlarmSink,
) {

    /**
     * Raises an alarm for a device, depending on its utilisation:
     *
     *   below 60   no alarm at all, returns null, publishes nothing
     *   60 .. 89   WARNING
     *   90 and up  CRITICAL
     *
     * The message reads "<name> at <utilisation> %". If the lookup does
     * not know the device, its id is used in place of the name.
     *
     * Publishing is retried once: if the first `publish` throws, it waits
     * 200 ms and tries again. If the second attempt also throws, the
     * exception reaches the caller.
     */
    suspend fun raise(deviceId: String, utilisation: Int): Alarm? {
        require(utilisation in 0..100) { "utilisation must be within 0..100" }

        val severity = when {
            utilisation < 60 -> return null
            utilisation < 90 -> Severity.WARNING
            else -> Severity.CRITICAL
        }

        val name = lookup.nameOf(deviceId) ?: deviceId
        val alarm = Alarm(deviceId, severity, "$name at $utilisation %")

        try {
            sink.publish(alarm)
        } catch (e: IllegalStateException) {
            delay(200.milliseconds)
            sink.publish(alarm)
        }

        return alarm
    }

    /**
     * Raises alarms for several devices and returns only the ones that
     * were actually raised, in input order.
     */
    suspend fun raiseAll(utilisationByDevice: Map<String, Int>): List<Alarm> =
        utilisationByDevice.mapNotNull { (deviceId, utilisation) ->
            raise(deviceId, utilisation)
        }
}
