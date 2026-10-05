package demo

/**
 * Demo 7 – Lambda expressions.
 *
 * The syntax is quickly explained. What is interesting are the rules
 * around it: the trailing lambda convention, `it`, function references -
 * and why Kotlin lambdas often create no objects at all.
 */

// ------------------------------------------------------------------ 1
/**
 * A higher-order function: it takes a function as a parameter.
 *
 * The type `(Device) -> Boolean` reads like the signature it describes.
 */
fun countWhere(devices: List<Device>, predicate: (Device) -> Boolean): Int {
    var hits = 0
    for (device in devices) {
        if (predicate(device)) hits++
    }
    return hits
}

/**
 * A function that RETURNS a function.
 * The lambda accesses `limit` - the variable outlives the call.
 */
fun thresholdCheck(limit: Int): (Device) -> Boolean =
    { device -> device.utilisation >= limit }

/**
 * `inline` makes the compiler paste the body, lambda included, into the
 * call site. No function object is created at runtime - which is why the
 * entire collections API in Kotlin is inline.
 */
inline fun withTiming(name: String, block: () -> Unit) {
    val start = System.nanoTime()
    block()
    val millis = (System.nanoTime() - start) / 1_000_000.0
    println("  $name: %.2f ms".format(millis))
}

fun main() {
    val devices = sampleDevices

    // ---- notations, from verbose to idiomatic
    println(countWhere(devices, { device: Device -> device.utilisation > 50 }))
    println(countWhere(devices, { device -> device.utilisation > 50 }))   // type inferred

    // Trailing lambda: when the lambda is the LAST parameter, it may move
    // outside the parentheses. This is exactly why Kotlin code often looks
    // as though it had its own language constructs.
    println(countWhere(devices) { device -> device.utilisation > 50 })

    // `it` - the implicit name when there is exactly one parameter.
    println(countWhere(devices) { it.utilisation > 50 })

    // Rule of thumb: use `it` only for short, obvious lambdas. As soon as
    // they nest, give the parameter a name.

    // ---- when the only parameter is a lambda, the parentheses vanish:
    println()
    withTiming("summing") {
        (1..1_000_000).sum()
    }

    // ---- functions as values
    println()
    val critical: (Device) -> Boolean = thresholdCheck(90)
    val busy = thresholdCheck(50)
    println("critical: ${devices.count(critical)}")
    println("busy:     ${devices.count(busy)}")

    // ---- function references with ::
    println()
    // to a property
    println(devices.map(Device::name))
    // to an extension from demo 6
    println(devices.filter(Device::isCritical).map { it.id })
    // to a constructor
    val ids = listOf("cam-99", "rtr-99").map(::DeviceId)
    println(ids)

    // ---- several parameters, and `_` for what does not matter
    println()
    val byType = devices.groupBy { it.type }
    byType.forEach { (type, list) -> println("$type -> ${list.size}") }
    byType.forEach { (_, list) -> print("${list.size} ") }
    println()

    // ---- returning from a lambda
    println()
    val firstCritical = devices.firstOrNull { device ->
        // Inside a lambda the last expression is the return value.
        // A bare `return` would return from main(), not from the lambda -
        // which is why there is the labelled `return@`.
        if (device.type == DeviceType.SENSOR) return@firstOrNull false
        device.utilisation >= 90
    }
    println("first critical non-sensor device: ${firstCritical?.name}")

    // ---- SAM conversion: a Java interface with exactly one method takes
    // a lambda directly.
    val task = Runnable { println("running as a Runnable") }
    task.run()
}
