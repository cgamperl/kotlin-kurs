package exercise

/**
 * Exercise 2 - sealed classes: modelling states.
 *
 * A device query goes through several states. Model them so that the
 * compiler checks every evaluation for completeness.
 *
 * Verify with:
 *     ./gradlew :block2-grundlagen-2:exerciseTest --tests "exercise.E2*"
 */

data class Measurement(val deviceId: String, val value: Double)

/**
 * The state hierarchy is already declared.
 *
 * Note the choice of variants:
 *  - `Loading` has no data of its own -> `data object`
 *  - `Success` carries the payload
 *  - `Failure` carries the error message
 *  - `TimedOut` carries how long we waited
 *
 * Each variant carries exactly what belongs to IT - and nothing from the
 * others.
 */
sealed interface QueryState {

    data object Loading : QueryState

    data class Success(val measurements: List<Measurement>) : QueryState

    data class Failure(val message: String) : QueryState

    data class TimedOut(val afterSeconds: Int) : QueryState
}

/**
 * Exercise 2a
 *
 * Produces the display text for a state:
 *
 *     Loading                -> "query running …"
 *     Success (3 values)     -> "3 measurements received"
 *     Success (0 values)     -> "no measurements received"
 *     Failure("no network")  -> "error: no network"
 *     TimedOut(30)           -> "timed out after 30 s"
 *
 * Write the `when` WITHOUT an `else`. When a fifth variant is added later,
 * the compiler should complain right here.
 */
fun displayText(state: QueryState): String {
    TODO("2a: display text via an exhaustive when")
}

/**
 * Exercise 2b
 *
 * Returns the measurements when the state is `Success` - null otherwise.
 *
 * Useful: `when` with `is`, or a single `if (… is …)`.
 */
fun measurementsOrNull(state: QueryState): List<Measurement>? {
    TODO("2b: extract the measurements")
}

/**
 * Exercise 2c
 *
 * A final state is anything except `Loading` - that is, every state after
 * which nothing else happens.
 */
fun isFinal(state: QueryState): Boolean {
    TODO("2c: recognise a final state")
}

/**
 * Exercise 2d
 *
 * Decides whether another attempt is worthwhile:
 *
 *   - for `TimedOut`: yes, but only when we waited less than 60 seconds
 *     (waiting longer will not help after that)
 *   - for `Failure`: only when the message contains the word "temporary"
 *   - for `Loading` and `Success`: no
 *
 * Hint: a `when` branch may contain a condition - after the smart cast you
 * can access the variant's fields directly.
 */
fun isWorthRetrying(state: QueryState): Boolean {
    TODO("2d: decide about a retry")
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
