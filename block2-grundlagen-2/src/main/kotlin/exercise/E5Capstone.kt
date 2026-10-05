package exercise

/**
 * Exercise 5 – Block 2 capstone: telemetry analysis.
 *
 * This is where everything from block 2 comes together: a generic result
 * type as a sealed interface, a generic function with a type bound,
 * extension functions, the collections API and a sequence.
 *
 * Verify with:
 *     ./gradlew :block2-grundlagen-2:exerciseTest --tests "exercise.E5*"
 */

data class Reading(
    val deviceId: String,
    val second: Int,
    val value: Double,
)

/**
 * The generic result type.
 *
 * `out T` makes it covariant. That is why `NoData` can be declared as
 * `AnalysisResult<Nothing>` and still be used wherever an
 * `AnalysisResult<X>` is expected: `Nothing` is a subtype of every type.
 *
 * Nothing to do here – this type is the basis for 5a through 5e.
 */
sealed interface AnalysisResult<out T> {
    data class Ok<T>(val value: T) : AnalysisResult<T>
    data class NoData(val reason: String) : AnalysisResult<Nothing>
}

/**
 * Exercise 5a – generic function with a type bound
 *
 * Returns the smallest and the largest element of a list as a pair.
 *
 *   - not empty -> Ok(min to max)
 *   - empty     -> NoData("empty list")
 *
 * The bound `T : Comparable<T>` is what makes min/max meaningful in the
 * first place. Without it the function would not compile.
 *
 * Useful: minOrNull(), maxOrNull(), the infix function `to`
 */
fun <T : Comparable<T>> span(values: List<T>): AnalysisResult<Pair<T, T>> {
    TODO("5a: determine the span")
}

/**
 * Exercise 5b – extension function using `windowed`
 *
 * Moving average over the readings, using the given window size.
 * Four readings with a window size of 2 produce three averages.
 *
 * If the list is shorter than the window, the result is empty.
 *
 * Useful: windowed(size), average()
 */
fun List<Reading>.movingAverage(windowSize: Int): List<Double> {
    TODO("5b: compute the moving average")
}

/**
 * Exercise 5c – sequence
 *
 * Finds the first `count` readings above the given threshold.
 *
 * The parameter is deliberately a `Sequence` and not a `List`: the source
 * may be very large or even infinite. Your solution must therefore NOT
 * materialise it with `toList()` first – it has to stop reading as soon
 * as enough matches have been found.
 *
 * One test verifies this using an infinite sequence.
 */
fun Sequence<Reading>.firstAnomalies(threshold: Double, count: Int): List<Reading> {
    TODO("5c: take the first anomalies from the sequence")
}

/**
 * Exercise 5d – exhaustive `when` over the result type
 *
 * Produces a report line:
 *
 *   Ok(21.4 to 71.2)      -> "span: 21.4 to 71.2"
 *   NoData("empty list")  -> "no analysis possible (empty list)"
 *
 * Write the `when` without an `else` branch.
 */
fun reportLine(result: AnalysisResult<Pair<Double, Double>>): String {
    TODO("5d: build the report line")
}

/**
 * Exercise 5e – putting it together
 *
 * Analyses the readings per device and returns the span of each device's
 * values.
 *
 * For the sample data:
 *     sen-12 -> Ok(21.4 to 71.2)
 *     cam-04 -> Ok(38.0 to 39.5)
 *
 * Useful: groupBy, mapValues and the function from 5a
 */
fun analysisPerDevice(
    readings: List<Reading>,
): Map<String, AnalysisResult<Pair<Double, Double>>> {
    TODO("5e: analyse per device")
}

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
