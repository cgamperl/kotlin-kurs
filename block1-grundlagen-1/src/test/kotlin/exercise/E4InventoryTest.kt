package exercise

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class E4InventoryTest {

    private val inventory = sampleInventory

    // -------------------------------------------------------------- 4a
    @Test
    fun `creates the report`() {
        assertEquals("Inventory: 4 devices, 1 of them critical", inventory.report)
    }

    @Test
    fun `counts several critical devices`() {
        val full = Inventory(
            listOf(
                InventoryDevice(InventoryId("a"), "A", DeviceClass.CAMERA, utilisation = 90),
                InventoryDevice(InventoryId("b"), "B", DeviceClass.ROUTER, utilisation = 100),
                InventoryDevice(InventoryId("c"), "C", DeviceClass.SENSOR, utilisation = 89),
            )
        )
        assertEquals("Inventory: 3 devices, 2 of them critical", full.report)
    }

    @Test
    fun `copes with an empty inventory`() {
        assertEquals("Inventory: 0 devices, 0 of them critical", Inventory(emptyList()).report)
    }

    /**
     * `by lazy` computes once and remembers the result. A second access
     * must therefore return the very same object - not merely an equal
     * string.
     */
    @Test
    fun `computes the report only once`() {
        val fresh = Inventory(listOf(InventoryDevice(InventoryId("a"), "A", DeviceClass.SENSOR)))
        assertSame(fresh.report, fresh.report, "the report should be computed only once")
    }

    // -------------------------------------------------------------- 4b
    @Test
    fun `finds an existing device`() {
        assertEquals("Camera Studio B", inventory.find(InventoryId("cam-04"))?.name)
    }

    @Test
    fun `returns null for an unknown device`() {
        assertNull(inventory.find(InventoryId("does-not-exist")))
    }

    // -------------------------------------------------------------- 4c
    @Test
    fun `returns the recorded location`() {
        assertEquals("Studio B", inventory.locationOf(InventoryId("cam-04")))
    }

    @Test
    fun `reports a missing location`() {
        assertEquals("unassigned", inventory.locationOf(InventoryId("int-07")))
    }

    @Test
    fun `reports an unknown device as unassigned`() {
        assertEquals("unassigned", inventory.locationOf(InventoryId("does-not-exist")))
    }

    // -------------------------------------------------------------- 4d
    @Test
    fun `rates the state`() {
        assertEquals("normal", inventory.state(InventoryId("cam-04")))    // 42
        assertEquals("critical", inventory.state(InventoryId("rtr-01")))  // 95
        assertEquals("idle", inventory.state(InventoryId("int-07")))      // 5
        assertEquals("idle", inventory.state(InventoryId("sen-12")))      // 0 (default)
    }

    @Test
    fun `rates an unknown device`() {
        assertEquals("unknown", inventory.state(InventoryId("does-not-exist")))
    }

    @Test
    fun `hits the boundaries of the state rating`() {
        fun stateAt(utilisation: Int): String {
            val single = Inventory(
                listOf(InventoryDevice(InventoryId("x"), "X", DeviceClass.SENSOR, utilisation = utilisation))
            )
            return single.state(InventoryId("x"))
        }

        assertEquals("idle", stateAt(9))
        assertEquals("normal", stateAt(10))
        assertEquals("normal", stateAt(59))
        assertEquals("high", stateAt(60))
        assertEquals("high", stateAt(89))
        assertEquals("critical", stateAt(90))
    }

    // -------------------------------------------------------------- 4e
    @Test
    fun `maps every device class to a department`() {
        assertEquals("media technology", inventory.department(DeviceClass.CAMERA))
        assertEquals("media technology", inventory.department(DeviceClass.INTERCOM))
        assertEquals("network", inventory.department(DeviceClass.ROUTER))
        assertEquals("peripherals", inventory.department(DeviceClass.SENSOR))
    }
}
