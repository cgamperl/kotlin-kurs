package demo

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demo 2 – Suspend functions and coroutine builders.
 *
 * Three builders cover almost everything:
 *
 *   runBlocking { }  bridges blocking and suspending code. Blocks the
 *                    current thread until the block is done. Use it in
 *                    main() and in tests – not in production code.
 *
 *   launch { }       fire and forget. Returns a Job, no result value.
 *
 *   async { }        starts a computation and returns a Deferred<T>;
 *                    `await()` yields the result.
 *
 * One thing to notice before it confuses anyone: `launch` and `async` are
 * extension functions **on CoroutineScope**. You cannot call them without
 * one. `runBlocking` and `coroutineScope` are what provide that scope here -
 * which is why every function below is wrapped in one.
 *
 * What a scope actually is, who waits for those coroutines and who cancels
 * them, is the subject of demo 3. For now it is enough to read
 * `coroutineScope { }` as "the place these coroutines belong to".
 */

/**
 * A `suspend` function may call other suspend functions and may pause.
 *
 * What the keyword really means: the compiler rewrites the function into
 * a state machine and adds a hidden `Continuation` parameter. That is why
 * a suspend function can only be called from a coroutine or from another
 * suspend function – the continuation has to come from somewhere.
 */
suspend fun fetchDeviceStatus(deviceId: String): String {
    delay(300.milliseconds)                                  // stands in for network I/O
    return "$deviceId: online"
}

suspend fun fetchUtilisation(deviceId: String): Int {
    delay(300.milliseconds)
    return deviceId.length * 7
}

fun main() = runBlocking {
    launchBuilder()
    println()
    sequentialVersusParallel()
    println()
    awaitingMany()
}

// ------------------------------------------------------------------ 1
// `coroutineScope` opens the scope that `launch` needs - see the note above.
private suspend fun launchBuilder() = coroutineScope {
    println("— launch —")

    // launch returns a Job. It is a handle, not a result.
    val job = launch {
        println("  child started")
        delay(200.milliseconds)
        println("  child finished")
    }

    println("  launch returned immediately, job is active: ${job.isActive}")

    job.join()                                   // wait for completion
    println("  after join, completed: ${job.isCompleted}")
}

// ------------------------------------------------------------------ 2
private suspend fun sequentialVersusParallel() = coroutineScope {
    println("— sequential vs. parallel —")

    // Plain suspend calls run one after the other. Two calls of 300 ms
    // each take 600 ms. This is not a flaw: it is what you want whenever
    // the second call depends on the first.
    val sequential = measureTimeMillis {
        val status = fetchDeviceStatus("cam-04")
        val utilisation = fetchUtilisation("cam-04")
        println("  $status, utilisation $utilisation")
    }
    println("  sequential: $sequential ms")

    // `async` starts both immediately; `await` collects the results.
    // Note that both async calls happen BEFORE the first await – that is
    // what makes it parallel.
    val parallel = measureTimeMillis {
        val status = async { fetchDeviceStatus("cam-04") }
        val utilisation = async { fetchUtilisation("cam-04") }
        println("  ${status.await()}, utilisation ${utilisation.await()}")
    }
    println("  parallel:   $parallel ms")

    // A common mistake: awaiting right away makes it sequential again.
    val wrong = measureTimeMillis {
        val status = async { fetchDeviceStatus("cam-04") }.await()
        val utilisation = async { fetchUtilisation("cam-04") }.await()
        println("  $status, utilisation $utilisation")
    }
    println("  async but awaited too early: $wrong ms  <- no gain at all")
}

// ------------------------------------------------------------------ 3
private suspend fun awaitingMany() = coroutineScope {
    println("— many at once —")

    val deviceIds = listOf("cam-04", "cam-09", "rtr-01", "rtr-02", "int-07", "sen-12")

    val millis = measureTimeMillis {
        // map + async gives one Deferred per device …
        val pending = deviceIds.map { id -> async { fetchDeviceStatus(id) } }

        // … and awaitAll collects them. Six calls of 300 ms in roughly 300 ms.
        val results = pending.awaitAll()
        results.forEach { println("  $it") }
    }

    println("  ${deviceIds.size} devices in $millis ms")
}
