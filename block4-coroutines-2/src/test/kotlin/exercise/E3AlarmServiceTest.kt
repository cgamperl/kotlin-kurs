package exercise

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

/**
 * Exercise 3 - your own test suite for [AlarmService].
 *
 * One worked example is given below. Add the rest.
 *
 * Checklist (also in the module README):
 *
 *   Severity boundaries
 *     [ ] 59 raises nothing and returns null
 *     [ ] 60 raises a WARNING
 *     [ ] 89 raises a WARNING
 *     [ ] 90 raises a CRITICAL
 *     [ ] 100 raises a CRITICAL
 *
 *   The message
 *     [x] uses the name from the lookup          <- the example below
 *     [ ] falls back to the device id when the lookup returns null
 *
 *   Interaction with the sink
 *     [ ] publishes exactly once on success
 *     [ ] publishes NOTHING below the threshold
 *     [ ] retries once when the first publish throws IllegalStateException
 *     [ ] lets the exception through when the second attempt also throws
 *     [ ] the retry waits 200 ms  (use runTest and currentTime)
 *
 *   Validation
 *     [ ] a utilisation outside 0..100 throws IllegalArgumentException
 *
 *   raiseAll
 *     [ ] returns only the devices that actually raised an alarm
 *     [ ] keeps the input order
 *     [ ] an empty map yields an empty list
 *
 * Hints:
 *   - `mockk<DeviceLookup>()` plus `coEvery { ... } returns ...` for stubbing
 *   - `coVerify(exactly = n) { ... }` for the interactions
 *   - `coEvery { ... } throws ... andThenJust Runs` for the retry case
 *   - `runTest { ... }` plus `currentTime` for the 200 ms
 */
class E3AlarmServiceTest : StringSpec({

    // ------------------------------------------------------- worked example
    "uses the name from the lookup in the message" {
        val lookup = mockk<DeviceLookup>()
        val sink = mockk<AlarmSink>()

        coEvery { lookup.nameOf("cam-04") } returns "Camera Studio B"
        coEvery { sink.publish(any()) } just Runs

        val service = AlarmService(lookup, sink)

        runTest {
            val alarm = service.raise("cam-04", 95)

            alarm shouldBe Alarm("cam-04", Severity.CRITICAL, "Camera Studio B at 95 %")
            coVerify(exactly = 1) { sink.publish(alarm!!) }
        }
    }

    // TODO: add the tests from the checklist above.
})
