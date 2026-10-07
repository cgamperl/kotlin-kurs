package block2

/**
 * Solution for exercise 4 - scope functions.
 *
 * Each of the five functions gets shorter. More important than the
 * brevity is that every scope function expresses an intent: `apply` means
 * "configure", `also` means "do something on the side", `let` means "only
 * if present".
 */

class DeviceConfiguration {
    var target: String = ""
    var port: Int = 9000
    var timeoutSeconds: Int = 30
    var tls: Boolean = false

    fun asText(): String =
        "${if (tls) "tls" else "tcp"}://$target:$port (timeout ${timeoutSeconds}s)"
}

data class DeviceEntry(
    val id: String,
    val name: String,
    val location: String,
    val utilisation: Int,
)

val sampleEntries = listOf(
    DeviceEntry("cam-04", "Camera Studio B", "Studio B", 42),
    DeviceEntry("rtr-01", "Router Control Room 1", "Control Room 1", 95),
    DeviceEntry("int-07", "Intercom Desk", "Control Room 1", 5),
    DeviceEntry("sen-12", "Temperature Sensor", "Plant Room", 91),
)

/**
 * 4a - `let` behind a safe call.
 *
 * The block runs only when the value is not null; the Elvis operator
 * supplies the replacement text otherwise. An if/return/return turns into
 * a single expression.
 */
fun message(entry: DeviceEntry?): String =
    entry?.let { "${it.name} is running at ${it.utilisation} %" }
        ?: "device not found"

/**
 * 4b - `apply` returns the receiver.
 *
 * That is why there is no `return configuration` at the end any more, and
 * why the object name in front of every assignment disappears.
 */
fun standardConfiguration(target: String): DeviceConfiguration =
    DeviceConfiguration().apply {
        this.target = target    // `this.` is needed because the parameter has the same name
        port = 9100
        tls = true
        timeoutSeconds = 5
    }

/**
 * 4c - `with`, when the same object is addressed several times.
 *
 * Inside the block the entry is the receiver, so the bare property names
 * suffice in the string template.
 */
fun profile(entry: DeviceEntry): String = with(entry) {
    """
    id: $id
    name: $name
    location: $location
    utilisation: $utilisation %
    """.trimIndent()
}

/**
 * 4d - `also` logs without breaking the chain.
 *
 * `also` passes the receiver on unchanged, which is why it can be inserted
 * and removed anywhere without affecting the result. That is exactly what
 * makes it the right tool for logging.
 */
fun criticalNames(entries: List<DeviceEntry>, log: MutableList<String>): List<String> =
    entries
        .also { log.add("incoming: ${it.size}") }
        .filter { it.utilisation >= 90 }
        .also { log.add("critical: ${it.size}") }
        .map { it.name }
        .also { log.add("names: ${it.size}") }

/**
 * 4e - `run` yields the result of the block.
 *
 * The only difference from `with` is the notation: here the object stands
 * in front of the dot, which reads better inside a chain.
 */
fun rating(entry: DeviceEntry): String = entry.run {
    val level = when {
        utilisation < 10 -> "idle"
        utilisation < 60 -> "normal"
        utilisation < 90 -> "high"
        else -> "critical"
    }
    "$id is $level"
}

fun main() {
    println(message(sampleEntries.first()))
    println(message(null))
    println(standardConfiguration("control-1").asText())
    println(profile(sampleEntries.first()))

    val log = mutableListOf<String>()
    println(criticalNames(sampleEntries, log))
    log.forEach { println("  $it") }

    println(rating(sampleEntries[1]))
}
