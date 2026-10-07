package block2

/**
 * Solution for exercise 5 - block 2 capstone.
 */

data class Reading(
    val deviceId: String,
    val second: Int,
    val value: Double,
)

sealed interface AnalysisResult<out T> {
    data class Ok<T>(val value: T) : AnalysisResult<T>
    data class NoData(val reason: String) : AnalysisResult<Nothing>
}

/**
 * 5a - the type bound `T : Comparable<T>` is what makes this work for
 * numbers and strings alike.
 *
 * Note how the two `...OrNull` calls collapse into a single null check:
 * if the list is empty, `minOrNull()` already returns null, so there is
 * no separate `isEmpty()` branch.
 */
fun <T : Comparable<T>> span(values: List<T>): AnalysisResult<Pair<T, T>> {
    val min = values.minOrNull() ?: return AnalysisResult.NoData("empty list")
    val max = values.max()   // safe: if min exists, so does max

    return AnalysisResult.Ok(min to max)
}

/**
 * 5b - `windowed` slides a window of the given size across the list.
 *
 * It returns an empty list when the input is shorter than the window,
 * so the edge case needs no extra handling.
 */
fun List<Reading>.movingAverage(windowSize: Int): List<Double> =
    windowed(windowSize) { window -> window.map { it.value }.average() }

/**
 * 5c - the reason for using a sequence.
 *
 * `filter` and `take` are lazy here: nothing is computed until `toList()`
 * asks for elements, and `take(count)` stops the upstream as soon as it
 * has enough. That is why this also works on an infinite source.
 *
 * The same chain on a `List` would filter all elements first - and never
 * finish on an infinite input.
 */
fun Sequence<Reading>.firstAnomalies(threshold: Double, count: Int): List<Reading> =
    filter { it.value > threshold }
        .take(count)
        .toList()

/** 5d - exhaustive `when`, no `else`. */
fun reportLine(result: AnalysisResult<Pair<Double, Double>>): String = when (result) {
    is AnalysisResult.Ok -> {
        // Destructuring works because Pair provides component1/component2.
        val (min, max) = result.value
        "span: $min to $max"
    }

    is AnalysisResult.NoData ->
        "no analysis possible (${result.reason})"
}

/**
 * 5e - groupBy plus mapValues, reusing the generic function from 5a.
 *
 * `groupBy` never produces an empty group, so `span` will always take the
 * Ok branch here. The NoData case still has to be handled by the caller,
 * because the return type says it can occur.
 */
fun analysisPerDevice(
    readings: List<Reading>,
): Map<String, AnalysisResult<Pair<Double, Double>>> =
    readings
        .groupBy { it.deviceId }
        .mapValues { (_, group) -> span(group.map { it.value }) }

val sampleReadings = listOf(
    Reading("sen-12", 0, 21.4),
    Reading("sen-12", 1, 22.8),
    Reading("sen-12", 2, 71.2),
    Reading("sen-12", 3, 24.1),
    Reading("cam-04", 0, 38.0),
    Reading("cam-04", 1, 39.5),
)

fun main() {
    println(span(listOf(3, 1, 4, 1, 5)))
    println(sampleReadings.filter { it.deviceId == "sen-12" }.movingAverage(2))
    println(sampleReadings.asSequence().firstAnomalies(30.0, 2))

    analysisPerDevice(sampleReadings).forEach { (deviceId, result) ->
        println("$deviceId: ${reportLine(result)}")
    }
}
