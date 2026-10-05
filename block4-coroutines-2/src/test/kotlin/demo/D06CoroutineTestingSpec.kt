// The virtual-time tools - currentTime, advanceTimeBy, advanceUntilIdle,
// runCurrent, UnconfinedTestDispatcher, setMain - are still marked
// @ExperimentalCoroutinesApi. Opting in is a deliberate, visible decision
// rather than something hidden away in the build file.
@file:OptIn(ExperimentalCoroutinesApi::class)

package demo

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Demo 6 - Testing coroutines.
 *
 * The problem: `DeviceService.pingWithRetry` waits 1 s, then 2 s, then 3 s.
 * A test that actually waits six seconds is impractical to run.
 *
 * The answer is `runTest` and its VIRTUAL TIME. Inside it, `delay()` does
 * not wait - it advances a clock. Six seconds of waiting pass in
 * microseconds, and the timing is deterministic rather than flaky.
 *
 *     ./gradlew :block4-coroutines-2:demoTest
 */
class D06CoroutineTestingSpec : StringSpec({

    // ------------------------------------------------------------ 1
    "virtual time skips the waiting" {
        val repository = mockk<DeviceRepository>()
        coEvery { repository.ping("cam-04") } returns false      // always fails

        val service = DeviceService(repository)

        val realMillis = measureTimeMillis {
            runTest {
                val result = service.pingWithRetry("cam-04", attempts = 3)
                result shouldBe false

                // The virtual clock has advanced by 1000 + 2000 + 3000 ms …
                currentTime shouldBe 6000
            }
        }

        // … while barely any real time has passed.
        realMillis shouldBeLessThan 1000
    }

    "the retry stops as soon as the ping succeeds" {
        val repository = mockk<DeviceRepository>()
        // First call fails, second succeeds.
        coEvery { repository.ping("cam-04") } returnsMany listOf(false, true)

        runTest {
            DeviceService(repository).pingWithRetry("cam-04") shouldBe true

            // Only the first delay of 1000 ms happened.
            currentTime shouldBe 1000
        }

        coVerify(exactly = 2) { repository.ping("cam-04") }
    }

    // ------------------------------------------------------------ 2
    "advancing the clock by hand" {
        runTest {
            val log = mutableListOf<String>()

            launch {
                delay(1.seconds)
                log += "after 1s"
                delay(1.seconds)
                log += "after 2s"
            }

            // Nothing has run yet: the default dispatcher inside runTest is
            // a StandardTestDispatcher, which queues rather than executes.
            log.shouldContainExactly()

            advanceTimeBy(1_001.milliseconds)
            log.shouldContainExactly("after 1s")

            advanceUntilIdle()             // run everything that is pending
            log.shouldContainExactly("after 1s", "after 2s")
        }
    }

    "runCurrent executes what is due right now" {
        runTest {
            val log = mutableListOf<String>()

            launch { log += "immediately" }
            launch { delay(500.milliseconds); log += "later" }

            runCurrent()                   // no time passes
            log.shouldContainExactly("immediately")

            advanceTimeBy(501.milliseconds)
            log.shouldContainExactly("immediately", "later")
        }
    }

    // ------------------------------------------------------------ 3
    """the two test dispatchers behave differently""" {
        // StandardTestDispatcher (the default): coroutines are queued and
        // only run when you advance the clock. Deterministic, and it makes
        // ordering explicit.
        runTest(StandardTestDispatcher()) {
            var ran = false
            launch { ran = true }

            ran shouldBe false             // not yet
            advanceUntilIdle()
            ran shouldBe true
        }

        // UnconfinedTestDispatcher: coroutines start eagerly, right up to
        // their first suspension. Suitable when the ordering does not
        // matter and only the result is of interest.
        runTest(UnconfinedTestDispatcher()) {
            var ran = false
            launch { ran = true }

            ran shouldBe true              // already done
        }
    }

    // ------------------------------------------------------------ 4
    "a flow can be collected into a list" {
        val repository = mockk<DeviceRepository>()
        coEvery { repository.findById("cam-04") } returnsMany listOf(
            Device("cam-04", "Camera", 10),
            Device("cam-04", "Camera", 20),
            Device("cam-04", "Camera", 30),
        )

        runTest {
            val values = DeviceService(repository)
                .observeUtilisation("cam-04", intervalMillis = 1_000)
                .take(3)
                .toList()

            values.shouldContainExactly(10, 20, 30)

            // Two intervals passed between three emissions.
            currentTime shouldBe 2000
        }
    }

    // ------------------------------------------------------------ 5
    "code that hard-codes Dispatchers.Main needs setMain" {
        // A service that pins itself to the Main dispatcher cannot be
        // tested on the JVM - there is no Main dispatcher outside Android
        // or a UI toolkit. Dispatchers.setMain injects a test dispatcher.
        Dispatchers.setMain(StandardTestDispatcher())

        try {
            runTest {
                val result = async { withContext(Dispatchers.Main) { "done on main" } }
                result.await() shouldBe "done on main"
            }
        } finally {
            // Always reset it, otherwise the next test inherits it.
            Dispatchers.resetMain()
        }
    }

    // ------------------------------------------------------------ 6
    "suspending calls can be stubbed and verified" {
        val repository = mockk<DeviceRepository>()
        coEvery { repository.findById("cam-04") } returns Device("cam-04", "Camera", 42)
        coEvery { repository.save(any()) } just Runs

        runTest {
            DeviceService(repository).setUtilisation("cam-04", 77)
        }

        coVerify(exactly = 1) { repository.save(Device("cam-04", "Camera", 77)) }
    }
})
