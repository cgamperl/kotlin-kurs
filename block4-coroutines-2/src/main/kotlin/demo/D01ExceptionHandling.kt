package demo

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Demo 1 - Exception handling in coroutines.
 *
 * The rule that explains almost everything:
 *
 *   `launch` treats an exception as a FAILURE - it propagates up the job
 *           hierarchy immediately and cancels the siblings.
 *   `async` treats an exception as a RESULT - it is stored in the Deferred
 *           and rethrown when you call `await()`.
 */

fun main() = runBlocking {
    tryCatchInsideTheCoroutine()
    println()
    asyncStoresTheException()
    println()
    launchPropagatesImmediately()
    println()
    exceptionHandler()
    println()
    supervisorScopeIsolates()
    println()
    cancellationIsNotAFailure()
}

// ------------------------------------------------------------------ 1
/**
 * The simplest case, and the one to reach for first: catch it where it
 * happens. A try/catch inside a coroutine works exactly as expected.
 */
private suspend fun tryCatchInsideTheCoroutine() = coroutineScope {
    println("- try/catch inside -")

    launch {
        try {
            failingCall()
        } catch (e: IllegalStateException) {
            println("  caught inside the coroutine: ${e.message}")
        }
    }
}

private suspend fun failingCall(): Nothing {
    delay(50.milliseconds)
    throw IllegalStateException("device not reachable")
}

// ------------------------------------------------------------------ 2
/**
 * `async` does not throw at the point of failure. The exception waits in
 * the Deferred until somebody awaits it.
 */
private suspend fun asyncStoresTheException() {
    println("- async: the exception waits for await() -")

    supervisorScope {
        val deferred = async { failingCall() }

        println("  async has returned, nothing has thrown yet")
        delay(100.milliseconds)
        println("  the call has failed by now - still nothing thrown here")

        try {
            deferred.await()
        } catch (e: IllegalStateException) {
            println("  await() threw: ${e.message}")
        }
    }
}

// ------------------------------------------------------------------ 3
/**
 * `launch` is different: the exception travels up immediately, and the
 * enclosing `coroutineScope` rethrows it. That is why the try/catch
 * around the scope works, while a try/catch around `launch` alone would
 * not - `launch` itself returns long before the failure happens.
 */
private suspend fun launchPropagatesImmediately() {
    println("- launch: propagates immediately -")

    try {
        coroutineScope {
            launch { failingCall() }

            launch {
                repeat(10) {
                    delay(30.milliseconds)
                    println("  sibling step $it")
                }
            }
        }
    } catch (e: IllegalStateException) {
        println("  caught at the scope: ${e.message}")
    }

    println("  note the sibling never reached step 9 - it was cancelled")
}

// ------------------------------------------------------------------ 4
/**
 * A `CoroutineExceptionHandler` is the last resort for coroutines that
 * nobody awaits - the equivalent of an uncaught exception handler.
 *
 * Two things surprise people:
 *   - it only works for `launch`, never for `async` (there the exception
 *     belongs to the Deferred)
 *   - it only works on a ROOT coroutine, not on a nested child `launch`
 */
private suspend fun exceptionHandler() {
    println("- CoroutineExceptionHandler -")

    val handler = CoroutineExceptionHandler { context, cause ->
        println("  handler: ${cause.message}")
    }

    val scope = CoroutineScope(SupervisorJob() + handler)

    scope.launch { failingCall() }
    delay(150.milliseconds)

    scope.cancel()
}

// ------------------------------------------------------------------ 5
/**
 * `supervisorScope` is the block-level counterpart to `SupervisorJob`:
 * children fail independently of each other.
 *
 * Use it when the children are genuinely unrelated. Use `coroutineScope`
 * when they are parts of one computation - there, cancelling the rest on
 * failure is the right behaviour.
 */
private suspend fun supervisorScopeIsolates() {
    println("- supervisorScope -")

    // Inside supervisorScope each child is a ROOT coroutine as far as
    // exceptions are concerned. That is exactly why a handler works here
    // when attached to the child itself - and why, without one, the
    // exception would reach the default handler of the thread and take
    // the whole program down.
    val handler = CoroutineExceptionHandler { _, cause ->
        println("  handler: ${cause.message}")
    }

    supervisorScope {
        launch(handler) {
            delay(40.milliseconds)
            throw IllegalStateException("monitor A failed")
        }.invokeOnCompletion { cause -> println("  A ended: ${cause?.message}") }

        launch {
            repeat(4) {
                delay(30.milliseconds)
                println("  B step $it")
            }
        }
    }

    println("  B finished although A failed")
}

// ------------------------------------------------------------------ 6
/**
 * `CancellationException` is special: it is how cancellation is
 * implemented, and it is NOT treated as a failure.
 *
 * The practical consequence is a trap: `catch (e: Exception)` swallows
 * cancellation too, and a coroutine that swallows its cancellation
 * refuses to stop. Always rethrow it - or catch the specific type you
 * actually expect.
 */
private suspend fun cancellationIsNotAFailure() = coroutineScope {
    println("- CancellationException -")

    val job = launch {
        try {
            delay(5.seconds)
        } catch (e: kotlinx.coroutines.CancellationException) {
            println("  cancelled - rethrowing, as one should")
            throw e
        }
    }

    delay(50.milliseconds)
    job.cancelAndJoin()
    println("  job cancelled: ${job.isCancelled}")

    println(
        """
          Rule: never write catch (e: Exception) around suspending code
          without rethrowing CancellationException. Catch the specific
          exception you expect instead.
        """.trimIndent()
    )
}
