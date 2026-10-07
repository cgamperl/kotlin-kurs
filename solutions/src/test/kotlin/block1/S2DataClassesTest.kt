package block1

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class S2DataClassesTest {

    // ------------------------------------------------------------ SensorId
    @Test
    fun `accepts a filled sensor id`() {
        assertEquals("cam-04", SensorId("cam-04").value)
    }

    @Test
    fun `rejects an empty sensor id`() {
        assertFailsWith<IllegalArgumentException> { SensorId("") }
    }

    @Test
    fun `rejects a whitespace only sensor id`() {
        assertFailsWith<IllegalArgumentException> { SensorId("   ") }
    }

    // ------------------------------------------------------------- Percent
    @Test
    fun `accepts the boundaries of the range`() {
        assertEquals(0, Percent(0).value)
        assertEquals(100, Percent(100).value)
    }

    @Test
    fun `rejects values outside the range`() {
        assertFailsWith<IllegalArgumentException> { Percent(-1) }
        assertFailsWith<IllegalArgumentException> { Percent(101) }
    }

    @Test
    fun `flags critical utilisation from ninety percent`() {
        assertFalse(Percent(89).isCritical)
        assertTrue(Percent(90).isCritical)
        assertTrue(Percent(100).isCritical)
    }

    // ------------------------------------------------------------- Reading
    @Test
    fun `uses the default value for the source`() {
        assertEquals("device", Reading(SensorId("cam-04"), Percent(42)).source)
    }

    @Test
    fun `compares readings by value`() {
        val a = Reading(SensorId("cam-04"), Percent(42))
        val b = Reading(SensorId("cam-04"), Percent(42))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, Reading(SensorId("cam-04"), Percent(43)))
    }

    /**
     * `note` lives in the class body, not in the primary constructor, so it
     * does not count towards equals() - a pitfall worth seeing once.
     */
    @Test
    fun `ignores the note when comparing`() {
        val a = Reading(SensorId("cam-04"), Percent(42))
        val b = Reading(SensorId("cam-04"), Percent(42)).also { it.note = "replacement scheduled" }

        assertEquals(a, b)
    }

    @Test
    fun `creates a new object in withUtilisation`() {
        val original = Reading(SensorId("cam-04"), Percent(42), source = "gateway")
        val changed = original.withUtilisation(Percent(95))

        assertEquals(Percent(95), changed.utilisation)
        assertEquals(Percent(42), original.utilisation, "the original must not change")
        assertEquals(SensorId("cam-04"), changed.sensor)
        assertEquals("gateway", changed.source, "the remaining values are carried over")
    }

    // --------------------------------------------------------------- label
    @Test
    fun `labels a non critical reading`() {
        val reading = Reading(SensorId("cam-04"), Percent(42))
        assertEquals("cam-04: 42 % (source: device)", reading.label())
    }

    @Test
    fun `labels a critical reading`() {
        val reading = Reading(SensorId("cam-04"), Percent(95))
        assertEquals("cam-04: 95 % (source: device) [critical]", reading.label())
    }

    @Test
    fun `carries a different source into the label`() {
        val reading = Reading(SensorId("rtr-01"), Percent(10), source = "gateway")
        assertEquals("rtr-01: 10 % (source: gateway)", reading.label())
    }

    // ------------------------------------------------------- destructuring
    @Test
    fun `can be destructured`() {
        val (sensor, utilisation, source) = Reading(SensorId("cam-04"), Percent(42))

        assertEquals(SensorId("cam-04"), sensor)
        assertEquals(Percent(42), utilisation)
        assertEquals("device", source)
    }
}
