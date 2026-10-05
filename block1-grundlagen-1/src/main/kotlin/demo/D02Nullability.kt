package demo

/**
 * Demo 2 - Nullability.
 *
 * The core statement: the TYPE says whether null is possible. `String` and
 * `String?` are two different types, and the compiler does not negotiate.
 * This is not a convention and not an annotation - it is part of the type
 * system.
 */

// A device database that sometimes finds nothing.
private val deviceNames = mapOf(
    "rtr-01" to "Router Control Room 1",
    "cam-04" to "Camera Studio B",
)

/** Return type `String?` - the caller MUST deal with null. */
private fun findDeviceName(id: String): String? = deviceNames[id]

fun main() {
    val present: String? = findDeviceName("rtr-01")
    val missing: String? = findDeviceName("does-not-exist")

    // 1) Safe call: yields null instead of throwing.
    println(present?.uppercase())   // ROUTER CONTROL ROOM 1
    println(missing?.uppercase())   // null

    // 2) Elvis operator: a replacement value when the left side is null.
    println(missing ?: "unknown device")

    // 3) Chains: the first null short-circuits the rest.
    val length = missing?.trim()?.length ?: 0
    println("length: $length")

    // 4) Smart cast: after the null check the type is `String`, not `String?`.
    //    No `?.` needed from here on - the compiler followed along.
    if (present != null) {
        println("smart cast, length: ${present.length}")
    }

    // 5) let: run the block only when the value is not null.
    present?.let { println("found: $it") }
    missing?.let { println("this line is never reached") }

    // 6) Leaving early. Possible because `return` and `throw` are
    //    expressions of type `Nothing` in Kotlin, so they may appear on
    //    the right-hand side of an Elvis.
    println("channels rtr-01: ${channelsOrDefault("rtr-01")}")
    println("channels xxx-99: ${channelsOrDefault("xxx-99")}")

    // 7) !! is the escape hatch: it asserts that the value is not null and
    //    throws a NullPointerException if it is. The assertion states an
    //    invariant the compiler cannot verify, which is why the project
    //    convention is to avoid it.
    println(present!!.length)

    // Almost always better than !!, because the error message says something:
    val checked = requireNotNull(findDeviceName("cam-04")) {
        "device cam-04 must be present in the master data"
    }
    println(checked)

    platformTypes()
}

private fun channelsOrDefault(id: String): Int {
    val name = findDeviceName(id) ?: return 0
    return name.length   // stands in for a real computation
}

/**
 * The one gap in the system: Java knows nothing about Kotlin's nullability.
 *
 * Values coming from Java libraries are "platform types" (the IDE shows
 * `String!`). Kotlin enforces nothing there and trusts the caller - so an
 * unexpected NPE can still occur. Hence the rule: write the type explicitly
 * at the system boundary and decide for yourself.
 */
private fun platformTypes() {
    val path: String? = System.getenv("PATH")        // deliberately declared nullable
    println("PATH is set: ${path != null}")
}
