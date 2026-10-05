package demo

/**
 * Demo 5 - Sealed classes.
 *
 * An enum says "one out of this fixed set of VALUES".
 * A sealed class says "one out of this fixed set of TYPES" - and each of
 * those types may carry its own data.
 *
 * That is exactly the difference that makes state modelling so much
 * better: the failure case carries its message, the success case carries
 * its data - and neither drags the other one's fields around.
 */

// ------------------------------------------------------------------ 1
/**
 * A loading state, the most common application of a sealed hierarchy.
 *
 * `sealed interface` rather than `sealed class` when the variants share no
 * data - then they can be `data object`s too.
 */
sealed interface LoadState {

    /**
     * `data object` (since Kotlin 1.9): a singleton with a sensible
     * toString(). For variants without their own data this is the right
     * choice - no new object is created on every access.
     */
    data object Loading : LoadState

    data class Success(val devices: List<Device>) : LoadState

    data class Failure(val message: String, val cause: Throwable? = null) : LoadState

    /** Variants with extra information are possible too. */
    data class PartiallyLoaded(val devices: List<Device>, val missingIds: List<DeviceId>) : LoadState
}

/**
 * The exhaustive `when`.
 *
 * There is NO `else`. If a fifth variant were added, this would be a
 * compile error - at every place that uses the `when`. That is the real
 * benefit: the compiler finds the spots you would forget when extending.
 */
fun displayText(state: LoadState): String = when (state) {
    LoadState.Loading ->
        "loading devices …"

    is LoadState.Success ->
        // Smart cast: from here on `state` is of type Success.
        "${state.devices.size} devices loaded"

    is LoadState.Failure ->
        "error: ${state.message}" + (state.cause?.let { " (${it.message})" } ?: "")

    is LoadState.PartiallyLoaded ->
        "${state.devices.size} loaded, ${state.missingIds.size} missing"
}

// ------------------------------------------------------------------ 2
/**
 * A generic result - the pattern you meet constantly in Kotlin projects.
 *
 * Compared to exceptions: the failure case is part of the return type and
 * therefore impossible for the caller to overlook.
 */
sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Failed(val reason: String) : Outcome<Nothing>
}

/**
 * `Nothing` as the type argument is what makes this work:
 * `Nothing` is a subtype of everything, so `Failed` is an `Outcome<T>` for
 * every T. That is why the failure case needs no type parameter.
 */
fun loadDevice(id: DeviceId): Outcome<Device> {
    val found = sampleDevices.firstOrNull { it.id == id }
    return if (found != null) Outcome.Ok(found)
    else Outcome.Failed("no device with the id $id")
}

fun main() {
    val states = listOf(
        LoadState.Loading,
        LoadState.Success(sampleDevices),
        LoadState.Failure("timeout", IllegalStateException("no network")),
        LoadState.PartiallyLoaded(sampleDevices.take(2), listOf(DeviceId("rtr-09"))),
    )

    for (state in states) {
        println(displayText(state))
    }

    // data object: same instance, readable toString()
    println()
    println("Loading === Loading: ${LoadState.Loading === LoadState.Loading}")
    println("toString: ${LoadState.Loading}")

    // ---- the generic outcome
    println()
    for (id in listOf(DeviceId("cam-04"), DeviceId("does-not-exist"))) {
        val text = when (val outcome = loadDevice(id)) {
            is Outcome.Ok -> "found: ${outcome.value.name}"
            is Outcome.Failed -> "failed: ${outcome.reason}"
        }
        println(text)
    }

    // A sealed hierarchy can also be queried without handling every case -
    // but then you need an `else` again, and that is exactly how you lose
    // the compiler check:
    val single: LoadState = LoadState.Loading
    if (single is LoadState.Success) {
        println(single.devices.size)
    }
}
