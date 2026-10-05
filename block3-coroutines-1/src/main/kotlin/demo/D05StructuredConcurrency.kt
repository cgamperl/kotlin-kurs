package demo

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demo 5 – Structured concurrency.
 *
 * Three rules. Everything else follows from them:
 *
 *   1. Every coroutine has a parent (the scope it was started in).
 *   2. A parent does not complete before all its children have completed.
 *   3. Cancelling a parent cancels all its children. A failing child
 *      cancels its parent – and through it, its siblings.
 *
 * The hierarchy as a diagram:
 *
 *        runBlocking (Job)
 *              |
 *        coroutineScope (Job)
 *          /        \
 *      child A    child B
 *                    |
 *                 grandchild
 *
 * Cancellation travels down the tree, failure travels up and then down again.
 */

fun main() = runBlocking {
    parentWaitsForChildren()
    println()
    cancellationTravelsDown()
    println()
    failureTravelsUp()
    println()
    supervisorJobIsolatesFailure()
}

// ------------------------------------------------------------------ 1
private suspend fun parentWaitsForChildren() = coroutineScope {
    println("— a parent waits —")

    launch {
        delay(100.milliseconds)
        println("  child A done")

        // A child may itself have children; the tree can be any depth.
        launch {
            delay(100.milliseconds)
            println("  grandchild done")
        }
    }

    launch {
        delay(50.milliseconds)
        println("  child B done")
    }

    println("  end of the block – but coroutineScope still waits")
}

// ------------------------------------------------------------------ 2
private suspend fun cancellationTravelsDown() = coroutineScope {
    println("— cancellation travels down —")

    val parent = launch {
        launch {
            repeat(100) { i ->
                delay(50.milliseconds)
                println("  child 1, step $i")
            }
        }
        launch {
            repeat(100) { i ->
                delay(50.milliseconds)
                println("  child 2, step $i")
            }
        }
    }

    delay(160.milliseconds)
    println("  cancelling the parent")
    parent.cancel()
    parent.join()

    println("  both children stopped, without either being cancelled directly")
}

// ------------------------------------------------------------------ 3
/**
 * A failing child cancels its parent – and the parent then cancels the
 * siblings. That is intentional: if part of a joint computation has
 * failed, the rest of it is usually pointless.
 */
private suspend fun failureTravelsUp() {
    println("— a failure cancels the siblings —")

    try {
        coroutineScope {
            launch {
                repeat(10) { i ->
                    delay(50.milliseconds)
                    println("  sibling still working, step $i")
                }
            }

            launch {
                delay(120.milliseconds)
                println("  the other child fails now")
                throw IllegalStateException("device not reachable")
            }
        }
    } catch (e: IllegalStateException) {
        println("  caught at the scope: ${e.message}")
    }

    println("  the sibling was cancelled as well – note it stopped before step 9")
}

// ------------------------------------------------------------------ 4
/**
 * `SupervisorJob` breaks rule 3 in one direction on purpose: a failing
 * child no longer cancels its parent, and therefore not its siblings
 * either. Cancellation from the parent downwards still works.
 *
 * Use it wherever the children are genuinely independent – several device
 * monitors, several subscriptions.
 */
private suspend fun supervisorJobIsolatesFailure() {
    println("— SupervisorJob —")

    // Without a handler the failure of monitor 1 would reach the default
    // handler of the thread and print a raw stack trace. Exception handling
    // is the topic of block 4 - here it only keeps the output readable.
    val handler = CoroutineExceptionHandler { _, cause ->
        println("  handler caught: ${cause.message}")
    }

    val scope = CoroutineScope(SupervisorJob() + handler)

    scope.launch {
        delay(60.milliseconds)
        throw IllegalStateException("monitor 1 crashed")
    }.invokeOnCompletion { cause ->
        println("  monitor 1 ended: ${cause?.message}")
    }

    val survivor = scope.launch {
        repeat(4) { i ->
            delay(50.milliseconds)
            println("  monitor 2 still running, step $i")
        }
    }

    survivor.join()
    println("  monitor 2 finished normally despite its sibling failing")

    scope.cancel()
}

/** Small helper for the whiteboard: show the current job and its children. */
private suspend fun printJobTree(label: String) {
    val job: Job = coroutineContext.job
    println("$label: job=$job active=${job.isActive} children=${job.children.count()}")
}
