package block2

/**
 * Solution for exercise 2 - sealed classes.
 */

data class Measurement(val deviceId: String, val value: Double)

sealed interface QueryState {
    data object Loading : QueryState
    data class Success(val measurements: List<Measurement>) : QueryState
    data class Failure(val message: String) : QueryState
    data class TimedOut(val afterSeconds: Int) : QueryState
}

/**
 * 2a - the exhaustive `when`.
 *
 * No `else`. For the `data object` a plain comparison with the object
 * itself is enough; the data classes need `is`. After the `is` the smart
 * cast kicks in: `state.measurements` is reachable without a cast.
 *
 * The nested `if` in the success branch shows as a side note that a when
 * branch may be any expression.
 */
fun displayText(state: QueryState): String = when (state) {
    QueryState.Loading ->
        "query running …"

    is QueryState.Success ->
        if (state.measurements.isEmpty()) "no measurements received"
        else "${state.measurements.size} measurements received"

    is QueryState.Failure ->
        "error: ${state.message}"

    is QueryState.TimedOut ->
        "timed out after ${state.afterSeconds} s"
}

/**
 * 2b - a single type test is enough here.
 *
 * `(state as? QueryState.Success)?.measurements` would be the short form -
 * both are fine. The `if` variant usually reads more clearly for people
 * coming from other languages.
 */
fun measurementsOrNull(state: QueryState): List<Measurement>? =
    if (state is QueryState.Success) state.measurements else null

/**
 * 2c - deliberately NOT written as `state != Loading`.
 *
 * With the exhaustive `when`, adding another variant forces a decision
 * here. With the inequality comparison the new variant would silently
 * count as final.
 */
fun isFinal(state: QueryState): Boolean = when (state) {
    QueryState.Loading -> false
    is QueryState.Success -> true
    is QueryState.Failure -> true
    is QueryState.TimedOut -> true
}

/**
 * 2d - conditions inside the branches.
 *
 * After the `is`, `state` is already narrowed to the variant, so
 * `afterSeconds` and `message` are directly accessible.
 */
fun isWorthRetrying(state: QueryState): Boolean = when (state) {
    QueryState.Loading -> false
    is QueryState.Success -> false
    is QueryState.Failure -> "temporary" in state.message
    is QueryState.TimedOut -> state.afterSeconds < 60
}

fun main() {
    val states = listOf(
        QueryState.Loading,
        QueryState.Success(listOf(Measurement("sen-12", 21.4))),
        QueryState.Success(emptyList()),
        QueryState.Failure("temporary outage"),
        QueryState.TimedOut(30),
    )

    states.forEach {
        println("${displayText(it)}  (final: ${isFinal(it)}, retry: ${isWorthRetrying(it)})")
    }
}
