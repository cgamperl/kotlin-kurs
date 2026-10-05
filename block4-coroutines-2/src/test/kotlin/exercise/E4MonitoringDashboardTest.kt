@file:OptIn(ExperimentalCoroutinesApi::class)

package exercise

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.milliseconds

/**
 * A configurable fake gateway.
 *
 * Each device is described by how long it takes, whether it throws, and
 * what readings it produces. Declared at file level because a class
 * inside the spec lambda would be a local class, and local classes
 * cannot declare nested ones.
 */
private class FakeGateway(
    private val devices: Map<String, Behaviour>,
) : DeviceGateway {

    data class Behaviour(
        val utilisation: Int = 50,
        val latencyMillis: Long = 100,
        val failWith: String? = null,
        val readings: List<Pair<Long, Double>> = emptyList(),
    )

    override suspend fun status(deviceId: String): DeviceStatus {
        val behaviour = devices[deviceId] ?: error("unknown device $deviceId")
        delay(behaviour.latencyMillis.milliseconds)

        behaviour.failWith?.let { throw IllegalStateException(it) }
        return DeviceStatus(deviceId, behaviour.utilisation)
    }

    override fun readings(deviceId: String): Flow<Double> {
        val behaviour = devices[deviceId] ?: return emptyFlow()

        return flow {
            for ((delayMillis, value) in behaviour.readings) {
                delay(delayMillis.milliseconds)
                emit(value)
            }
        }
    }
}

/**
 * Acceptance tests for the capstone project.
 *
 * They use a hand written fake rather than MockK: the gateway has to
 * produce flows and controllable delays, which is far easier to express
 * directly than to stub.
 */
