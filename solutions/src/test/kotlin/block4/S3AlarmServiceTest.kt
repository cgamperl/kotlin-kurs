@file:OptIn(ExperimentalCoroutinesApi::class)

package block4

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.andThenJust
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest

/**
 * Reference solution for exercise 3 - the full test suite for AlarmService.
 *
 * Note the shape of the file as much as its contents: a small helper that
 * builds the mocks, one `describe` block per concern, and assertions that
 * check one thing each. A test suite is production code too.
 */
class S3AlarmServiceTest : DescribeSpec({

    /**
     * Shared setup. A local helper beats a `beforeTest` block here,
     * because each test gets its own fresh mocks and there is no shared
     * mutable state between tests.
     */
    fun serviceWith(
        knownName: String? = "Camera Studio B",
        sink: AlarmSink = mockk<AlarmSink>().also { coEvery { it.publish(any()) } just Runs },
    ): Pair<AlarmService, AlarmSink> {
        val lookup = mockk<DeviceLookup>()
        coEvery { lookup.nameOf(any()) } returns knownName
        return AlarmService(lookup, sink) to sink
    }

    describe("severity boundaries") {

        // Data driven: each value is reported as its own test, so a
        // failure names the exact boundary that broke.
        withData(0, 30, 59) { utilisation ->
            val (service, sink) = serviceWith()

            runTest {
                service.raise("cam-04", utilisation).shouldBeNull()
                coVerify(exactly = 0) { sink.publish(any()) }
            }
        }

        withData(60, 75, 89) { utilisation ->
            val (service, _) = serviceWith()

            runTest {
                service.raise("cam-04", utilisation)?.severity shouldBe Severity.WARNING
            }
        }

        withData(90, 95, 100) { utilisation ->
            val (service, _) = serviceWith()

            runTest {
                service.raise("cam-04", utilisation)?.severity shouldBe Severity.CRITICAL
            }
        }
    }

    describe("the message") {

        it("uses the name from the lookup") {
            val (service, _) = serviceWith(knownName = "Camera Studio B")

            runTest {
                service.raise("cam-04", 95)?.message shouldBe "Camera Studio B at 95 %"
            }
        }

        it("falls back to the device id when the device is unknown") {
            val (service, _) = serviceWith(knownName = null)

            runTest {
                service.raise("cam-04", 95)?.message shouldBe "cam-04 at 95 %"
            }
        }
    }

    describe("publishing") {

        it("publishes exactly once on success") {
            val (service, sink) = serviceWith()

            runTest {
                val alarm = service.raise("cam-04", 95)
                coVerify(exactly = 1) { sink.publish(alarm!!) }
            }
        }

        it("publishes nothing below the threshold") {
            val (service, sink) = serviceWith()

            runTest {
                service.raise("cam-04", 10)
                coVerify(exactly = 0) { sink.publish(any()) }
            }
        }

        it("retries once when the first attempt fails") {
            val sink = mockk<AlarmSink>()
            // `andThenJust Runs` makes the second call succeed.
            coEvery { sink.publish(any()) } throws
                IllegalStateException("sink unavailable") andThenJust Runs

            val (service, _) = serviceWith(sink = sink)

            runTest {
                val alarm = service.raise("cam-04", 95)

                alarm?.severity shouldBe Severity.CRITICAL
                coVerify(exactly = 2) { sink.publish(any()) }
            }
        }

        it("waits 200 ms before retrying") {
            val sink = mockk<AlarmSink>()
            coEvery { sink.publish(any()) } throws
                IllegalStateException("sink unavailable") andThenJust Runs

            val (service, _) = serviceWith(sink = sink)

            runTest {
                service.raise("cam-04", 95)

                // Virtual time: the 200 ms are asserted exactly, and the
                // test still finishes in microseconds.
                currentTime shouldBe 200
            }
        }

        it("lets the exception through when the retry fails too") {
            val sink = mockk<AlarmSink>()
            coEvery { sink.publish(any()) } throws IllegalStateException("sink unavailable")

            val (service, _) = serviceWith(sink = sink)

            runTest {
                shouldThrow<IllegalStateException> { service.raise("cam-04", 95) }
                coVerify(exactly = 2) { sink.publish(any()) }
            }
        }
    }

    describe("validation") {

        withData(-1, 101, 1_000) { invalid ->
            val (service, _) = serviceWith()

            runTest {
                shouldThrow<IllegalArgumentException> { service.raise("cam-04", invalid) }
            }
        }
    }

    describe("raiseAll") {

        it("returns only the devices that actually raised an alarm") {
            val (service, _) = serviceWith(knownName = null)

            runTest {
                val alarms = service.raiseAll(
                    // LinkedHashMap keeps the insertion order, which the
                    // next test relies on.
                    linkedMapOf("cam-04" to 95, "rtr-01" to 10, "sen-12" to 70),
                )

                alarms.map { it.deviceId } shouldContainExactly listOf("cam-04", "sen-12")
            }
        }

        it("keeps the input order") {
            val (service, _) = serviceWith(knownName = null)

            runTest {
                val alarms = service.raiseAll(
                    linkedMapOf("sen-12" to 70, "cam-04" to 95),
                )

                alarms.map { it.deviceId } shouldContainExactly listOf("sen-12", "cam-04")
                alarms.map { it.severity } shouldContainExactly
                    listOf(Severity.WARNING, Severity.CRITICAL)
            }
        }

        it("returns an empty list for an empty map") {
            val (service, sink) = serviceWith()

            runTest {
                service.raiseAll(emptyMap()) shouldContainExactly emptyList()
                coVerify(exactly = 0) { sink.publish(any()) }
            }
        }
    }
})
