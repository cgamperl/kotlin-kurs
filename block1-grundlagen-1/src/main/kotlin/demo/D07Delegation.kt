package demo

import kotlin.properties.Delegates

/**
 * Demo 7 – Delegation.
 *
 * Two different things share the name:
 *   1. interface delegation (class X : I by y)      – composition without boilerplate
 *   2. property delegation  (val x by lazy { ... }) – handing access to another object
 */

interface DeviceSource {
    fun all(): List<Device>
    fun find(id: DeviceId): Device?
}

/** A plain implementation that we reuse right away. */
class InMemoryDeviceSource(private val devices: List<Device>) : DeviceSource {
    override fun all(): List<Device> = devices
    override fun find(id: DeviceId): Device? = devices.firstOrNull { it.id == id }
}

/**
 * Interface delegation: `by source` generates every method of the interface
 * and forwards it to `source`.
 *
 * We override only the one behaviour we care about - the rest stays as it
 * is, without a single line of forwarding code. And when the interface gains
 * a method tomorrow, nothing here has to be updated.
 */
class LoggingDeviceSource(
    private val source: DeviceSource,
) : DeviceSource by source {

    override fun find(id: DeviceId): Device? {
        println("  [log] looking for $id")
        return source.find(id)
    }
}

/**
 * Property delegation.
 */
class Diagnostics {

    /**
     * `by lazy`: the block runs on the FIRST read access, after that the
     * remembered value is returned. Thread-safe by default (synchronized).
     */
    val expensiveMetric: Int by lazy {
        println("  [lazy] computing now …")
        (1..1_000_000).sum() % 97
    }

    /**
     * `Delegates.observable`: the callback fires on every assignment.
     * Useful for state changes that are to be logged.
     */
    var status: String by Delegates.observable("unknown") { _, old, new ->
        println("  [observable] status: $old -> $new")
    }

    /**
     * `Delegates.vetoable`: an assignment can be rejected.
     */
    var utilisation: Int by Delegates.vetoable(0) { _, _, new ->
        val allowed = new in 0..100
        if (!allowed) println("  [vetoable] $new rejected (allowed: 0..100)")
        allowed
    }
}

fun main() {
    val devices = listOf(
        Device(DeviceId("cam-04"), "Camera Studio B", DeviceType.CAMERA),
        Device(DeviceId("rtr-01"), "Router Control Room 1", DeviceType.ROUTER),
    )

    println("— interface delegation —")
    val source: DeviceSource = LoggingDeviceSource(InMemoryDeviceSource(devices))
    println(source.find(DeviceId("rtr-01")))
    // all() was never implemented and works anyway:
    println("forwarded: ${source.all().size} devices")

    println()
    println("— by lazy —")
    val diagnostics = Diagnostics()
    println("the object exists, nothing has been computed yet.")
    println("first access:  ${diagnostics.expensiveMetric}")
    println("second access: ${diagnostics.expensiveMetric}")   // no recomputation

    println()
    println("— observable / vetoable —")
    diagnostics.status = "online"
    diagnostics.status = "maintenance"

    diagnostics.utilisation = 42
    diagnostics.utilisation = 150        // gets rejected
    println("utilisation stays: ${diagnostics.utilisation}")
}
