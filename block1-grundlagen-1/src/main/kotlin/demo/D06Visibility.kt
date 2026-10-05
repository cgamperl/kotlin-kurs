package demo

/**
 * Demo 6 – Visibility modifiers.
 *
 * Kotlin has four: public (the default), private, protected, internal.
 *
 * Two properties worth noting:
 *  - The default is `public`, not package-private.
 *  - There is NO package-private visibility. When you mean "only within
 *    this building block", you use `internal`.
 */

// No modifier: public. Visible everywhere, including from other modules.
const val MAX_DEVICES = 512

/**
 * `internal` means: visible throughout the whole Gradle module, but not
 * beyond it.
 *
 * That is exactly why this course project is split into several modules:
 * this function is reachable anywhere in `block1-grundlagen-1`, but not
 * from `block2-grundlagen-2` - not even with the same package name.
 */
internal fun internalDiagnosticsPath(): String = "/var/log/devices"

// `private` at the top level means: visible only in THIS FILE.
private const val SECRET_KEY = "do-not-log-this"

open class DeviceRegistry {
    // private: only inside this class.
    private val devices = mutableListOf<Device>()

    // protected: in this class and in subclasses - but, unlike Java,
    // not in the same package.
    protected open fun onAdded(device: Device) {
        println("registered: ${device.id}")
    }

    fun add(device: Device) {
        devices += device
        onAdded(device)
    }

    val count: Int get() = devices.size
}

fun main() {
    val registry = DeviceRegistry()
    registry.add(Device(DeviceId("rtr-01"), "Router Control Room 1", DeviceType.ROUTER))

    println("count: ${registry.count} of at most $MAX_DEVICES")
    println("diagnostics path: ${internalDiagnosticsPath()}")
    println("the key is ${SECRET_KEY.length} characters long")

    // registry.devices    // compile error: private
    // registry.onAdded()  // compile error: protected
}
