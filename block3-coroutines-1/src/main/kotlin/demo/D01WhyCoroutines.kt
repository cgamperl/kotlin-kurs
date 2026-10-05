package demo

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.seconds

/**
 * Demo 1 - Why coroutines?
 *
 * A JVM platform thread reserves around 1 MB of stack and switching
 * between threads goes through the kernel. A coroutine is an object on
 * the heap costing a few dozen bytes, and switching between coroutines
 * is an ordinary method call.
 *
 * This demo makes that difference measurable.
 */

/** Both variants get the same number of tasks, so the comparison is fair. */
private const val FAIR_COUNT = 10_000

/** Only the coroutine version is asked to do this. See [scaleUp]. */
private const val LARGE_COUNT = 200_000

fun main() {
    println("Part 1 - same workload, $FAIR_COUNT tasks each waiting one second\n")

    val coroutineMillis = withCoroutines(FAIR_COUNT)
    val threadMillis = withThreads(FAIR_COUNT)

    println(
        """

        Both variants did exactly the same thing: wait one second, $FAIR_COUNT times.

          coroutines: $coroutineMillis ms
          threads:    $threadMillis ms

        The coroutine version barely exceeds that one second, because a
        suspended coroutine occupies no thread at all.

        Note that the difference in TIME is still moderate here - threads
        are not dramatically slow at this scale. Their real cost is memory:
        $FAIR_COUNT stacks of roughly 1 MB each. That is what part 2 is about.
        """.trimIndent()
    )

    scaleUp()

    println(
        """

        The one sentence the rest of this block builds on:

            delay() SUSPENDS the coroutine, Thread.sleep() BLOCKS the thread.

        Everything else - dispatchers, scopes, cancellation - follows from
        that distinction.
        """.trimIndent()
    )
}

private fun withCoroutines(count: Int): Long {
    val done = AtomicInteger()

    return measureTimeMillis {
        runBlocking {
            repeat(count) {
                // `launch` starts a coroutine, not a thread.
                launch {
                    delay(1.seconds)                 // suspends, does not block
                    done.incrementAndGet()
                }
            }
            // runBlocking waits for all children on its own - that is
            // structured concurrency, see demo 5.
        }
    }.also { check(done.get() == count) }
}

private fun withThreads(count: Int): Long {
    val done = AtomicInteger()

    return measureTimeMillis {
        val threads = List(count) {
            Thread {
                Thread.sleep(1000)              // blocks an entire OS thread
                done.incrementAndGet()
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
    }.also { check(done.get() == count) }
}

/**
 * Part 2 is deliberately asymmetric: only the coroutine version is asked
 * to handle [LARGE_COUNT] tasks.
 *
 * The thread version is not attempted on purpose: starting 200,000 platform
 * threads fails, typically with an OutOfMemoryError ("unable to create
 * native thread"). The limit is therefore not one of throughput but of
 * feasibility.
 */
private fun scaleUp() {
    println("\nPart 2 - $LARGE_COUNT coroutines (the thread version is not even attempted)\n")

    val millis = withCoroutines(LARGE_COUNT)
    println("coroutines: $LARGE_COUNT tasks in $millis ms")
}
