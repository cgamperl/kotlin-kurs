package exercise

import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class E1ParallelTest {

    /**
     * Timing assertions are generous on purpose: the sequential version
     * needs 1200 ms, the parallel one about 200 ms. Anything below 700 ms
     * can only have run concurrently, even on a loaded machine.
     */
    private val parallelBudgetMillis = 700L

    @Test
    fun `returns the same results as the sequential version`() = runBlocking {
        assertEquals(
            collectStatusesSequentially(sampleDeviceIds),
            collectStatusesInParallel(sampleDeviceIds),
        )
    }

    @Test
    fun `keeps the order of the input`() = runBlocking {
        val result = collectStatusesInParallel(listOf("rtr-01", "cam-04", "sen-12"))

        assertEquals(
            listOf("rtr-01: online", "cam-04: online", "sen-12: online"),
            result,
        )
    }

    @Test
    fun `runs the requests concurrently`() = runBlocking {
        val millis = measureTimeMillis {
            collectStatusesInParallel(sampleDeviceIds)
        }

        assertTrue(
            millis < parallelBudgetMillis,
            "six requests of 200 ms took $millis ms - they ran one after another",
        )
    }

    @Test
    fun `handles an empty list`() = runBlocking {
        assertEquals(emptyList(), collectStatusesInParallel(emptyList()))
    }

    // ----------------------------------------------------------------- 1b
    @Test
    fun `builds a device summary`() = runBlocking {
        assertEquals("cam-04: online, utilisation 42", deviceSummary("cam-04"))
    }

    @Test
    fun `fetches status and utilisation concurrently`() = runBlocking {
        val millis = measureTimeMillis { deviceSummary("cam-04") }

        assertTrue(
            millis < 350,
            "two calls of 200 ms took $millis ms - the second waited for the first",
        )
    }

    // ----------------------------------------------------------------- 1c
    @Test
    fun `builds all summaries`() = runBlocking {
        val result = allSummaries(listOf("cam-04", "rtr-01"))

        assertEquals(
            listOf("cam-04: online, utilisation 42", "rtr-01: online, utilisation 42"),
            result,
        )
    }

    @Test
    fun `builds all summaries concurrently`() = runBlocking {
        val millis = measureTimeMillis { allSummaries(sampleDeviceIds) }

        assertTrue(
            millis < parallelBudgetMillis,
            "twelve calls of 200 ms took $millis ms - they did not all run at once",
        )
    }
}
