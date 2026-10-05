package demo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demo 3 – Coroutine scope.
 *
 * Every coroutine belongs to a scope. The scope answers one question:
 * **who waits for this coroutine, and who cancels it?**
 *
 * A coroutine without a scope cannot even be started. Ownership is not
 * something to keep in mind - it is built into the type system.
 */

fun main() = runBlocking {
    coroutineScopeBuilder()
    println()
    ownScope()
    println()
    globalScopeProblem()
}

// ------------------------------------------------------------------ 1
/**
 * `coroutineScope { }` creates a scope and only returns once every child
 * inside it has finished.
 *
 * That guarantee is the whole idea: after the closing brace, nothing from
 * inside is still running. No leaked background work.
 */
private suspend fun coroutineScopeBuilder() {
    println("— coroutineScope —")

    coroutineScope {
        launch {
            delay(300.milliseconds)
            println("  child A done")
        }
        launch {
            delay(100.milliseconds)
            println("  child B done")
        }
        println("  block body reached its end – but the scope still waits")
    }

    println("  after coroutineScope: everything really is finished")
}

// ------------------------------------------------------------------ 2
/**
 * A scope you own yourself – the pattern for a component with a lifecycle
 * (a service, a connection, a screen).
 *
 * Two things matter:
 *   - the scope holds a Job, which gives you one handle for all children
 *   - whoever creates the scope is responsible for cancelling it
 */
private class DeviceMonitor {

    // SupervisorJob: a failing child does not take down its siblings.
    // See demo 5 for the difference to a plain Job.
    private val scope = CoroutineScope(SupervisorJob())

    fun start(deviceId: String) {
        scope.launch {
            var tick = 0
            while (true) {
                delay(100.milliseconds)
                tick++
                println("  [$deviceId] poll $tick")
            }
        }
    }

    /**
     * Without this call the coroutines above would run forever.
     * Cancelling the scope cancels every coroutine started in it.
     */
    fun stop() {
        scope.cancel()
        println("  monitor stopped")
    }
}

private suspend fun ownScope() {
    println("— your own scope —")

    val monitor = DeviceMonitor()
    monitor.start("cam-04")
    monitor.start("rtr-01")

    delay(350.milliseconds)
    monitor.stop()

    // Give the cancellation a moment to become visible in the output.
    delay(200.milliseconds)
    println("  nothing polls any more")
}

// ------------------------------------------------------------------ 3
/**
 * `GlobalScope` exists, and it is almost always the wrong answer.
 *
 * A coroutine in GlobalScope lives as long as the process. Nobody waits
 * for it, nobody cancels it, and an exception inside it disappears into
 * the default handler. It is the direct equivalent of a detached thread.
 *
 * It is marked `@DelicateCoroutinesApi` precisely so that using it is a
 * conscious decision.
 */
@OptIn(DelicateCoroutinesApi::class)
private suspend fun globalScopeProblem() {
    println("— GlobalScope (anti-pattern) —")

    GlobalScope.launch {
        delay(50.milliseconds)
        println("  still running – nobody is waiting for me")
    }

    println("  the function returns immediately, the coroutine keeps going")
    delay(100.milliseconds)   // only so we can see it at all

    println(
        """
          Rule of thumb: if you cannot name who cancels a coroutine,
          it is in the wrong scope. Use coroutineScope for a block, or
          a scope owned by a component with a lifecycle.
        """.trimIndent()
    )
}

/** Unused here, but worth mentioning: a scope is just a Job plus a context. */
private fun scopeIsJobPlusContext(): CoroutineScope = CoroutineScope(Job())
