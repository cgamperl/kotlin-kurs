package demo

/**
 * Demo 1 – Generics.
 *
 * Deliberately at the start of block 2: enum classes, sealed classes and
 * the whole collections API build on it.
 *
 * Kotlin generics are compiled ONCE and erased at runtime (type erasure
 * on the JVM). Whatever the compiler is meant to check therefore has to
 * appear in the bound (`: Comparable<T>`); it cannot surface only when a
 * concrete type is substituted.
 */

// ------------------------------------------------------------------ 1
/** A generic function. `T` is inferred from the argument. */
fun <T> secondOrNull(list: List<T>): T? = list.getOrNull(1)

/**
 * An upper bound: `T` must be comparable. Without the bound, `a > b` would
 * not be allowed in the body.
 */
fun <T : Comparable<T>> larger(a: T, b: T): T = if (a > b) a else b

/**
 * Several bounds need the `where` clause.
 */
fun <T> describeAll(values: List<T>): String
    where T : Comparable<T>,
          T : CharSequence =
    values.sorted().joinToString(", ") { "${it.length} characters" }

// ------------------------------------------------------------------ 2
/**
 * A generic class with `out`: COVARIANCE.
 *
 * `out T` means T only ever appears in out positions - the class produces
 * T, it never consumes one. That is why a `Source<Camera>` may stand where
 * a `Source<Equipment>` is expected.
 */
interface Source<out T> {
    fun get(): T
}

/**
 * `in`: CONTRAVARIANCE. The class consumes T but never hands one out.
 * A `Sink<Equipment>` may stand wherever a `Sink<Camera>` is required -
 * whoever can process equipment can process cameras too.
 */
interface Sink<in T> {
    fun accept(value: T)
}

open class Equipment(val label: String) {
    override fun toString(): String = label
}

class Camera(label: String) : Equipment(label)

// ------------------------------------------------------------------ 3
/**
 * `reified` together with `inline`: the type is available at runtime,
 * because the compiler inlines the body at the call site and substitutes
 * the concrete type.
 *
 * Without `reified`, `is T` would not work - the type would be erased.
 */
inline fun <reified T> onlyOfType(values: List<Any>): List<T> =
    values.filterIsInstance<T>()

inline fun <reified T> typeName(): String = T::class.simpleName ?: "unknown"

fun main() {
    // ---- generic functions
    println(secondOrNull(listOf("a", "b", "c")))
    println(secondOrNull(listOf(1)))
    println(larger(3, 7))
    println(larger("Camera", "Router"))
    println(describeAll(listOf("cam-04", "rtr-1")))

    // ---- variance
    println()
    val cameraSource: Source<Camera> = object : Source<Camera> {
        override fun get() = Camera("Camera Studio B")
    }

    // Allowed, because Source is declared with `out`:
    val equipmentSource: Source<Equipment> = cameraSource
    println("from the source: ${equipmentSource.get()}")

    val equipmentSink: Sink<Equipment> = object : Sink<Equipment> {
        override fun accept(value: Equipment) = println("processed: $value")
    }

    // Allowed, because Sink is declared with `in`:
    val cameraSink: Sink<Camera> = equipmentSink
    cameraSink.accept(Camera("Camera Studio A"))

    // Exactly this pattern is baked into the standard library:
    // List<out E> is covariant, so a List<Camera> is a List<Equipment>.
    val cameras: List<Camera> = listOf(Camera("A"), Camera("B"))
    val equipment: List<Equipment> = cameras
    println("list as the supertype: $equipment")

    // MutableList<E> is NOT covariant - it both consumes and produces.
    // val m: MutableList<Equipment> = mutableListOf(Camera("A"))   // compile error

    // ---- reified
    println()
    val mixed: List<Any> = listOf("cam-04", 42, "rtr-01", 3.14)
    println(onlyOfType<String>(mixed))
    println(onlyOfType<Int>(mixed))
    println("the type parameter was: ${typeName<Device>()}")
}
