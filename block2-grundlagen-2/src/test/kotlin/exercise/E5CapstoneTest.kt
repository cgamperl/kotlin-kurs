package exercise

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class E5CapstoneTest {

    // ----------------------------------------------------------------- 5a
    @Test
    fun `determines the span of numbers`() {
        assertEquals(AnalysisResult.Ok(1 to 5), span(listOf(3, 1, 4, 1, 5)))
    }

    @Test
    fun `determines the span of strings`() {
        // Works for any Comparable, not only numbers, which is what the
        // type parameter provides.
        assertEquals(AnalysisResult.Ok("cam-04" to "sen-12"), span(listOf("sen-12", "cam-04", "rtr-01")))
    }

    @Test
    fun `reports missing data for an empty list`() {
        assertEquals(AnalysisResult.NoData("empty list"), span(emptyList<Int>()))
    }

    @Test
    fun `handles a single element`() {
        assertEquals(AnalysisResult.Ok(7 to 7), span(listOf(7)))
    }

    // ----------------------------------------------------------------- 5b
    @Test
    fun `computes the moving average`() {
        val readings = sampleReadings.filter { it.deviceId == "sen-12" }

        val averages = readings.movingAverage(2)

        assertEquals(3, averages.size)
        assertEquals(22.1, averages[0], 0.0001)   // (21.4 + 22.8) / 2
        assertEquals(47.0, averages[1], 0.0001)   // (22.8 + 71.2) / 2
        assertEquals(47.65, averages[2], 0.0001)  // (71.2 + 24.1) / 2
    }

    @Test
    fun `returns nothing when the window is larger than the list`() {
        val readings = sampleReadings.take(2)
        assertEquals(emptyList(), readings.movingAverage(5))
    }

    // ----------------------------------------------------------------- 5c
    @Test
    fun `takes the first anomalies`() {
        val found = sampleReadings.asSequence().firstAnomalies(30.0, 2)

        assertEquals(listOf(71.2, 38.0), found.map { it.value })
    }

    @Test
    fun `returns fewer results when there are not enough matches`() {
        val found = sampleReadings.asSequence().firstAnomalies(100.0, 3)
        assertEquals(emptyList(), found)
    }

    /**
     * The decisive test for 5c: the sequence is infinite. A solution that
     * calls toList() first, or that filters everything before taking, will
     * hang here instead of failing - which is exactly why the limit below
     * exists.
     */
    @Test
    fun `terminates on an infinite sequence`() {
        var produced = 0
        val endless = generateSequence(0) { it + 1 }
            .map { index ->
                produced++
                check(produced < 10_000) { "the sequence was consumed eagerly" }
                Reading("dev-$index", index, index.toDouble())
            }

        val found = endless.firstAnomalies(threshold = 5.0, count = 3)

        assertEquals(listOf(6.0, 7.0, 8.0), found.map { it.value })
        assertTrue(produced < 100, "only a few elements should have been read, but it was $produced")
    }

    // ----------------------------------------------------------------- 5d
    @Test
    fun `builds a report line for a result`() {
        assertEquals("span: 21.4 to 71.2", reportLine(AnalysisResult.Ok(21.4 to 71.2)))
    }

    @Test
    fun `builds a report line for missing data`() {
        assertEquals(
            "no analysis possible (empty list)",
            reportLine(AnalysisResult.NoData("empty list")),
        )
    }

    // ----------------------------------------------------------------- 5e
    @Test
    fun `analyses each device separately`() {
        val analysis = analysisPerDevice(sampleReadings)

        assertEquals(2, analysis.size)
        assertEquals(AnalysisResult.Ok(21.4 to 71.2), analysis.getValue("sen-12"))
        assertEquals(AnalysisResult.Ok(38.0 to 39.5), analysis.getValue("cam-04"))
    }

    @Test
    fun `analyses an empty input`() {
        assertEquals(emptyMap(), analysisPerDevice(emptyList()))
    }

    @Test
    fun `combines analysis and report line`() {
        val lines = analysisPerDevice(sampleReadings)
            .map { (deviceId, result) -> "$deviceId: ${reportLine(result)}" }
            .sorted()

        assertEquals(
            listOf("cam-04: span: 38.0 to 39.5", "sen-12: span: 21.4 to 71.2"),
            lines,
        )
    }
}
