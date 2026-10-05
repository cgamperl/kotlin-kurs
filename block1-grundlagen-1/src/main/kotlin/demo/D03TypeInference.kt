package demo

/**
 * Demo 3 - Type inference.
 *
 * A short topic, but with one team rule that carries:
 * let it be inferred locally, write it out on the public API.
 */

fun main() {
    // Inferred and unambiguous - spelling out the type here would be noise.
    val id = "cam-04"
    val channels = 16
    val utilisation = 0.73
    val active = true

    // A pitfall: integer literals are Int, not Long. There is no implicit
    // widening - an Int never silently becomes a Long.
    val small = 42            // Int
    val large = 42L           // Long
    val precise = 42.0f       // Float
    println("$small $large $precise")

    // Here inference is not enough: without <String> the compiler has no
    // way to know at this point what will go into the list.
    val selected = mutableListOf<String>()
    selected += id

    // When the type serves as documentation, write it down:
    val threshold: Double = 0.9

    println("$id: $channels channels, utilisation $utilisation (limit $threshold), active=$active")
    println(summary(selected))
}

/**
 * The return type is spelled out on the public API.
 *
 * Reason: otherwise a refactoring inside the body silently changes the
 * signature - and with it the contract for every caller. Inside a function
 * that does not matter; across a module boundary it does.
 */
fun summary(ids: List<String>): String = "managed devices: ${ids.size}"
