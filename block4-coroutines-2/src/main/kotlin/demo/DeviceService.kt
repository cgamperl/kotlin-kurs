package demo

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * The subject under test for demos 5 and 6.
 *
 * The implementation is deliberately unremarkable and shaped like the code
 * that typically ends up under test: a service with one dependency, some
 * suspending calls, a retry and a flow.
 */

data class Device(
    val id: String,
    val name: String,
    val utilisation: Int,
)

/**
 * The dependency. An interface, so it can be replaced in a test - with
 * MockK in demo 5, and by hand wherever that is simpler.
 */
interface DeviceRepository {
    suspend fun findById(id: String): Device?
    suspend fun save(device: Device)
    suspend fun ping(id: String): Boolean
}

class DeviceNotFoundException(val deviceId: String) :
    NoSuchElementException("no device with id $deviceId")

class DeviceService(
    private val repository: DeviceRepository,
) {

    /** Throws if the device does not exist - a case worth testing. */
    suspend fun require(id: String): Device =
        repository.findById(id) ?: throw DeviceNotFoundException(id)

    /** Reads, changes, writes back - the case for verifying that save was called. */
    suspend fun setUtilisation(id: String, utilisation: Int): Device {
        require(utilisation in 0..100) { "utilisation must be within 0..100" }

        val device = require(id)
        val updated = device.copy(utilisation = utilisation)
        repository.save(updated)
        return updated
    }

    /**
     * Retries with a growing delay. Virtual time in demo 6 makes this
     * testable without actually waiting seconds.
     */
    suspend fun pingWithRetry(id: String, attempts: Int = 3): Boolean {
        repeat(attempts) { attempt ->
            if (repository.ping(id)) return true
            delay((attempt + 1).seconds)
        }
        return false
    }

    /** A flow that reports the utilisation at a fixed interval. */
    fun observeUtilisation(id: String, intervalMillis: Long = 1_000): Flow<Int> = flow {
        while (true) {
            val device = repository.findById(id) ?: return@flow
            emit(device.utilisation)
            delay(intervalMillis.milliseconds)
        }
    }
}
