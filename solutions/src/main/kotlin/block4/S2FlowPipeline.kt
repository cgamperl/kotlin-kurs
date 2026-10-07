package block4

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlin.time.Duration.Companion.milliseconds

/**
 * Solution for exercise 2 - Flow pipeline.
 */

data class SensorEvent(
    val deviceId: String,
    val value: Double,
)

data class StreamSummary(
    val count: Int,
    val maximum: Double,
    val average: Double,
)

val sampleEvents = listOf(
    SensorEvent("sen-12", 21.4),
    SensorEvent("sen-12", 22.8),
    SensorEvent("sen-12", 71.2),
    SensorEvent("sen-12", 24.1),
    SensorEvent("sen-12", 85.0),
)

/**
 * 2a - the flow builder.
 *
 * The body runs once per collector, which is exactly what "cold" means.
 * Nothing runs at the point where this function returns; it only
 * describes what should happen.
 */
fun sensorFlow(
    readings: List<SensorEvent>,
    intervalMillis: Long = 100,
): Flow<SensorEvent> = flow {
    for (reading in readings) {
        delay(intervalMillis.milliseconds)
        emit(reading)
    }
}

/**
 * 2b - filter and map, exactly as on a List.
 *
 * Worth noticing: these operators are not terminal. Nothing here iterates
 * anything; the chain is only built. It runs when somebody collects.
 */
fun Flow<SensorEvent>.anomalyMessages(threshold: Double): Flow<String> =
    filter { it.value >= threshold }
        .map { "${it.deviceId}: ${it.value} above $threshold" }

/**
 * 2c - the running average.
 *
 * `runningFold` carries a pair of (sum, count) along and emits after
 * every step. Its first emission is the INITIAL value, which is not a
 * real average - hence `drop(1)`. Forgetting that is the usual mistake,
 * and the test that checks the element count catches it.
 */
fun Flow<SensorEvent>.runningAverage(): Flow<Double> =
    runningFold(0.0 to 0) { (sum, count), event ->
        (sum + event.value) to (count + 1)
    }
        .drop(1)
        .map { (sum, count) -> sum / count }

/**
 * 2d - `catch` handles failures from upstream.
 *
 * Inside `catch` you may emit, which is how the fallback gets into the
 * stream. Everything emitted before the failure has already reached the
 * collector and stays there.
 *
 * Note that `catch` deliberately does NOT see exceptions thrown by the
 * collector itself - otherwise a bug in the consumer would look like a
 * failure of the producer.
 */
fun Flow<SensorEvent>.withFallback(fallback: SensorEvent): Flow<SensorEvent> =
    catch { emit(fallback) }

/**
 * 2e - a terminal operator built with `fold`.
 *
 * `toList()` followed by the collections API would be shorter and is
 * perfectly fine for a bounded stream. `fold` is shown here because it
 * keeps only the accumulator in memory, which is what you want for a
 * stream that may be long.
 */
suspend fun Flow<SensorEvent>.summarise(): StreamSummary {
    val (count, sum, maximum) = fold(Triple(0, 0.0, Double.NEGATIVE_INFINITY)) { acc, event ->
        val (c, s, m) = acc
        Triple(c + 1, s + event.value, maxOf(m, event.value))
    }

    if (count == 0) return StreamSummary(0, 0.0, 0.0)

    return StreamSummary(count, maximum, sum / count)
}
