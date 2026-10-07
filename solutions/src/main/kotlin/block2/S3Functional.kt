package block2

/**
 * Solution for exercise 3 - the collections pipeline.
 *
 * Not a single loop, not a single `var`. Every function describes WHAT
 * should come out, not how to iterate towards it.
 */

val sampleTelemetry: List<Measurement> = listOf(
    Measurement("sen-12", 21.4),
    Measurement("sen-12", 22.8),
    Measurement("sen-12", 71.2),
    Measurement("cam-04", 38.0),
    Measurement("cam-04", 39.5),
    Measurement("rtr-01", 88.0),
    Measurement("rtr-01", 92.5),
    Measurement("rtr-01", 95.0),
)

/**
 * 3a - the standard shape for evaluations.
 *
 * `groupBy` yields Map<String, List<Measurement>>, `mapValues` transforms
 * the value side and leaves the keys untouched. The parameter of mapValues
 * is a Map.Entry - hence the destructuring `(_, list)`, in which the key
 * is discarded with `_`.
 */
fun averagePerDevice(measurements: List<Measurement>): Map<String, Double> =
    measurements
        .groupBy { it.deviceId }
        .mapValues { (_, values) -> values.map { it.value }.average() }

/**
 * 3b - the same scaffolding, a different aggregation.
 *
 * `maxOf` is available in the non-nullable variant because groupBy never
 * produces an empty group: every key has at least one element.
 */
fun peakPerDevice(measurements: List<Measurement>): Map<String, Double> =
    measurements
        .groupBy { it.deviceId }
        .mapValues { (_, values) -> values.maxOf { it.value } }

/** 3c - `filter` preserves the order of the input. */
fun outliers(measurements: List<Measurement>, limit: Double): List<Measurement> =
    measurements.filter { it.value > limit }

/**
 * 3d - building on 3a.
 *
 * `toList()` on a map yields List<Pair<K, V>>; after that you sort by the
 * second component and keep only the keys at the end.
 */
fun topDevices(measurements: List<Measurement>, count: Int): List<String> =
    averagePerDevice(measurements)
        .toList()
        .sortedByDescending { (_, average) -> average }
        .take(count)
        .map { (deviceId, _) -> deviceId }

/**
 * 3e - an extension property.
 *
 * It necessarily has a getter: there is nowhere for it to store a value,
 * because the class `Measurement` knows nothing about it.
 */
val Measurement.isNotable: Boolean
    get() = value >= 70.0

/**
 * 3f - an extension function on a generic type.
 *
 * `distinctBy` counts the devices without first building an intermediate
 * list of names. `count { … }` is the short form of `filter { … }.size`
 * and gets by without an intermediate list too.
 */
fun List<Measurement>.summarise(): String {
    if (isEmpty()) return "no measurements"

    val devices = distinctBy { it.deviceId }.size
    val notable = count { it.isNotable }

    return "$size measurements from $devices devices, $notable notable"
}

fun main() {
    println(averagePerDevice(sampleTelemetry))
    println(peakPerDevice(sampleTelemetry))
    println(outliers(sampleTelemetry, 70.0))
    println(topDevices(sampleTelemetry, 2))
    println(sampleTelemetry.summarise())
}
