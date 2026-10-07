package block2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class S2SealedTest {

    private val values = listOf(
        Measurement("sen-12", 21.4),
        Measurement("sen-12", 22.8),
        Measurement("cam-04", 38.0),
    )

    // ----------------------------------------------------------------- 2a
    @Test
    fun `describes the loading state`() {
        assertEquals("query running …", displayText(QueryState.Loading))
    }

    @Test
    fun `describes a success with measurements`() {
        assertEquals("3 measurements received", displayText(QueryState.Success(values)))
        assertEquals("1 measurements received", displayText(QueryState.Success(values.take(1))))
    }

    @Test
    fun `describes a success without measurements separately`() {
        assertEquals("no measurements received", displayText(QueryState.Success(emptyList())))
    }

    @Test
    fun `describes a failure`() {
        assertEquals("error: no network", displayText(QueryState.Failure("no network")))
    }

    @Test
    fun `describes a timeout`() {
        assertEquals("timed out after 30 s", displayText(QueryState.TimedOut(30)))
    }

    // ----------------------------------------------------------------- 2b
    @Test
    fun `extracts the measurements from a success`() {
        assertEquals(values, measurementsOrNull(QueryState.Success(values)))
    }

    @Test
    fun `returns null for every other state`() {
        assertNull(measurementsOrNull(QueryState.Loading))
        assertNull(measurementsOrNull(QueryState.Failure("no network")))
        assertNull(measurementsOrNull(QueryState.TimedOut(30)))
    }

    // ----------------------------------------------------------------- 2c
    @Test
    fun `recognises final states`() {
        assertFalse(isFinal(QueryState.Loading))
        assertTrue(isFinal(QueryState.Success(values)))
        assertTrue(isFinal(QueryState.Failure("no network")))
        assertTrue(isFinal(QueryState.TimedOut(30)))
    }

    // ----------------------------------------------------------------- 2d
    @Test
    fun `retries after a short timeout`() {
        assertTrue(isWorthRetrying(QueryState.TimedOut(30)))
        assertTrue(isWorthRetrying(QueryState.TimedOut(59)))
    }

    @Test
    fun `does not retry after a long timeout`() {
        assertFalse(isWorthRetrying(QueryState.TimedOut(60)))
        assertFalse(isWorthRetrying(QueryState.TimedOut(120)))
    }

    @Test
    fun `retries only on temporary failures`() {
        assertTrue(isWorthRetrying(QueryState.Failure("temporary outage")))
        assertFalse(isWorthRetrying(QueryState.Failure("unknown device")))
    }

    @Test
    fun `retries neither while loading nor on success`() {
        assertFalse(isWorthRetrying(QueryState.Loading))
        assertFalse(isWorthRetrying(QueryState.Success(values)))
    }

    // ------------------------------------------------------------ data object
    /**
     * `data object` is a singleton: there is exactly one instance, and
     * toString() yields the name instead of an object address.
     */
    @Test
    fun `has exactly one instance of the loading state`() {
        val a: QueryState = QueryState.Loading
        val b: QueryState = QueryState.Loading

        assertTrue(a === b)
        assertEquals("Loading", QueryState.Loading.toString())
    }
}
