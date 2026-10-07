package block2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class S3FunctionalTest {

    private val measurements = sampleTelemetry

    // ----------------------------------------------------------------- 3a
    @Test
    fun `computes the average per device`() {
        val result = averagePerDevice(measurements)

        assertEquals(3, result.size)
        assertEquals(38.466666, result.getValue("sen-12"), 0.0001)
        assertEquals(38.75, result.getValue("cam-04"), 0.0001)
        assertEquals(91.833333, result.getValue("rtr-01"), 0.0001)
    }

    @Test
    fun `returns an empty result for no measurements`() {
        assertEquals(emptyMap(), averagePerDevice(emptyList()))
    }

    // ----------------------------------------------------------------- 3b
    @Test
    fun `determines the peak per device`() {
        val result = peakPerDevice(measurements)

        assertEquals(71.2, result.getValue("sen-12"))
        assertEquals(39.5, result.getValue("cam-04"))
        assertEquals(95.0, result.getValue("rtr-01"))
    }

    // ----------------------------------------------------------------- 3c
    @Test
    fun `filters outliers and keeps their order`() {
        val result = outliers(measurements, 70.0)

        assertEquals(listOf(71.2, 88.0, 92.5, 95.0), result.map { it.value })
    }

    @Test
    fun `filters nothing with a high limit`() {
        assertEquals(emptyList(), outliers(measurements, 200.0))
    }

    // ----------------------------------------------------------------- 3d
    @Test
    fun `determines the top devices`() {
        assertEquals(listOf("rtr-01", "cam-04"), topDevices(measurements, 2))
        assertEquals(listOf("rtr-01"), topDevices(measurements, 1))
    }

    @Test
    fun `returns every device when the count is too large`() {
        assertEquals(listOf("rtr-01", "cam-04", "sen-12"), topDevices(measurements, 99))
    }

    // ----------------------------------------------------------------- 3e
    @Test
    fun `recognises notable measurements`() {
        assertFalse(Measurement("sen-12", 69.9).isNotable)
        assertTrue(Measurement("sen-12", 70.0).isNotable)
        assertTrue(Measurement("sen-12", 95.0).isNotable)
    }

    // ----------------------------------------------------------------- 3f
    @Test
    fun `summarises the measurements`() {
        assertEquals("8 measurements from 3 devices, 4 notable", measurements.summarise())
    }

    @Test
    fun `summarises an empty list`() {
        assertEquals("no measurements", emptyList<Measurement>().summarise())
    }

    @Test
    fun `summarises a list without notable values`() {
        val harmless = listOf(Measurement("a", 1.0), Measurement("b", 2.0))
        assertEquals("2 measurements from 2 devices, 0 notable", harmless.summarise())
    }
}
