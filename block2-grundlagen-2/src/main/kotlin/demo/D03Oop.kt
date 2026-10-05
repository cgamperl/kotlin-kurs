package demo

/**
 * Demo 3 – OOP in Kotlin.
 *
 * The rule to keep in mind:
 * **classes and methods are final unless `open` says otherwise.**
 * Inheritance is a deliberate decision, not a default.
 */

// ------------------------------------------------------------------ 1
/**
 * A primary constructor with properties declared directly in the parameter
 * list. `val id` creates the property, its getter and the assignment in one.
 */
open class Component(
    val id: String,
    initialLocation: String,        // constructor parameter only, not a property
) {
    /**
     * A property with a backing field and a custom setter.
     * Inside get()/set() the storage is reached through `field` - writing
     * `this.location = …` in the setter would recurse forever.
     */
    var location: String = initialLocation
        set(value) {
            require(value.isNotBlank()) { "location must not be blank" }
            println("  relocated: $field -> $value")
            field = value
        }

    /** A computed property - no backing field, evaluated on every access. */
    val description: String
        get() = "$id @ $location"

    /**
     * The init block runs as part of the primary constructor, in the order
     * in which it appears between the property initialisers.
     */
    init {
        println("component $id created")
    }

    /** Without `open` this method could not be overridden. */
    open fun selfTest(): String = "$id: ok"
}

/**
 * A secondary constructor. It MUST call the primary one (`: this(...)`).
 *
 * In practice you rarely need one - default arguments usually do the same
 * job with less code.
 */
class Sensor(id: String, location: String, val unit: String) :
    Component(id, location) {

    constructor(id: String) : this(id, "Storage", "°C")

    override fun selfTest(): String = "$id: ok, measuring in $unit"
}

// ------------------------------------------------------------------ 2
/**
 * An interface with a default implementation - and with a property the
 * implementing class has to supply.
 */
interface Maintainable {
    val intervalMonths: Int

    fun maintenanceHint(): String = "maintenance every $intervalMonths months"
}

interface Logging {
    fun log(text: String) = println("[log] $text")
}

/**
 * Several interfaces are allowed, only one base class.
 */
class CameraUnit(
    id: String,
    location: String,
) : Component(id, location), Maintainable, Logging {

    override val intervalMonths = 6

    override fun selfTest(): String {
        log("self test for $id")
        return "$id: video signal ok"
    }
}

// ------------------------------------------------------------------ 3
/**
 * Explicit backing field (stable since Kotlin 2.4).
 *
 * The problem before: you want to keep a `MutableList` internally but hand
 * out only a `List`. That took two properties - a private mutable one and
 * a public view onto it.
 *
 * With `field` this fits into one declaration: the property type is what
 * the outside sees, the field type is what is used inside.
 */
class EventStore {
    val events: List<String>
        field = mutableListOf()

    fun record(text: String) {
        // Inside the class `events` is the MutableList …
        events.add(text)
    }
}

fun main() {
    val sensor = Sensor("sen-12", "Plant Room", "°C")
    println(sensor.description)
    println(sensor.selfTest())

    sensor.location = "Studio B"
    println(sensor.description)

    println()
    val fromStorage = Sensor("sen-13")
    println(fromStorage.description)

    println()
    val camera = CameraUnit("cam-04", "Studio B")
    println(camera.selfTest())
    println(camera.maintenanceHint())

    println()
    val store = EventStore()
    store.record("start")
    store.record("signal lost")
    println("events: ${store.events}")
    // store.events.add("…")   // not possible from outside: there it is a List
}
