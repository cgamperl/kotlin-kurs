package block1

/**
 * Solution for exercise 2 - data classes and value classes.
 *
 * The guiding idea: the checks move into the type. Whoever holds a
 * `Percent` no longer has to ask whether the value is plausible - that was
 * decided when it was created, and there is no way around it.
 */

@JvmInline
value class SensorId(val value: String) {
    init {
        // require throws IllegalArgumentException. The message is a lambda
        // and is only evaluated when the condition is violated.
        require(value.isNotBlank()) { "SensorId must not be blank" }
    }

    override fun toString(): String = value
}

@JvmInline
value class Percent(val value: Int) {
    init {
        require(value in 0..100) { "Percent must be within 0..100, was: $value" }
    }

    // Computed property without a backing field - value classes allow
    // exactly that.
    val isCritical: Boolean
        get() = value >= 90

    override fun toString(): String = "$value %"
}

data class Reading(
    val sensor: SensorId,
    val utilisation: Percent,
    val source: String = "device",
) {
    var note: String = ""

    /**
     * copy() creates a new object and carries over everything not named.
     * This is the standard way of moving state forward without touching
     * the existing object.
     *
     * Visible as a side note: copy() does NOT carry `note` along, because
     * it is not in the primary constructor.
     */
    fun withUtilisation(newValue: Percent): Reading = copy(utilisation = newValue)

    /**
     * The toString() implementations of both value classes are picked up by
     * the string template automatically - which is why neither `.value` nor
     * any formatting appears here.
     */
    fun label(): String {
        val base = "$sensor: $utilisation (source: $source)"
        return if (utilisation.isCritical) "$base [critical]" else base
    }
}

fun main() {
    val reading = Reading(SensorId("cam-04"), Percent(42))
    println(reading.label())
    println(reading.withUtilisation(Percent(95)).label())
}
