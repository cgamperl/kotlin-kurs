package exercise

import kotlinx.coroutines.flow.Flow

/**
 * Exercise 2 - build your own Flow pipeline.
 *
 * A sensor sends a stream of readings. You build the processing chain:
 * filtering, aggregating, error handling, and a terminal operator that
 * turns the stream into a single result.
 *
 * Verify with:
 *     ./gradlew :block4-coroutines-2:exerciseTest --tests "exercise.E2*"
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
 * Exercise 2a
 *
 * Turns a list of readings into a Flow that emits them one by one,
 * waiting [intervalMillis] BEFORE each emission.
 *
 * The flow must be cold: collecting it twice runs it twice.
 *
 * Useful: the flow { } builder, emit, delay
 */
fun sensorFlow(
    readings: List<SensorEvent>,
    intervalMillis: Long = 100,
): Flow<SensorEvent> {
    TODO("2a: build a cold flow from the readings")
}

/**
 * Exercise 2b
 *
 * Keeps only the readings at or above [threshold] and turns each one
 * into a message:
 *
 *     "sen-12: 71.2 above 70.0"
 *
 * Useful: filter, map
 */
fun Flow<SensorEvent>.anomalyMessages(threshold: Double): Flow<String> {
    TODO("2b: filter anomalies and format them")
}

/**
 * Exercise 2c
 *
 * Emits the running average after every reading. For the values
 * 10, 20, 30 the result is 10.0, 15.0, 20.0.
 *
 * Useful: runningFold with a Pair as the accumulator, then map -
 * or scan, which is the same thing under an older name.
 *
 * Careful: `runningFold` also emits the INITIAL value. Your result must
 * contain exactly as many elements as the input.
 */
fun Flow<SensorEvent>.runningAverage(): Flow<Double> {
    TODO("2c: emit the running average")
}

/**
 * Exercise 2d
 *
 * Makes the stream resilient: if the upstream flow fails, [fallback] is
 * emitted and the flow completes normally instead of throwing.
 *
 * Readings emitted before the failure must still reach the collector.
 *
 * Useful: catch
 */
fun Flow<SensorEvent>.withFallback(fallback: SensorEvent): Flow<SensorEvent> {
    TODO("2d: emit a fallback instead of failing")
}

/**
 * Exercise 2e
 *
 * A terminal operator: consumes the whole flow and condenses it into one
 * [StreamSummary].
 *
 * For an empty flow the result is StreamSummary(0, 0.0, 0.0).
 *
 * Useful: toList and then the collections API - or fold, if you would
 * rather not hold everything in memory.
 */
suspend fun Flow<SensorEvent>.summarise(): StreamSummary {
    TODO("2e: condense the stream into a summary")
}
