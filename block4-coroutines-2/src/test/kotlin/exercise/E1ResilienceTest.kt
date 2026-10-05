@file:OptIn(ExperimentalCoroutinesApi::class)

package exercise

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.milliseconds

class E1ResilienceTest : StringSpec({

    // ----------------------------------------------------------------- 1a
    "succeeds on the third attempt" {
        val client = FlakyDeviceClient(failuresBeforeSuccess = 2, latencyMillis = 100)

        runTest {
            readStatusWithRetry(client, "cam-04") shouldBe "cam-04: online"
            client.callCount shouldBe 3
        }
    }

    "returns immediately when the first attempt works" {
        val client = FlakyDeviceClient(failuresBeforeSuccess = 0, latencyMillis = 100)

        runTest {
            readStatusWithRetry(client, "cam-04") shouldBe "cam-04: online"
            client.callCount shouldBe 1
            currentTime shouldBe 100          // no backoff at all
        }
    }

    "backs off exponentially" {
        val client = FlakyDeviceClient(failuresBeforeSuccess = 2, latencyMillis = 100)

        runTest {
            readStatusWithRetry(client, "cam-04", maxAttempts = 3, backoffMillis = 100)

            // 3 calls of 100 ms, plus backoffs of 100 and 200 ms.
            // A fixed backoff would end up at 500 ms instead.
            currentTime shouldBe 600
        }
    }

    "gives up after the last attempt and lets the exception through" {
        val client = FlakyDeviceClient(failuresBeforeSuccess = 10, latencyMillis = 100)

        runTest {
            shouldThrow<DeviceUnreachableException> {
                readStatusWithRetry(client, "cam-04", maxAttempts = 3)
            }
            client.callCount shouldBe 3
        }
    }

    // ----------------------------------------------------------------- 1b
    "returns null when the overall timeout is exceeded" {
        val client = FlakyDeviceClient(failuresBeforeSuccess = 10, latencyMillis = 100)

        runTest {
            val result = readStatusOrNull(
                client,
                "cam-04",
                maxAttempts = 5,
                backoffMillis = 100,
                timeoutMillis = 1_000,
            )

            result.shouldBeNull()
            currentTime shouldBe 1_000        // stopped exactly at the timeout
        }
    }

    "returns the value when it arrives within the timeout" {
        val client = FlakyDeviceClient(failuresBeforeSuccess = 1, latencyMillis = 100)

        runTest {
            val result = readStatusOrNull(client, "cam-04", timeoutMillis = 1_000)

            result shouldBe "cam-04: online"
            currentTime shouldBe 300          // 100 fail + 100 backoff + 100 success
        }
    }

    // ----------------------------------------------------------------- 1c
    "reads several devices and tolerates individual failures" {
        val clients = mapOf(
            "cam-04" to FlakyDeviceClient(failuresBeforeSuccess = 0),
            "rtr-01" to FlakyDeviceClient(failuresBeforeSuccess = 10),   // never works
            "sen-12" to FlakyDeviceClient(failuresBeforeSuccess = 1),
        )

        runTest {
            val results = readAllStatuses(clients)

            results shouldHaveSize 3
            results["cam-04"] shouldBe "cam-04: online"
            results["rtr-01"].shouldBeNull()
            results["sen-12"] shouldBe "sen-12: online"
        }
    }

    "reads the devices concurrently" {
        val clients = (1..5).associate {
            "dev-$it" to FlakyDeviceClient(failuresBeforeSuccess = 0, latencyMillis = 100)
        }

        runTest {
            readAllStatuses(clients)

            // Five devices at 100 ms each: 100 ms concurrently, 500 sequentially.
            currentTime shouldBe 100
        }
    }

    "handles an empty set of devices" {
        runTest {
            readAllStatuses(emptyMap()) shouldHaveSize 0
        }
    }

    // ----------------------------------------------------------------- 1d
    "closes down cleanly on cancellation" {
        runTest {
            val log = mutableListOf<String>()

            val job = launch { pollUntilCancelled(log, intervalMillis = 100) }

            advanceTimeBy(350.milliseconds)
            job.cancelAndJoin()

            // Three polls happened, and the cleanup still ran afterwards.
            log.shouldContainExactly("poll", "poll", "poll", "closed")
        }
    }
})
