package exercise

/**
 * Exercise 1 – translating a class hierarchy to Kotlin.
 *
 * The original class hierarchy is printed in the module README. This file
 * is the Kotlin skeleton for it: classes, interfaces and signatures are
 * already there, the bodies are missing.
 *
 * Points of interest:
 *  - `abstract val` for a property every subclass has to supply
 *  - `open` - without the keyword nothing could be overridden
 *  - properties with `get()` instead of a getter method
 *
 * Verify with:
 *     ./gradlew :block2-grundlagen-2:exerciseTest --tests "exercise.E1*"
 */

abstract class Component(val id: String) {

    /**
     * Exercise 1a
     *
     * An abstract property - every subclass has to supply it.
     * Nothing to do here; it is listed so the numbering stays complete.
     */
    abstract val maintenanceIntervalMonths: Int

    /**
     * Exercise 1b
     *
     * Returns "<id>: ok".
     *
     * `open` means: may be overridden. Without it the method is final.
     */
    open fun selfTest(): String {
        TODO("1b: the default self test")
    }

    /**
     * Exercise 1c
     *
     * Returns the simple class name with the id in parentheses,
     * for example: "Camera(cam-04)".
     *
     * Useful: this::class.simpleName
     */
    override fun toString(): String {
        TODO("1c: class name with id")
    }
}

/** An interface with one property and one method. */
interface Calibratable {
    val offset: Double
    fun calibrate(value: Double)
}

/**
 * Exercise 1d
 *
 * A camera is maintained every 6 months.
 * Its self test returns "<id>: video signal ok (<resolution>)",
 * for example: "cam-04: video signal ok (1080p)".
 */
class Camera(id: String, val resolution: String) : Component(id) {

    override val maintenanceIntervalMonths: Int
        get() = TODO("1d: the camera's maintenance interval")

    override fun selfTest(): String {
        TODO("1d: the camera's self test")
    }
}

/**
 * Exercise 1e
 *
 * A sensor is maintained every 24 months and is calibratable.
 *
 * - `offset` starts at 0.0 and may only be read from the outside.
 * - `calibrate(value)` ADDS the given value to the current offset
 *   (it does not replace it!).
 * - The self test returns "<id>: measuring in <unit>, offset <offset>",
 *   for example: "sen-12: measuring in °C, offset 1.5".
 */
class Sensor(id: String, val unit: String) : Component(id), Calibratable {

    override val maintenanceIntervalMonths: Int
        get() = TODO("1e: the sensor's maintenance interval")

    override var offset: Double = 0.0
        private set

    override fun calibrate(value: Double) {
        TODO("1e: carry the offset forward")
    }

    override fun selfTest(): String {
        TODO("1e: the sensor's self test")
    }
}

/**
 * Exercise 1f
 *
 * Produces an overview of all components, one line each:
 *
 *     Camera(cam-04) – maintenance every 6 months
 *     Sensor(sen-12) – maintenance every 24 months
 *
 * The lines are joined with "\n".
 */
fun maintenanceOverview(components: List<Component>): String {
    TODO("1f: produce the overview")
}

fun main() {
    val components = listOf(
        Camera("cam-04", "1080p"),
        Sensor("sen-12", "°C"),
    )
    components.forEach { println(it.selfTest()) }
    println(maintenanceOverview(components))
}
