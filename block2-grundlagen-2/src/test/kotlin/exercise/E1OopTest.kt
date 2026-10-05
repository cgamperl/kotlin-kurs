package exercise

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class E1OopTest {

    @Test
    fun `uses the default self test from the base class`() {
        // An anonymous subclass that overrides nothing - checks that
        // selfTest() is implemented in Component itself.
        val plain = object : Component("gen-01") {
            override val maintenanceIntervalMonths = 12
        }
        assertEquals("gen-01: ok", plain.selfTest())
    }

    @Test
    fun `prints the class name and the id`() {
        assertEquals("Camera(cam-04)", Camera("cam-04", "1080p").toString())
        assertEquals("Sensor(sen-12)", Sensor("sen-12", "°C").toString())
    }

    @Test
    fun `knows the maintenance intervals`() {
        assertEquals(6, Camera("cam-04", "1080p").maintenanceIntervalMonths)
        assertEquals(24, Sensor("sen-12", "°C").maintenanceIntervalMonths)
    }

    @Test
    fun `overrides the self test of the camera`() {
        assertEquals("cam-04: video signal ok (1080p)", Camera("cam-04", "1080p").selfTest())
        assertEquals("cam-09: video signal ok (4K)", Camera("cam-09", "4K").selfTest())
    }

    @Test
    fun `starts the sensor with a zero offset`() {
        assertEquals(0.0, Sensor("sen-12", "°C").offset)
    }

    @Test
    fun `carries the offset forward when calibrating`() {
        val sensor = Sensor("sen-12", "°C")

        sensor.calibrate(1.5)
        assertEquals(1.5, sensor.offset)

        // adds, does not replace
        sensor.calibrate(0.5)
        assertEquals(2.0, sensor.offset)

        sensor.calibrate(-2.0)
        assertEquals(0.0, sensor.offset)
    }

    @Test
    fun `overrides the self test of the sensor`() {
        val sensor = Sensor("sen-12", "°C")
        assertEquals("sen-12: measuring in °C, offset 0.0", sensor.selfTest())

        sensor.calibrate(1.5)
        assertEquals("sen-12: measuring in °C, offset 1.5", sensor.selfTest())
    }

    @Test
    fun `treats sensors as calibratable`() {
        val sensor: Component = Sensor("sen-12", "°C")

        assertTrue(sensor is Calibratable)
        // Smart cast after the type check:
        sensor.calibrate(3.0)
        assertEquals(3.0, sensor.offset)
    }

    @Test
    fun `builds the maintenance overview`() {
        val overview = maintenanceOverview(
            listOf(Camera("cam-04", "1080p"), Sensor("sen-12", "°C"))
        )

        assertEquals(
            "Camera(cam-04) - maintenance every 6 months\n" +
                "Sensor(sen-12) - maintenance every 24 months",
            overview,
        )
    }

    @Test
    fun `builds an empty overview without components`() {
        assertEquals("", maintenanceOverview(emptyList()))
    }
}
