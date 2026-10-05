package demo

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demo 4 – Coroutine context and dispatchers.
 *
 * The context is a set of elements attached to a coroutine: its Job, its
 * dispatcher, optionally a name and an exception handler. Children inherit
 * it from their parent.
 *
 * The dispatcher decides **which thread** the coroutine runs on:
 *
 *   Dispatchers.Default    CPU-bound work. Pool size = number of cores.
 *   Dispatchers.IO         blocking I/O. Large, elastic pool (64+ threads).
 *   Dispatchers.Unconfined starts on the calling thread, resumes wherever
 *                          the suspending call resumed. Rarely what you want.
 *   Dispatchers.Main       UI thread – only on Android/JavaFX/Swing.
 *
 * Tip for running this in the course: add the VM option
 * `-Dkotlinx.coroutines.debug` and the thread names also carry the
 * coroutine name and id.
 */

private fun here(label: String) {
    println("  %-22s %s".format(label, Thread.currentThread().name))
}

fun main() = runBlocking {
    whichThread()
    println()
    switchingContext()
    println()
    naming()
    println()
    whyIoIsDifferent()
}

// ------------------------------------------------------------------ 1
private suspend fun whichThread() = coroutineScope {
    println("— which thread runs what —")

    here("runBlocking")

    launch { here("launch (inherited)") }
    launch(Dispatchers.Default) { here("Dispatchers.Default") }
    launch(Dispatchers.IO) { here("Dispatchers.IO") }

    // Unconfined starts on the thread of the caller …
    launch(Dispatchers.Unconfined) {
        here("Unconfined before")
        delay(10.milliseconds)
        // … and continues on whatever thread resumed the delay.
        here("Unconfined after")
    }
}

// ------------------------------------------------------------------ 2
/**
 * `withContext` switches the dispatcher for one block and switches back
 * afterwards. It is a suspend function, so it does not start a new
 * coroutine – it moves the current one.
 *
 * This is the idiomatic way to keep blocking work off the wrong pool.
 */
private suspend fun switchingContext() = coroutineScope {
    println("— withContext —")

    here("before")

    val result = withContext(Dispatchers.IO) {
        here("inside IO")
        readConfigurationBlocking()
    }

    here("after")
    println("  result: $result")
}

/**
 * Deliberately blocking – it stands in for a legacy library, a JDBC
 * driver or a file read. Such code belongs on Dispatchers.IO.
 */
private fun readConfigurationBlocking(): String {
    Thread.sleep(200)
    return "port=9100"
}

// ------------------------------------------------------------------ 3
private suspend fun naming() = coroutineScope {
    println("— CoroutineName —")

    // Context elements are combined with `+`.
    launch(Dispatchers.Default + CoroutineName("poller")) {
        println("  name from the context: ${coroutineContext[CoroutineName]?.name}")
        here("named coroutine")
    }
}

// ------------------------------------------------------------------ 4
/**
 * Why the distinction between Default and IO is not cosmetic.
 *
 * Default has as many threads as there are CPU cores. If blocking calls
 * occupy those threads, everything else starves – including work that
 * would have plenty of CPU available.
 */
private suspend fun whyIoIsDifferent() = coroutineScope {
    println("— Default vs. IO for blocking work —")

    val cores = Runtime.getRuntime().availableProcessors()
    println("  cores available: $cores")

    val onDefault = measureTimeMillis {
        coroutineScope {
            repeat(cores * 2) {
                launch(Dispatchers.Default) { Thread.sleep(200) }
            }
        }
    }

    val onIo = measureTimeMillis {
        coroutineScope {
            repeat(cores * 2) {
                launch(Dispatchers.IO) { Thread.sleep(200) }
            }
        }
    }

    println("  ${cores * 2} blocking tasks on Default: $onDefault ms")
    println("  ${cores * 2} blocking tasks on IO:      $onIo ms")
    println(
        """
          Default has to run them in waves because its pool is as small as
          the core count. IO grows its pool and runs them at once.

          The rule: CPU work -> Default, blocking calls -> IO.
          And if it suspends instead of blocking, the dispatcher hardly matters.
        """.trimIndent()
    )
}
