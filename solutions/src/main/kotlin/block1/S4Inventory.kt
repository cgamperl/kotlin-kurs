package block1

/**
 * Solution for exercise 4 - device inventory.
 */

enum class DeviceClass { CAMERA, INTERCOM, ROUTER, SENSOR }

@JvmInline
value class InventoryId(val value: String) {
    override fun toString(): String = value
}

data class InventoryDevice(
    val id: InventoryId,
    val name: String,
    val deviceClass: DeviceClass,
    val location: String? = null,
    val utilisation: Int = 0,
)

class Inventory(private val devices: List<InventoryDevice>) {

    /**
     * 4a - `by lazy`.
     *
     * The block runs on the first read access; afterwards the property
     * returns the remembered value. That is exactly why `assertSame` works
     * in the test: both accesses hand back the same String object.
     *
     * With `get() = "…"` a new string would be built on every access -
     * equal in content, but recomputed every time.
     */
    val report: String by lazy {
        val critical = devices.count { it.utilisation >= 90 }
        "Inventory: ${devices.size} devices, $critical of them critical"
    }

    /** 4b - `firstOrNull` returns a nullable type by itself. */
    fun find(id: InventoryId): InventoryDevice? =
        devices.firstOrNull { it.id == id }

    /**
     * 4c - both failure cases collapse into one chain.
     *
     * The first safe call catches "device unknown", the second step
     * "location is null". Both end up at the same Elvis branch - no case
     * distinction is needed, even though there are two reasons.
     */
    fun locationOf(id: InventoryId): String =
        find(id)?.location ?: "unassigned"

    /**
     * 4d - `when` as an expression.
     *
     * The utilisation is fetched as a nullable first; the argument-less
     * `when` then treats the null case as an ordinary branch.
     */
    fun state(id: InventoryId): String {
        val utilisation = find(id)?.utilisation

        return when {
            utilisation == null -> "unknown"
            utilisation < 10 -> "idle"
            utilisation < 60 -> "normal"
            utilisation < 90 -> "high"
            else -> "critical"
        }
    }

    /**
     * 4e - `when` over an enum, exhaustive and therefore without `else`.
     *
     * Leaving out `else` is deliberate: if a fifth device class were added,
     * this would be a compile error instead of a silent fall-through into
     * a catch-all branch.
     */
    fun department(deviceClass: DeviceClass): String = when (deviceClass) {
        DeviceClass.CAMERA, DeviceClass.INTERCOM -> "media technology"
        DeviceClass.ROUTER -> "network"
        DeviceClass.SENSOR -> "peripherals"
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
