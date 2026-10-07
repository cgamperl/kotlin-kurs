package block3

import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class S3DashboardTest {

    // ----------------------------------------------------------------- 3a
    @Test
    fun `collects a complete snapshot`() = runBlocking {
        val snapshot = deviceSnapshot("cam-04")

        assertEquals(
            DeviceSnapshot.Complete("cam-04", "online", 42, 21.5),
            snapshot,
        )
    }

    @Test
    fun `queries the three sources concurrently`() = runBlocking {
        // 150 + 200 + 180 = 530 ms sequentially, about 200 ms concurrently.
        val millis = measureTimeMillis { deviceSnapshot("cam-04") }

        assertTrue(
            millis < 400,
            "the snapshot took $millis ms - the sources were queried one after another",
        )
    }

    @Test
    fun `falls back to a partial snapshot on timeout`() = runBlocking {
        val snapshot = deviceSnapshot("slow-01", temperatureTimeoutMillis = 300)

        assertEquals(
            DeviceSnapshot.Partial("slow-01", "online", 49),
            snapshot,
        )
    }

    @Test
    fun `does not wait longer than the timeout allows`() = runBlocking {
        // The temperature would need 2000 ms; the timeout cuts it at 300 ms.
        val millis = measureTimeMillis {
            deviceSnapshot("slow-01", temperatureTimeoutMillis = 300)
        }

        assertTrue(
            millis < 800,
            "waited $millis ms - the timeout did not take effect",
        )
    }

    @Test
    fun `keeps a slow but tolerable source`() = runBlocking {
        // 180 ms for the temperature, timeout of 500 ms - must stay complete.
        val snapshot = deviceSnapshot("rtr-01", temperatureTimeoutMillis = 500)

        assertTrue(snapshot is DeviceSnapshot.Complete)
    }

    // ----------------------------------------------------------------- 3b
    @Test
    fun `builds the dashboard in input order`() = runBlocking {
        val snapshots = dashboard(listOf("cam-04", "rtr-01", "int-07"))

        assertEquals(listOf("cam-04", "rtr-01", "int-07"), snapshots.map { it.deviceId })
    }

    @Test
    fun `builds the dashboard concurrently`() = runBlocking {
        val deviceIds = List(8) { "dev-$it" }

        val millis = measureTimeMillis { dashboard(deviceIds) }

        assertTrue(
            millis < 600,
            "eight devices took $millis ms - they were not collected at the same time",
        )
    }

    @Test
    fun `one slow device does not hold up the others`() = runBlocking {
        val millis = measureTimeMillis {
            dashboard(listOf("cam-04", "slow-01", "rtr-01"), temperatureTimeoutMillis = 300)
        }

        assertTrue(
            millis < 800,
            "the dashboard took $millis ms - the slow device blocked the rest",
        )
    }

    @Test
    fun `handles an empty dashboard`() = runBlocking {
        assertEquals(emptyList(), dashboard(emptyList()))
    }

    // ----------------------------------------------------------------- 3c
    @Test
    fun `renders a complete snapshot`() {
        assertEquals(
            "cam-04: online, 42 %, 21.5 °C",
            renderLine(DeviceSnapshot.Complete("cam-04", "online", 42, 21.5)),
        )
    }

    @Test
    fun `renders a partial snapshot`() {
        assertEquals(
            "slow-01: online, 49 %, temperature unavailable",
            renderLine(DeviceSnapshot.Partial("slow-01", "online", 49)),
        )
    }

    // ----------------------------------------------------------------- 3d
    @Test
    fun `counts the incomplete snapshots`() {
        val snapshots = listOf(
            DeviceSnapshot.Complete("a", "online", 1, 20.0),
            DeviceSnapshot.Partial("b", "online", 2),
            DeviceSnapshot.Partial("c", "online", 3),
        )

        assertEquals(2, countIncomplete(snapshots))
        assertEquals(0, countIncomplete(emptyList()))
    }
}
