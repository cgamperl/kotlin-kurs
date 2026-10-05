package demo

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot

/**
 * Demo 5 - Unit testing with Kotest and MockK.
 *
 * These are DEMOS, not exercises: they are green and meant to be read and
 * run during the course.
 *
 *     ./gradlew :block4-coroutines-2:demoTest
 *
 * Kotest offers several "styles". Two are shown here:
 *
 *   StringSpec   - one string, one lambda. Shortest possible form.
 *   DescribeSpec - describe/it, nestable; the structure used by Jasmine,
 *                  Jest and RSpec.
 *
 * Both are ordinary classes; there are no annotations anywhere.
 */
class D05StringSpecDemo : StringSpec({

    "a device is returned when it exists" {
        val repository = mockk<DeviceRepository>()
        coEvery { repository.findById("cam-04") } returns Device("cam-04", "Camera", 42)

        val service = DeviceService(repository)

        service.require("cam-04").name shouldBe "Camera"
    }

    "an unknown device raises an exception" {
        val repository = mockk<DeviceRepository>()
        coEvery { repository.findById(any()) } returns null

        val service = DeviceService(repository)

        // shouldThrow returns the exception, so it can be inspected further.
        val thrown = shouldThrow<DeviceNotFoundException> {
            service.require("gone-01")
        }

        thrown.deviceId shouldBe "gone-01"
        thrown.message shouldContain "gone-01"
    }
})

class D05DescribeSpecDemo : DescribeSpec({

    describe("DeviceService.setUtilisation") {

        it("stores the changed device") {
            val repository = mockk<DeviceRepository>()
            coEvery { repository.findById("cam-04") } returns Device("cam-04", "Camera", 42)

            // `just Runs` is the MockK way of stubbing a function returning Unit.
            coEvery { repository.save(any()) } just Runs

            val service = DeviceService(repository)
            val updated = service.setUtilisation("cam-04", 80)

            updated.utilisation shouldBe 80

            // coVerify is the suspending counterpart of verify.
            coVerify(exactly = 1) { repository.save(Device("cam-04", "Camera", 80)) }
        }

        it("captures the argument that was passed") {
            val repository = mockk<DeviceRepository>()
            coEvery { repository.findById("cam-04") } returns Device("cam-04", "Camera", 42)

            // A slot captures the actual argument - useful when the
            // expected value is not convenient to construct up front.
            val saved = slot<Device>()
            coEvery { repository.save(capture(saved)) } just Runs

            DeviceService(repository).setUtilisation("cam-04", 55)

            saved.captured.utilisation shouldBe 55
            saved.captured.id shouldBe "cam-04"
        }

        it("rejects a utilisation outside the valid range") {
            val repository = mockk<DeviceRepository>()
            val service = DeviceService(repository)

            shouldThrow<IllegalArgumentException> {
                service.setUtilisation("cam-04", 101)
            }

            // Nothing should have been read or written.
            coVerify(exactly = 0) { repository.findById(any()) }
            coVerify(exactly = 0) { repository.save(any()) }
        }
    }

    describe("data driven testing") {

        // withData runs the same block for every input and reports each
        // one as its own test, which is more informative than a loop
        // inside a single test.
        withData(0, 1, 50, 99, 100) { utilisation ->
            val repository = mockk<DeviceRepository>()
            coEvery { repository.findById("cam-04") } returns Device("cam-04", "Camera", 0)
            coEvery { repository.save(any()) } just Runs

            DeviceService(repository).setUtilisation("cam-04", utilisation).utilisation shouldBe utilisation
        }
    }

    describe("a hand written fake") {

        // A mocking library is not always required. A fake is often
        // clearer, survives refactoring better and needs no stubbing per
        // test.
        it("works without MockK") {
            val repository = InMemoryDeviceRepository(
                Device("cam-04", "Camera", 42),
                Device("rtr-01", "Router", 95),
            )

            val service = DeviceService(repository)

            service.require("rtr-01").utilisation shouldBe 95
            service.setUtilisation("cam-04", 10)

            repository.findById("cam-04")?.utilisation shouldBe 10
            repository.saved shouldHaveSize 1
        }
    }

    describe("a few matchers") {

        it("expresses assertions directly") {
            val devices = listOf(
                Device("cam-04", "Camera", 42),
                Device("rtr-01", "Router", 95),
            )

            devices shouldHaveSize 2
            devices.map { it.id } shouldContainExactly listOf("cam-04", "rtr-01")
            devices.firstOrNull { it.id == "nope" }.shouldBeNull()
        }
    }
})

/** A hand written fake, used above. */
private class InMemoryDeviceRepository(vararg devices: Device) : DeviceRepository {
    private val store = devices.associateBy { it.id }.toMutableMap()
    val saved = mutableListOf<Device>()

    override suspend fun findById(id: String): Device? = store[id]

    override suspend fun save(device: Device) {
        store[device.id] = device
        saved += device
    }

    override suspend fun ping(id: String): Boolean = id in store
}
