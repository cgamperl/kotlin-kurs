package block2

/**
 * Solution for exercise 1 - class hierarchy.
 *
 * Four things the translation makes visible:
 *
 *  - An abstract property is one line: `abstract val x: Int`.
 *  - `open` is required for anything that may be overridden; without it
 *    the member is final.
 *  - The constructor moves into the class header.
 *  - `override` is mandatory, not optional.
 */

abstract class Component(val id: String) {

    abstract val maintenanceIntervalMonths: Int

    open fun selfTest(): String = "$id: ok"

    /**
     * `this::class.simpleName` is the counterpart of `GetType().Name`.
     * Its type is `String?`, because anonymous objects have no simple
     * name - hence the Elvis operator.
     */
    override fun toString(): String = "${this::class.simpleName ?: "Component"}($id)"
}

interface Calibratable {
    val offset: Double
    fun calibrate(value: Double)
}

class Camera(id: String, val resolution: String) : Component(id) {

    // A constant property - an initialiser is enough here; a get() would
    // mean pointless work on every access.
    override val maintenanceIntervalMonths = 6

    override fun selfTest(): String = "$id: video signal ok ($resolution)"
}

class Sensor(id: String, val unit: String) : Component(id), Calibratable {

    override val maintenanceIntervalMonths = 24

    /**
     * `private set` is Kotlin's counterpart to a property with a public
     * getter and a private setter. Readable from the outside, writable
     * inside the class.
     */
    override var offset: Double = 0.0
        private set

    override fun calibrate(value: Double) {
        offset += value
    }

    override fun selfTest(): String = "$id: measuring in $unit, offset $offset"
}

/**
 * `joinToString` with "\n" replaces the loop and the StringBuilder.
 *
 * As a side note: for an empty list this yields "" without any special
 * handling - exactly what the test expects.
 */
fun maintenanceOverview(components: List<Component>): String =
    components.joinToString("\n") { "$it - maintenance every ${it.maintenanceIntervalMonths} months" }

fun main() {
    val components = listOf(
        Camera("cam-04", "1080p"),
        Sensor("sen-12", "°C"),
    )
    components.forEach { println(it.selfTest()) }
    println(maintenanceOverview(components))
}
