package demo

/**
 * Demo 1 – Project setup and the first file.
 *
 * The first thing to notice: this file contains NO class. In Kotlin,
 * functions and properties may live directly at the top level of a file.
 * You write a class when you want to bundle state - not to have somewhere
 * to put a function.
 */

// Top-level constant. `const` means: known at compile time, inlined into
// the bytecode as a literal. Only allowed for primitives and String.
const val COURSE_NAME = "Kotlin Training"

/**
 * Top-level function.
 *
 * There are no free functions on the JVM, so the compiler generates a class
 * `D01ProjectSetupKt` (file name + "Kt") holding a static method. That name
 * shows up twice later on: when calling this from Java, and when starting a
 * demo via `./gradlew run -PmainClass=...`.
 */
fun greeting(name: String): String = "Welcome to: $name"

// Expression body: the body is a single expression after `=`.
// No `return`, no braces, return type inferred.
private fun square(x: Int) = x * x

fun main() {
    println(greeting(COURSE_NAME))

    // String templates: $name for a plain identifier,
    // ${...} for any expression.
    val modules = 5
    println("This course project has $modules modules, ${modules - 1} of them blocks.")

    println("7 squared is ${square(7)}")

    // main() needs no parameters. If you want the command line arguments,
    // write: fun main(args: Array<String>)
}
