package exercise

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class E1NullabilityTest {

    // --------------------------------------------------------- temperature
    @Test
    fun `reads a valid temperature`() {
        assertEquals(42.5, readTemperature(mapOf("temperature" to "42.5")))
    }

    @Test
    fun `returns null when the temperature is missing`() {
        assertNull(readTemperature(emptyMap()))
    }

    @Test
    fun `returns null when the temperature is null`() {
        assertNull(readTemperature(mapOf("temperature" to null)))
    }

    @Test
    fun `returns null when the temperature is not a number`() {
        assertNull(readTemperature(mapOf("temperature" to "warm")))
    }

    // ---------------------------------------------------------------- name
    @Test
    fun `reads the name and strips whitespace`() {
        assertEquals("Camera Studio B", readDeviceName(mapOf("name" to "  Camera Studio B  ")))
    }

    @Test
    fun `uses the default name when the field is missing`() {
        assertEquals("unknown device", readDeviceName(emptyMap()))
    }

    @Test
    fun `uses the default name for null`() {
        assertEquals("unknown device", readDeviceName(mapOf("name" to null)))
    }

    @Test
    fun `uses the default name for whitespace only`() {
        assertEquals("unknown device", readDeviceName(mapOf("name" to "    ")))
    }

    // ---------------------------------------------------------------- port
    @Test
    fun `reads a valid port`() {
        assertEquals(9100, readPort(mapOf("port" to "9100")))
    }

    @Test
    fun `uses the default port when the field is missing`() {
        assertEquals(9000, readPort(emptyMap()))
    }

    @Test
    fun `uses the supplied default port`() {
        assertEquals(1234, readPort(emptyMap(), default = 1234))
    }

    @Test
    fun `rejects ports outside the valid range`() {
        assertEquals(9000, readPort(mapOf("port" to "0")))
        assertEquals(9000, readPort(mapOf("port" to "70000")))
        assertEquals(9000, readPort(mapOf("port" to "-1")))
    }

    @Test
    fun `accepts the range boundaries`() {
        assertEquals(1, readPort(mapOf("port" to "1")))
        assertEquals(65535, readPort(mapOf("port" to "65535")))
    }

    // --------------------------------------------------------- description
    @Test
    fun `assembles the description with a temperature`() {
        val rawData = mapOf("name" to "  Camera Studio B  ", "temperature" to "42.5", "port" to "9100")
        assertEquals("Camera Studio B (port 9100) - 42.5 °C", describe(rawData))
    }

    @Test
    fun `assembles the description without a temperature`() {
        val rawData = mapOf("name" to "Router Control Room 1", "port" to "9100")
        assertEquals("Router Control Room 1 (port 9100) - no reading", describe(rawData))
    }

    @Test
    fun `falls back to every default`() {
        assertEquals("unknown device (port 9000) - no reading", describe(emptyMap()))
    }

    // ------------------------------------------------------------ style rule
    /**
     * This exercise can be solved entirely without `!!`. The test reads the
     * source file and verifies that the operator was not used after all.
     *
     * The working directory of the test task is the module directory.
     */
    @Test
    fun `solves the exercise without the not-null assertion operator`() {
        val source = File("src/main/kotlin/exercise/E1Nullability.kt")
        assertTrue(source.exists(), "source file not found: ${source.absolutePath}")

        // Hide comment lines - the exercise text itself mentions the
        // operator, and that must not trip the check.
        val codeLines = source.readLines()
            .map { it.substringBefore("//") }
            .filterNot { line ->
                val trimmed = line.trim()
                trimmed.isBlank() || trimmed.startsWith("*") || trimmed.startsWith("/*")
            }

        val offending = codeLines.filter { "!!" in it }
        assertTrue(
            offending.isEmpty(),
            "the not-null assertion operator is not allowed here. Affected:\n" +
                offending.joinToString("\n"),
        )
    }
}
