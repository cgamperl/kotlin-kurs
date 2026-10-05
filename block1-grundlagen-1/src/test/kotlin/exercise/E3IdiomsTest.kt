package exercise

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * These tests are green from the start. They describe the behaviour that
 * must survive the refactoring.
 */
class E3IdiomsTest {

    @Test
    fun `classifies the utilisation`() {
        assertEquals("idle", level(0))
        assertEquals("idle", level(9))
        assertEquals("normal", level(10))
        assertEquals("normal", level(59))
        assertEquals("high", level(60))
        assertEquals("high", level(89))
        assertEquals("critical", level(90))
        assertEquals("critical", level(100))
    }

    @Test
    fun `formats name and port`() {
        assertEquals("control-1 (port 9100)", format("control-1", 9100))
    }

    @Test
    fun `builds the url with all defaults`() {
        assertEquals("tcp://control-1:9000", connectionUrl("control-1"))
    }

    @Test
    fun `builds the url with a different port`() {
        assertEquals("tcp://control-1:9100", connectionUrl("control-1", 9100))
    }

    @Test
    fun `builds the url with a different protocol`() {
        assertEquals("udp://control-1:9100", connectionUrl("control-1", 9100, "udp"))
    }

    /**
     * This one goes beyond pure behaviour: with default arguments the
     * protocol can be passed by name without repeating the port. Three
     * overloads cannot do that.
     *
     * It is commented out until the refactoring - enable it once you have
     * rewritten 3c.
     */
    // @Test
    // fun `allows named arguments`() {
    //     assertEquals("udp://control-1:9000", connectionUrl("control-1", protocol = "udp"))
    // }

    @Test
    fun `detects the normal band`() {
        assertFalse(isWithinNormalBand(9))
        assertTrue(isWithinNormalBand(10))
        assertTrue(isWithinNormalBand(73))
        assertTrue(isWithinNormalBand(90))
        assertFalse(isWithinNormalBand(91))
    }

    @Test
    fun `cleans names`() {
        assertEquals("Camera Studio B", cleanName("  Camera Studio B  "))
        assertEquals("unknown", cleanName(null))
        assertEquals("unknown", cleanName(""))
        assertEquals("unknown", cleanName("    "))
    }

    @Test
    fun `names the installation size`() {
        assertEquals("small installation", installationSize(8))
        assertEquals("large installation", installationSize(9))
    }
}
