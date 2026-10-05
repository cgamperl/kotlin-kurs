package demo

/**
 * Demo 5 – Data classes and value classes.
 *
 * This is where the model appears that accompanies us through the whole
 * course: devices and their readings.
 */

/**
 * Value class: a bare String at runtime, but its own type in the type system.
 *
 * The benefit shows up in the signature further down: `label(DeviceId, Celsius)`
 * cannot be called with the arguments swapped, and a `DeviceId` can never be
 * confused with a device name - even though both are "just" strings.
 *
 * `@JvmInline` means NO wrapper object is created at runtime, as long as the
 * value is not needed as an object (in a list, for example).
 */
@JvmInline
value class DeviceId(val value: String) {
    init {
        // Value classes may check invariants - a good place for it, because
        // there is no way around the constructor.
        require(value.isNotBlank()) { "DeviceId must not be blank" }
    }

    override fun toString(): String = value
}

/** A second value class - prevents mixing up degrees and percent. */
@JvmInline
value class Celsius(val value: Double) {
    val isCritical: Boolean get() = value > 70.0

    override fun toString(): String = "%.1f °C".format(value)
}

enum class DeviceType { CAMERA, INTERCOM, ROUTER, SENSOR }

/**
 * Data class.
 *
 * From this the compiler generates equals(), hashCode(), toString(), copy()
 * and the componentN() functions for destructuring - each based on the
 * parameters of the PRIMARY CONSTRUCTOR.
 *
 * Important and often overlooked: properties declared in the body rather
 * than in the constructor do NOT count (see `note` below).
 */
data class Device(
    val id: DeviceId,
    val name: String,
    val type: DeviceType,
    val firmware: String = "1.0.0",     // default argument
) {
    // Not part of equals/hashCode/toString/copy:
    var note: String = ""
}

fun main() {
    val camera = Device(DeviceId("cam-04"), "Camera Studio B", DeviceType.CAMERA)

    // 1) toString() comes for free and is readable.
    println(camera)

    // 2) equals() compares values, not references.
    val same = Device(DeviceId("cam-04"), "Camera Studio B", DeviceType.CAMERA)
    println("equal: ${camera == same}")             // true
    println("identical: ${camera === same}")        // false - two objects

    // 3) copy(): change by creating a new object instead of mutating.
    //    This is the standard way of moving state forward.
    val updated = camera.copy(firmware = "2.1.4")
    println(updated)
    println("original unchanged: $camera")

    // 4) Destructuring - uses the generated componentN() functions.
    val (id, name, type) = updated
    println("$id / $name / $type")

    // 5) The trap with the body property:
    val withNote = camera.copy().also { it.note = "replacement scheduled" }
    println("equal despite different notes: ${camera == withNote}")   // true!
    println("copy() does not carry the note along: '${camera.copy().note}'")

    // 6) Value classes in action.
    val temperature = Celsius(81.5)
    println("$temperature critical: ${temperature.isCritical}")

    // The compiler prevents the mix-up:
    // consumption(temperature)  // would not compile if consumption(Percent) was expected
    println(label(camera.id, temperature))
}

private fun label(id: DeviceId, temperature: Celsius): String =
    "device $id reports $temperature"
