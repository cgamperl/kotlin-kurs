package demo

/**
 * Demo 4 - Immutability.
 *
 * Two levels that get mixed up regularly:
 *   1. val / var          -> may the REFERENCE be reassigned?
 *   2. List / MutableList -> may the CONTENT be changed?
 *
 * Question for the room: when is `var` genuinely justified?
 * (In most cases a new `val` binding or a `copy()` does the job.)
 */

fun main() {
    referenceVersusContent()
    println()
    readOnlyIsNotACopy()
    println()
    defensiveCopy()
}

private fun referenceVersusContent() {
    var counter = 0
    counter += 1                     // allowed: var

    val name = "Camera Studio B"
    // name = "Camera Studio C"      // compile error: val cannot be reassigned

    // The decisive distinction: `val` protects the reference, not the
    // content.
    val ids = mutableListOf("cam-01")
    ids += "cam-02"                  // allowed! The list itself is mutable.

    println("counter=$counter, name=$name, ids=$ids")
}

private fun readOnlyIsNotACopy() {
    val mutable = mutableListOf("cam-01", "cam-02")

    // `List` is a READ-ONLY VIEW, not an immutable list.
    // You cannot change anything through `view` - but you can through `mutable`.
    val view: List<String> = mutable

    println("view before: $view")
    mutable += "cam-03"
    println("view after:  $view")     // the change is visible: same object!
}

private fun defensiveCopy() {
    val internal = mutableListOf("cam-01", "cam-02")

    // toList() copies. Returning that, you can be sure the caller cannot
    // touch your internal state any more.
    val published = internal.toList()

    internal += "cam-03"

    println("internal:  $internal")
    println("published: $published")

    // Genuinely immutable collections come from the ...Of functions:
    val constant = listOf("a", "b")
    val mapping = mapOf("cam-01" to 4, "cam-02" to 2)
    println("$constant $mapping")
}
