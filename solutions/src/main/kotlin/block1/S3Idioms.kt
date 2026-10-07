package block1

/**
 * Solution for exercise 3 - idioms.
 *
 * After the rewrite all six functions are one-liners with `=`. That is not
 * an end in itself: when a function yields a value instead of performing
 * assignments, there is no intermediate state left for the reader to track.
 */

/**
 * 3a - `when` without an argument replaces the if/else cascade and is an
 * expression. The compiler insists on the else branch here, because the
 * result is used.
 */
fun level(utilisation: Int): String = when {
    utilisation < 10 -> "idle"
    utilisation < 60 -> "normal"
    utilisation < 90 -> "high"
    else -> "critical"
}

/** 3b - a string template instead of a StringBuilder. */
fun format(name: String, port: Int): String = "$name (port $port)"

/**
 * 3c - one function instead of three overloads.
 *
 * The real gain shows at the call site: with default arguments the
 * protocol can be set by name without repeating the port -
 * `connectionUrl("control-1", protocol = "udp")`. Overloads would need yet
 * another variant for that.
 */
fun connectionUrl(
    host: String,
    port: Int = 9000,
    protocol: String = "tcp",
): String = "$protocol://$host:$port"

/**
 * 3d - `in` with a range. It reads like the business rule rather than like
 * its implementation. The `if (…) return true; return false` disappears
 * entirely: the comparison IS the boolean.
 */
fun isWithinNormalBand(utilisation: Int): Boolean = utilisation in 10..90

/**
 * 3e - the chain of safe call, takeIf and Elvis.
 *
 * `isNotEmpty()` is enough here because `trim()` has already run;
 * `isNotBlank()` directly on the input would be the alternative.
 */
fun cleanName(input: String?): String =
    input?.trim()?.takeIf { it.isNotEmpty() } ?: "unknown"

/** 3f - `if` as an expression; the variable disappears along with the `var`. */
fun installationSize(channels: Int): String =
    if (channels > 8) "large installation" else "small installation"

fun main() {
    println(level(85))
    println(format("control-1", 9100))
    println(connectionUrl("control-1"))
    println(connectionUrl("control-1", protocol = "udp"))
    println(isWithinNormalBand(73))
    println(cleanName("  Camera Studio B  "))
    println(installationSize(16))
}
