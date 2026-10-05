package demo

/**
 * Demo 6 – Functional programming: the collections API and extensions.
 *
 * Two properties of the collections API are worth the attention:
 *
 *  1. The operations are **eager**. Every step creates a new list. The
 *     lazy counterpart is `Sequence` - see demo 9.
 *  2. The set of ready-made operations is large, because operations can
 *     be added as extension functions without changing the types.
 */

fun main() {
    basics()
    println()
    groupingAndAggregating()
    println()
    extensionFunctions()
    println()
    extensionProperties()
}

// ---------------------------------------------------------------- 1
private fun basics() {
    val devices = sampleDevices

    // map: transform
    println(devices.map { it.name })

    // filter: select
    println(devices.filter { it.utilisation > 50 }.map { it.id })

    // predicates
    println("all in service: ${devices.all { it.utilisation >= 0 }}")
    println("any critical: ${devices.any { it.utilisation >= 90 }}")
    println("critical count: ${devices.count { it.utilisation >= 90 }}")
    println("none above 100: ${devices.none { it.utilisation > 100 }}")

    // Single elements - always the ...OrNull variant when "not found" is
    // an ordinary case.
    println(devices.firstOrNull { it.type == DeviceType.SENSOR }?.name)
    println(devices.maxByOrNull { it.utilisation }?.name)
    println(devices.minByOrNull { it.utilisation }?.name)

    // Sorting - multi-level with compareBy
    val sorted = devices.sortedWith(compareBy({ it.type }, { -it.utilisation }))
    println(sorted.map { "${it.type}/${it.utilisation}" })

    // partition: split in one pass instead of filtering twice
    val (critical, uncritical) = devices.partition { it.utilisation >= 90 }
    println("critical=${critical.size}, uncritical=${uncritical.size}")

    // distinct
    val locations = devices.map { it.location }.distinct()
    println("locations: $locations")

    // zip: two lists pairwise
    val names = devices.map { it.name }
    val values = devices.map { it.utilisation }
    println(names.zip(values).take(2))

    // chunked / windowed - applicable to measurement series
    val measurements = sampleMeasurements.map { it.value }
    println("chunks:  ${measurements.chunked(2)}")
    println("windows: ${measurements.windowed(3).map { window -> "%.1f".format(window.average()) }}")
}

// ---------------------------------------------------------------- 2
private fun groupingAndAggregating() {
    val devices = sampleDevices

    // groupBy: Map<Key, List<Element>>
    val byType: Map<DeviceType, List<Device>> = devices.groupBy { it.type }
    byType.forEach { (type, list) -> println("$type: ${list.size}") }

    // With a second lambda, map straight onto the interesting property
    val namesByLocation = devices.groupBy({ it.location }, { it.name })
    println(namesByLocation)

    // associateBy: Map<Key, Element> - an index over a list
    val byId: Map<DeviceId, Device> = devices.associateBy { it.id }
    println(byId[DeviceId("rtr-01")]?.name)

    // Aggregation
    println("total utilisation: ${devices.sumOf { it.utilisation }}")
    println("average: %.1f".format(devices.map { it.utilisation }.average()))

    // fold: your own accumulator with a start value
    val allIds = devices.fold(StringBuilder()) { acc, device ->
        acc.append(device.id).append(" ")
    }
    println("fold: ${allIds.toString().trim()}")

    // joinToString already covers the most common fold case
    println("joinToString: ${devices.joinToString(", ", prefix = "[", postfix = "]") { it.id.value }}")

    // Combined: the highest utilisation per type
    val peaks = devices
        .groupBy { it.type }
        .mapValues { (_, list) -> list.maxOf { it.utilisation } }
    println("peaks: $peaks")
}

// ---------------------------------------------------------------- 3
/**
 * An extension function: extends a type without changing it and without
 * inheriting from it.
 *
 * Important for the mental model: nothing is written into the class. The
 * compiler turns this into a static function whose first parameter is the
 * receiver. That is why it is resolved **statically** - an extension
 * cannot override a method.
 */
fun Device.isCritical(): Boolean = utilisation >= 90

fun Device.shortDescription(): String = "$id (${type.displayName}) @ $location"

/** Extensions on generic types are particularly useful. */
fun List<Device>.critical(): List<Device> = filter { it.isCritical() }

/** An extension on a type you do not own - here from the standard library. */
fun Double.asPercentage(): String = "%.1f %%".format(this * 100)

private fun extensionFunctions() {
    val device = sampleDevices[2]
    println(device.shortDescription())
    println("critical: ${device.isCritical()}")
    println("critical in total: ${sampleDevices.critical().map { it.id }}")
    println(0.734.asPercentage())
}

// ---------------------------------------------------------------- 4
/**
 * An extension property: the same idea for properties.
 *
 * It cannot have a backing field - there is nowhere for it to store
 * anything. So it always needs a getter.
 */
val Device.utilisationLevel: String
    get() = when {
        utilisation < 10 -> "idle"
        utilisation < 60 -> "normal"
        utilisation < 90 -> "high"
        else -> "critical"
    }

val List<Device>.averageUtilisation: Double
    get() = if (isEmpty()) 0.0 else sumOf { it.utilisation }.toDouble() / size

private fun extensionProperties() {
    sampleDevices.forEach { println("${it.id}: ${it.utilisationLevel}") }
    println("average: %.1f".format(sampleDevices.averageUtilisation))
}
