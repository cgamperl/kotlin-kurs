package demo

/**
 * Demo 8 – Kotlin idioms (collection).
 *
 * Many small things that together make the difference between
 * "written in Kotlin" and "written in Java with Kotlin syntax".
 */

fun main() {
    defaultAndNamedArguments()
    println()
    stringTemplates()
    println()
    whenAsExpression()
    println()
    ranges()
    println()
    takeIfAndTakeUnless()
    println()
    ifAsExpression()
}

// ---------------------------------------------------------------- 1
/**
 * Default arguments replace overloads. This single function covers what
 * would otherwise need four overloads.
 */
private fun connectionUrl(
    host: String,
    port: Int = 9000,
    protocol: String = "tcp",
    timeoutSeconds: Int = 30,
): String = "$protocol://$host:$port?timeout=$timeoutSeconds"

private fun defaultAndNamedArguments() {
    println(connectionUrl("control-1"))
    println(connectionUrl("control-1", 9100))

    // Named arguments: order does not matter, and the call explains itself.
    // That is what they are for - not for every parameter, but wherever a
    // bare literal says nothing.
    println(connectionUrl("control-1", protocol = "udp", timeoutSeconds = 5))

    // Compare: connectionUrl("control-1", 9100, "udp", 5)
    // reads considerably worse at the call site.
}

// ---------------------------------------------------------------- 2
private fun stringTemplates() {
    val device = Device(DeviceId("cam-04"), "Camera Studio B", DeviceType.CAMERA)

    println("device ${device.name} (${device.id}) of type ${device.type}")

    // Multi-line raw strings: no escaping, ideal for JSON, SQL, messages.
    val report = """
        Device report
        =============
        id:       ${device.id}
        name:     ${device.name}
        firmware: ${device.firmware}
    """.trimIndent()
    println(report)
}

// ---------------------------------------------------------------- 3
private fun whenAsExpression() {
    // `when` yields a VALUE. No assignment in every branch, no break.
    for (utilisation in listOf(5, 45, 85, 99)) {
        val level = when {
            utilisation < 10 -> "idle"
            utilisation < 60 -> "normal"
            utilisation < 90 -> "high"
            else -> "critical"
        }
        println("$utilisation% -> $level")
    }

    // `when` with an argument, including several values per branch.
    for (type in DeviceType.entries) {
        val category = when (type) {
            DeviceType.CAMERA, DeviceType.INTERCOM -> "media technology"
            DeviceType.ROUTER -> "network"
            DeviceType.SENSOR -> "peripherals"
        }
        println("$type -> $category")
    }
    // Because every enum value is covered, no `else` is needed.
    // If a value is added tomorrow, the compiler complains right here.
}

// ---------------------------------------------------------------- 4
private fun ranges() {
    println((1..5).toList())            // 1, 2, 3, 4, 5   – inclusive
    println((1..<5).toList())           // 1, 2, 3, 4      – exclusive
    println((1..10 step 3).toList())    // 1, 4, 7, 10
    println((5 downTo 1).toList())      // 5, 4, 3, 2, 1

    // Ranges are useful outside loops too: `in` reads better than two
    // comparisons joined with &&.
    val utilisation = 73
    println("within the normal band: ${utilisation in 10..90}")

    val character = 'x'
    println("lower case letter: ${character in 'a'..'z'}")

    // `repeat` for "do the same thing n times":
    repeat(3) { i -> print("ping ${i + 1} ") }
    println()
}

// ---------------------------------------------------------------- 5
private fun takeIfAndTakeUnless() {
    // takeIf returns the value when the condition holds - null otherwise.
    // That lets you put a check inside a chain instead of pulling it out
    // into a preceding if.
    val input = "  cam-04  "
    val id = input.trim().takeIf { it.isNotEmpty() } ?: "unknown"
    println("id: $id")

    val blank = "   "
    println("blank -> ${blank.trim().takeIf { it.isNotEmpty() } ?: "default value"}")

    // takeUnless is the same with the condition inverted. Use it where the
    // negative phrasing is the more natural one.
    val temperature = 85.0
    val unremarkable = temperature.takeUnless { it > 70.0 }
    println("unremarkable temperature: $unremarkable")     // null, too hot
}

// ---------------------------------------------------------------- 6
private fun ifAsExpression() {
    val channels = 16

    // `if` yields a value - which is why Kotlin has no ternary operator:
    // it would be redundant.
    val description = if (channels > 8) "large installation" else "small installation"
    println(description)

    // With blocks too: the last expression of the block is the result.
    val assessment = if (channels > 8) {
        val spare = channels - 8
        "large installation with $spare spare channels"
    } else {
        "small installation"
    }
    println(assessment)
}