class E4MonitoringDashboardTest : DescribeSpec({

    describe("task 5 - levels") {

        it("maps utilisation to a level") {
            MonitoringService.levelOf(0) shouldBe Level.IDLE
            MonitoringService.levelOf(9) shouldBe Level.IDLE
            MonitoringService.levelOf(10) shouldBe Level.NORMAL
            MonitoringService.levelOf(59) shouldBe Level.NORMAL
            MonitoringService.levelOf(60) shouldBe Level.HIGH
            MonitoringService.levelOf(89) shouldBe Level.HIGH
            MonitoringService.levelOf(90) shouldBe Level.CRITICAL
            MonitoringService.levelOf(100) shouldBe Level.CRITICAL
        }
    }

    describe("task 1 - a single report") {

        it("reports a device that answers") {
            val gateway = FakeGateway(mapOf("cam-04" to FakeGateway.Behaviour(utilisation = 95)))
            val service = MonitoringService(gateway)

            runTest {
                service.report("cam-04") shouldBe
                    DeviceReport.Ok("cam-04", 95, Level.CRITICAL)
            }
        }

        it("reports a device that fails as unreachable") {
            val gateway = FakeGateway(
                mapOf("rtr-01" to FakeGateway.Behaviour(failWith = "link down")),
            )
            val service = MonitoringService(gateway)

            runTest {
                service.report("rtr-01") shouldBe
                    DeviceReport.Unreachable("rtr-01", "link down")
            }
        }

        it("reports a slow device as timed out") {
            val gateway = FakeGateway(
                mapOf("sen-12" to FakeGateway.Behaviour(latencyMillis = 5_000)),
            )
            val service = MonitoringService(gateway)

            runTest {
                service.report("sen-12", timeoutMillis = 500) shouldBe
                    DeviceReport.TimedOut("sen-12", 500)

                // It really gave up at the timeout instead of waiting 5 s.
                currentTime shouldBe 500
            }
        }
    }

    describe("task 2 - the dashboard") {

        val gateway = FakeGateway(
            mapOf(
                "cam-04" to FakeGateway.Behaviour(utilisation = 95, latencyMillis = 100),
                "rtr-01" to FakeGateway.Behaviour(failWith = "link down", latencyMillis = 100),
                "sen-12" to FakeGateway.Behaviour(latencyMillis = 5_000),
                "int-07" to FakeGateway.Behaviour(utilisation = 5, latencyMillis = 100),
            ),
        )
        val service = MonitoringService(gateway)
        val deviceIds = listOf("cam-04", "rtr-01", "sen-12", "int-07")

        it("keeps the input order") {
            runTest {
                service.dashboard(deviceIds).map { it.deviceId } shouldContainExactly deviceIds
            }
        }

        it("tolerates failures and timeouts") {
            runTest {
                val reports = service.dashboard(deviceIds, timeoutMillis = 500)

                reports[0].shouldBeInstanceOf<DeviceReport.Ok>()
                reports[1].shouldBeInstanceOf<DeviceReport.Unreachable>()
                reports[2].shouldBeInstanceOf<DeviceReport.TimedOut>()
                reports[3].shouldBeInstanceOf<DeviceReport.Ok>()
            }
        }

        it("queries the devices concurrently") {
            runTest {
                service.dashboard(deviceIds, timeoutMillis = 500)

                // The slowest device sets the pace, and it is capped by
                // the timeout. Sequentially this would be 5300 ms.
                currentTime shouldBe 500
            }
        }

        it("handles an empty dashboard") {
            runTest {
                service.dashboard(emptyList()) shouldContainExactly emptyList()
            }
        }
    }

    describe("task 3 - the alert stream") {

        it("merges the streams of all devices") {
            val gateway = FakeGateway(
                mapOf(
                    "cam-04" to FakeGateway.Behaviour(
                        readings = listOf(100L to 50.0, 100L to 91.0),
                    ),
                    "rtr-01" to FakeGateway.Behaviour(
                        readings = listOf(50L to 95.0, 200L to 10.0),
                    ),
                ),
            )
            val service = MonitoringService(gateway)

            runTest {
                val alerts = service.alerts(listOf("cam-04", "rtr-01"), threshold = 90.0).toList()

                alerts shouldContainExactlyInAnyOrder listOf("rtr-01: 95.0", "cam-04: 91.0")
            }
        }

        it("observes the devices at the same time") {
            val gateway = FakeGateway(
                mapOf(
                    "slow" to FakeGateway.Behaviour(readings = listOf(1_000L to 99.0)),
                    "fast" to FakeGateway.Behaviour(readings = listOf(10L to 99.0)),
                ),
            )
            val service = MonitoringService(gateway)

            runTest {
                val alerts = service.alerts(listOf("slow", "fast"), threshold = 90.0).toList()

                // Concurrent: 1000 ms. Sequential: 1010 ms. The order of
                // the first alert is the giveaway.
                alerts.first() shouldBe "fast: 99.0"
                currentTime shouldBe 1_000
            }
        }

        it("emits nothing when nothing crosses the threshold") {
            val gateway = FakeGateway(
                mapOf("cam-04" to FakeGateway.Behaviour(readings = listOf(10L to 20.0))),
            )
            val service = MonitoringService(gateway)

            runTest {
                service.alerts(listOf("cam-04"), threshold = 90.0).toList() shouldContainExactly
                    emptyList()
            }
        }
    }

    describe("task 4 - the summary") {

        it("summarises a mixed dashboard") {
            val reports = listOf(
                DeviceReport.Ok("a", 95, Level.CRITICAL),
                DeviceReport.Ok("b", 50, Level.NORMAL),
                DeviceReport.Ok("c", 5, Level.IDLE),
                DeviceReport.Unreachable("d", "link down"),
                DeviceReport.TimedOut("e", 500),
            )

            MonitoringService(FakeGateway(emptyMap())).summarise(reports) shouldBe
                "5 devices: 3 ok, 1 unreachable, 1 timed out"
        }

        it("summarises an empty dashboard") {
            MonitoringService(FakeGateway(emptyMap())).summarise(emptyList()) shouldBe
                "0 devices: 0 ok, 0 unreachable, 0 timed out"
        }
    }
})
