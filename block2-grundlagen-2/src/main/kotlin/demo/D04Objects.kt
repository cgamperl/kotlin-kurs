package demo

/**
 * Demo 4 – Object declarations and companion objects.
 *
 * `object` covers three things that other languages need three separate
 * constructs for: singleton, static members, and anonymous class.
 */

// ------------------------------------------------------------------ 1
/**
 * An object declaration = a singleton.
 *
 * The instance is created on first access, thread-safely, without you
 * having to do anything. No double-checked locking, no static
 * initialisation order problem.
 */
object DeviceRegistry {
    private val registered = mutableMapOf<DeviceId, Device>()

    val count: Int get() = registered.size

    fun register(device: Device) {
        registered[device.id] = device
    }

    fun find(id: DeviceId): Device? = registered[id]
}

// ------------------------------------------------------------------ 2
/**
 * A companion object: what is called `static` elsewhere.
 *
 * Important: it is a real object. It can implement interfaces and be
 * passed around as a value - static members cannot.
 */
class Connection private constructor(
    val target: String,
    val port: Int,
) {
    fun describe(): String = "$target:$port"

    companion object {
        // Constants belong here.
        const val DEFAULT_PORT = 9000

        /**
         * A factory method. The constructor is private - whoever wants a
         * connection goes through the factory. That allows validation and,
         * later, returning cached instances without touching any call site.
         */
        fun to(device: Device): Connection =
            Connection(device.name, device.type.defaultPort)

        fun fromUrl(url: String): Connection? {
            val parts = url.split(":")
            if (parts.size != 2) return null
            val port = parts[1].toIntOrNull() ?: return null
            return Connection(parts[0], port)
        }

        /**
         * `invoke` in the companion makes the class look as though it had a
         * constructor - even though a factory sits behind it.
         */
        operator fun invoke(target: String): Connection =
            Connection(target, DEFAULT_PORT)
    }
}

// ------------------------------------------------------------------ 3
/** An interface for the object expression further down. */
interface ChangeObserver {
    fun onChange(device: Device, previousUtilisation: Int)
}

fun main() {
    // ---- singleton
    DeviceRegistry.register(sampleDevices[0])
    DeviceRegistry.register(sampleDevices[1])
    println("registered: ${DeviceRegistry.count}")
    println(DeviceRegistry.find(DeviceId("cam-04")))

    // There is no second instance - that is not a convention but a
    // guarantee from the compiler.

    // ---- companion / factory
    println()
    val camera = sampleDevices[0]
    println(Connection.to(camera).describe())
    println(Connection.fromUrl("control-1:9100")?.describe())
    println(Connection.fromUrl("broken")?.describe() ?: "invalid url")

    // Thanks to invoke() the call looks like a constructor:
    println(Connection("control-2").describe())
    println("default port: ${Connection.DEFAULT_PORT}")

    // ---- object expression (anonymous class)
    println()
    val observer = object : ChangeObserver {
        var notifications = 0

        override fun onChange(device: Device, previousUtilisation: Int) {
            notifications++
            println("${device.id}: $previousUtilisation -> ${device.utilisation}")
        }
    }

    observer.onChange(camera.copy(utilisation = 70), 42)
    observer.onChange(camera.copy(utilisation = 91), 70)
    // Unlike a lambda, an object expression may hold its own state -
    // here the counter:
    println("notifications: ${observer.notifications}")
}
