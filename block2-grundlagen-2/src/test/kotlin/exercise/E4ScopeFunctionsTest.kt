package exercise

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * These tests are green from the start. They pin down the behaviour while
 * you rewrite the functions to use scope functions.
 */
class E4ScopeFunctionsTest {

    @Test
    fun `reports an existing device`() {
        assertEquals(
            "Camera Studio B is running at 42 %",
            message(sampleEntries.first()),
        )
    }

    @Test
    fun `reports a missing device`() {
        assertEquals("device not found", message(null))
    }

    @Test
    fun `builds the standard configuration`() {
        val configuration = standardConfiguration("control-1")

        assertEquals("control-1", configuration.target)
        assertEquals(9100, configuration.port)
        assertEquals(5, configuration.timeoutSeconds)
        assertEquals(true, configuration.tls)
        assertEquals("tls://control-1:9100 (timeout 5s)", configuration.asText())
    }

    @Test
    fun `builds the profile`() {
        assertEquals(
            "id: cam-04\nname: Camera Studio B\nlocation: Studio B\nutilisation: 42 %",
            profile(sampleEntries.first()),
        )
    }

    @Test
    fun `determines critical names and logs along the way`() {
        val log = mutableListOf<String>()
        val names = criticalNames(sampleEntries, log)

        assertEquals(listOf("Router Control Room 1", "Temperature Sensor"), names)
        assertEquals(listOf("incoming: 4", "critical: 2", "names: 2"), log)
    }

    @Test
    fun `logs for an empty input as well`() {
        val log = mutableListOf<String>()
        val names = criticalNames(emptyList(), log)

        assertEquals(emptyList(), names)
        assertEquals(listOf("incoming: 0", "critical: 0", "names: 0"), log)
    }

    @Test
    fun `rates the utilisation`() {
        assertEquals("cam-04 is normal", rating(sampleEntries[0]))
        assertEquals("rtr-01 is critical", rating(sampleEntries[1]))
        assertEquals("int-07 is idle", rating(sampleEntries[2]))
    }
}
