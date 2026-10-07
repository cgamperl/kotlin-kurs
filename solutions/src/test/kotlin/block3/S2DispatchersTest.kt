package block3

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class S2DispatchersTest {

    @Test
    fun `reads a configuration`() = runBlocking {
        assertEquals("cam-04:port=9100", readConfiguration("cam-04"))
    }

    /**
     * The decisive test for 2a.
     *
     * `runBlocking` runs its children on a single-threaded event loop on
     * this very thread. The ticker below can therefore only make progress
     * while nobody is blocking that thread.
     *
     * Call `readConfigurationBlocking` directly and the ticker never runs:
     * ticks stays 0. Move it to another dispatcher with `withContext` and
     * the ticker keeps counting.
     */
    @Test
    fun `does not block the calling thread`() = runBlocking {
        var ticks = 0

        val ticker = launch {
            while (isActive) {
                delay(10.milliseconds)
                ticks++
            }
        }

        readConfiguration("cam-04")
        ticker.cancel()

        assertTrue(
            ticks > 3,
            "the calling thread was blocked - the ticker only managed $ticks ticks",
        )
    }

    // ----------------------------------------------------------------- 2b
    @Test
    fun `reads several configurations in input order`() = runBlocking {
        val result = readConfigurations(listOf("rtr-01", "cam-04"))

        assertEquals(listOf("rtr-01:port=9100", "cam-04:port=9100"), result)
    }

    @Test
    fun `reads configurations concurrently`() = runBlocking {
        val deviceIds = List(12) { "dev-$it" }

        val millis = measureTimeMillis { readConfigurations(deviceIds) }

        assertTrue(
            millis < 900,
            "twelve reads of 150 ms took $millis ms - they ran one after another",
        )
    }

    @Test
    fun `handles an empty list`() = runBlocking {
        assertEquals(emptyList(), readConfigurations(emptyList()))
    }

    // ----------------------------------------------------------------- 2c
    @Test
    fun `computes the same checksum as the blocking version`() = runBlocking {
        assertEquals(checksumBlocking("cam-04"), checksum("cam-04"))
    }

    /**
     * The decisive test for 2c.
     *
     * The checksum completes within a few milliseconds, too quickly for a
     * timer-based check. The child coroutine is queued on the event loop of
     * `runBlocking` before `checksum` is called, so it runs only if
     * `checksum` suspends and releases the thread. A blocking implementation
     * returns before the child has been dispatched.
     */
    @Test
    fun `does not block the calling thread while computing`() = runBlocking {
        var childRan = false
        launch { childRan = true }

        checksum("cam-04")

        assertTrue(childRan, "the CPU-bound work blocked the calling thread")
    }
}
