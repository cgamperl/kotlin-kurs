package exercise

/**
 * Exercise 4 - the wrap-up exercise: a device inventory.
 *
 * Everything from block 1 in one piece: value class, data class with
 * default arguments, nullability, `by lazy`, `when` and visibility.
 *
 * Verify with:
 *     ./gradlew :block1-grundlagen-1:exerciseTest
 */

enum class DeviceClass { CAMERA, INTERCOM, ROUTER, SENSOR }

@JvmInline
value class InventoryId(val value: String) {
    override fun toString(): String = value
}

/**
 * A device in the inventory.
 *
 * `location` is deliberately nullable: not every device is assigned to a
 * room. `utilisation` has a default - a freshly registered device has not
 * reported anything yet.
 */
data class InventoryDevice(
    val id: InventoryId,
    val name: String,
    val deviceClass: DeviceClass,
    val location: String? = null,
    val utilisation: Int = 0,
)

class Inventory(private val devices: List<InventoryDevice>) {

    /**
     * Exercise 4a - `by lazy`
     *
     * A report in exactly this format:
     *
     *     Inventory: 4 devices, 1 of them critical
     *
     * "critical" means a utilisation of 90 or more.
     *
     * Important: the report must be computed on the FIRST access only and
     * reused afterwards. One test checks that two reads return the same
     * object - which would not hold with a plain `get()`.
     *
     * Useful: count { ... }
     */
    val report: String by lazy {
        TODO("4a: compute the report once")
    }

    /**
     * Exercise 4b - nullability
     *
     * Looks up a device. Returns null when the id is unknown.
     *
     * Useful: firstOrNull { ... }
     */
    fun find(id: InventoryId): InventoryDevice? {
        TODO("4b: look up a device")
    }

    /**
     * Exercise 4c - nullability with a default
     *
     * Returns the location of a device. When the device is unknown OR has
     * no location recorded, the result is "unassigned".
     *
     * Note: two different reasons, both leading to the same result - that
     * can be expressed in a single chain.
     */
    fun locationOf(id: InventoryId): String {
        TODO("4c: return the location with a default")
    }

    /**
     * Exercise 4d - `when` as an expression
     *
     * Rates the state of a device by its utilisation:
     *
     *     device unknown -> "unknown"
     *     below 10       -> "idle"
     *     below 60       -> "normal"
     *     below 90       -> "high"
     *     90 and up      -> "critical"
     */
    fun state(id: InventoryId): String {
        TODO("4d: rate the state")
    }

    /**
     * Exercise 4e - `when` over an enum
     *
     * Maps a device class to a department:
     *
     *     CAMERA, INTERCOM -> "media technology"
     *     ROUTER           -> "network"
     *     SENSOR           -> "peripherals"
     *
     * Write the `when` WITHOUT an else branch. When a device class is added
     * later, the compiler should complain right here.
     */
    fun department(deviceClass: DeviceClass): String {
        TODO("4e: determine the department")
    }
}

val sampleInventory = Inventory(
    listOf(
        InventoryDevice(InventoryId("cam-04"), "Camera Studio B", DeviceClass.CAMERA, "Studio B", 42),
        InventoryDevice(InventoryId("rtr-01"), "Router Control Room 1", DeviceClass.ROUTER, "Control Room 1", 95),
        InventoryDevice(InventoryId("int-07"), "Intercom Desk", DeviceClass.INTERCOM, utilisation = 5),
        InventoryDevice(InventoryId("sen-12"), "Temperature Sensor", DeviceClass.SENSOR, "Plant Room"),
    )
)

fun main() {
    println(sampleInventory.report)
    println(sampleInventory.locationOf(InventoryId("int-07")))
    println(sampleInventory.state(InventoryId("rtr-01")))
}
