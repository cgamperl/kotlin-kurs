package demo

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Demo 2 – Cancellation.
 *
 * The one sentence to remember: **cancellation is cooperative.**
 *
 * Cancelling a coroutine only sets a flag. Nothing is forcibly stopped -
 * there is no `Thread.stop()`, and that is deliberate: killing a thread
 * mid-operation leaves locks held and data half-written.
 *
 * A coroutine notices its cancellation in exactly two ways:
 *   - it calls a suspending function from kotlinx.coroutines (they all
 *     check), or
 *   - it checks for itself (isActive / ensureActive / yield).
 *
 * Code that does neither cannot be cancelled.
 */

fun main() = runBlocking {
    theUncancellableLoop()
    println()
    makingItCooperative()
    println()
    cleanupOnCancellation()
    println()
    timeouts()
}

// ------------------------------------------------------------------ 1
/**
 * A pure computation loop never suspends, so it never notices that it
 * has been cancelled. This is a frequent source of errors in practice.
 */
private suspend fun theUncancellableLoop() = coroutineScope {
    println("— a loop that ignores cancellation —")

    // Dispatchers.Default matters here: on the single-threaded event loop
    // of runBlocking this busy loop would block the very coroutine that is
    // supposed to cancel it, and nothing would happen at all.
    val job = launch(Dispatchers.Default) {
        var i = 0
        val start = System.currentTimeMillis()

        // No suspension point anywhere in here.
        while (System.currentTimeMillis() - start < 400) {
            i++
        }
        println("  ran to completion despite being cancelled, i = $i")
    }

    delay(50.milliseconds)
    job.cancelAndJoin()
    println("  cancelAndJoin returned - but only after the loop was done")
}

// ------------------------------------------------------------------ 2
private suspend fun makingItCooperative() = coroutineScope {
    println("— three ways to cooperate —")

    // a) isActive - check and leave on your own terms
    val a = launch(Dispatchers.Default) {
        var i = 0
        while (isActive) i++
        println("  isActive: stopped voluntarily at i = $i")
    }
    delay(50.milliseconds)
    a.cancelAndJoin()

    // b) ensureActive - throws CancellationException on your behalf
    val b = launch(Dispatchers.Default) {
        var i = 0
        while (true) {
            i++
            if (i % 100_000 == 0) coroutineContext.ensureActive()
        }
    }
    delay(50.milliseconds)
    b.cancelAndJoin()
    println("  ensureActive: threw CancellationException, job cancelled = ${b.isCancelled}")

    // c) yield - checks and additionally gives other coroutines a turn
    val c = launch(Dispatchers.Default) {
        var i = 0
        while (true) {
            i++
            if (i % 100_000 == 0) yield()
        }
    }
    delay(50.milliseconds)
    c.cancelAndJoin()
    println("  yield: same effect, plus it lets others run")
}

// ------------------------------------------------------------------ 3
/**
 * Cleanup belongs in `finally`. The catch: inside a cancelled coroutine
 * every suspending call fails immediately - so a `finally` block that
 * needs to suspend has to be wrapped in `NonCancellable`.
 */
private suspend fun cleanupOnCancellation() = coroutineScope {
    println("— cleanup —")

    val job = launch {
        try {
            println("  connection opened")
            delay(5.seconds)
        } finally {
            println("  finally reached")

            // Without NonCancellable this delay would throw immediately
            // and the message below would never appear.
            withContext(NonCancellable) {
                delay(50.milliseconds)
                println("  connection closed properly")
            }
        }
    }

    delay(100.milliseconds)
    job.cancelAndJoin()
}

// ------------------------------------------------------------------ 4
private suspend fun timeouts() = coroutineScope {
    println("— timeouts —")

    // withTimeoutOrNull: null instead of an exception. Usually what you want.
    val result = withTimeoutOrNull(200.milliseconds) {
        delay(1.seconds)
        "never gets here"
    }
    println("  withTimeoutOrNull: $result")

    val inTime = withTimeoutOrNull(500.milliseconds) {
        delay(100.milliseconds)
        "made it"
    }
    println("  withTimeoutOrNull, fast enough: $inTime")

    // withTimeout throws TimeoutCancellationException, a subclass of
    // CancellationException.
    try {
        withTimeout(200.milliseconds) {
            delay(1.seconds)
        }
    } catch (e: CancellationException) {
        println("  withTimeout threw: ${e::class.simpleName}")
    }

    println(
        """
          Because the timeout exception is a CancellationException, a
          surrounding catch (e: Exception) would swallow it - and the
          coroutine would carry on as if nothing had happened.
        """.trimIndent()
    )
}
