package exercise

/**
 * Exercise 3 - Idioms: a refactoring.
 *
 * This code WORKS. The tests are green from the start.
 *
 * It is, however, written without using what Kotlin offers for these
 * six cases.
 *
 * The task: rewrite each of the six functions idiomatically. The tests
 * must stay green THROUGHOUT - they are the safety net.
 *
 *     ./gradlew :block1-grundlagen-1:exerciseTest
 *
 * The comment above each function says what it is about. Afterwards no
 * function should need a `return` in the middle of its body, and none
 * should use a StringBuilder any more.
 */

/**
 * 3a - if/else cascade -> `when` as an expression.
 */
fun level(utilisation: Int): String {
    if (utilisation < 10) {
        return "idle"
    } else if (utilisation < 60) {
        return "normal"
    } else if (utilisation < 90) {
        return "high"
    } else {
        return "critical"
    }
}

/**
 * 3b - StringBuilder -> string template.
 */
fun format(name: String, port: Int): String {
    val sb = StringBuilder()
    sb.append(name)
    sb.append(" (port ")
    sb.append(port)
    sb.append(")")
    return sb.toString()
}

/**
 * 3c - three overloads -> one function with default arguments.
 *
 * Careful: after the rewrite there must be only ONE function
 * `connectionUrl`. The tests call it with one, two and three arguments.
 */
fun connectionUrl(host: String): String {
    return connectionUrl(host, 9000)
}

fun connectionUrl(host: String, port: Int): String {
    return connectionUrl(host, port, "tcp")
}

fun connectionUrl(host: String, port: Int, protocol: String): String {
    return protocol + "://" + host + ":" + port
}

/**
 * 3d - a chain of comparisons with && -> a range with `in`.
 */
fun isWithinNormalBand(utilisation: Int): Boolean {
    if (utilisation >= 10 && utilisation <= 90) {
        return true
    }
    return false
}

/**
 * 3e - nested null checks -> safe call, takeIf and Elvis.
 */
fun cleanName(input: String?): String {
    if (input == null) {
        return "unknown"
    }
    val trimmed = input.trim()
    if (trimmed.length == 0) {
        return "unknown"
    }
    return trimmed
}

/**
 * 3f - `var` with a later assignment -> `if` as an expression and `val`.
 */
fun installationSize(channels: Int): String {
    var description: String
    if (channels > 8) {
        description = "large installation"
    } else {
        description = "small installation"
    }
    return description
}

fun main() {
    println(level(85))
    println(format("control-1", 9100))
    println(connectionUrl("control-1"))
    println(isWithinNormalBand(73))
    println(cleanName("  Camera Studio B  "))
    println(installationSize(16))
}
