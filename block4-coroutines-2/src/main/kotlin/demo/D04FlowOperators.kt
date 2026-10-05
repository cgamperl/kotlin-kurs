package demo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demo 4 – Flow operators.
 *
 * Most of these will look familiar from the collections API in block 2 -
 * `map`, `filter`, `take` do the same thing. Two groups are genuinely new:
 *
 *   - operators about TIME (debounce, sample)
 *   - operators about CONTEXT and BACKPRESSURE (flowOn, buffer)
 *
 * Those two groups are the reason Flow exists at all; the rest is a
 * Sequence with suspension.
 */

/** A sensor that reports a value every 50 ms. */
private fun sensorReadings(count: Int = 6): Flow<Double> = flow {
    repeat(count) { i ->
        delay(50.milliseconds)
        emit(20.0 + i * 3)
    }
}

fun main() = runBlocking {
    familiarOnes()
    println()
    transformAndFold()
    println()
    aboutTime()
    println()
    aboutContext()
    println()
    errorHandling()
    println()
    combiningFlows()
}

// ------------------------------------------------------------------ 1
private suspend fun familiarOnes() {
    println("— the familiar ones —")

    val result = sensorReadings()
        .map { it * 1.8 + 32 }              // to Fahrenheit
        .filter { it > 80 }
        .take(3)
        .toList()

    println("  map/filter/take: ${result.map { "%.1f".format(it) }}")

    // onEach is the `also` of the flow world: a side effect that does not
    // change the stream. Ideal for logging.
    sensorReadings(3)
        .onEach { println("    raw: $it") }
        .map { it.toInt() }
        .collect { println("    rounded: $it") }
}

// ------------------------------------------------------------------ 2
private suspend fun transformAndFold() {
    println("— transform and running totals —")

    // transform is the general case: zero, one or many emissions per input.
    val flagged = sensorReadings(4)
        .transform { value ->
            emit("value $value")
            if (value > 24) emit("  -> WARNING at $value")
        }
        .toList()
    flagged.forEach { println("  $it") }

    // runningFold emits every intermediate result - a running average,
    // a running total, a state machine.
    val runningMax = sensorReadings(4)
        .runningFold(0.0) { max, value -> maxOf(max, value) }
        .toList()
    println("  running maximum: $runningMax")
}

// ------------------------------------------------------------------ 3
@OptIn(FlowPreview::class)
private suspend fun aboutTime() {
    println("— time —")

    // Worth knowing: debounce is still marked @FlowPreview, hence the
    // OptIn above. Most Flow operators are stable; the time-based ones
    // are the exception.

    // debounce: only emit once the source has been quiet for a while.
    // A typical application is a search field; here, a sensor that emits
    // in bursts.
    val chattering = flow {
        emit(1); delay(30.milliseconds)
        emit(2); delay(30.milliseconds)
        emit(3); delay(300.milliseconds)      // pause - now 3 gets through
        emit(4); delay(300.milliseconds)
    }

    val debounced = chattering.debounce(100.milliseconds).toList()
    println("  debounce(100 ms): $debounced   (only the values before a pause)")
}

// ------------------------------------------------------------------ 4
private suspend fun aboutContext() {
    println("— context and backpressure —")

    // flowOn changes the dispatcher of everything UPSTREAM of it.
    // The collector stays where it is. This is how you keep a slow
    // producer off the UI thread.
    flowOf(1, 2, 3)
        .map { it * 2 }
        .flowOn(Dispatchers.Default)         // affects the map above
        .collect { println("    collected $it on ${Thread.currentThread().name}") }

    // Without buffer, producer and collector alternate: 50 ms producing
    // plus 50 ms collecting, six times over.
    val unbuffered = measureTimeMillis {
        sensorReadings().collect { delay(50.milliseconds) }
    }

    // With buffer the producer runs ahead while the collector works.
    val buffered = measureTimeMillis {
        sensorReadings().buffer().collect { delay(50.milliseconds) }
    }

    println("  without buffer: $unbuffered ms")
    println("  with buffer:    $buffered ms")
}

// ------------------------------------------------------------------ 5
private suspend fun errorHandling() {
    println("— errors —")

    var attempt = 0
    val flaky = flow {
        attempt++
        emit("reading 1")
        if (attempt < 3) throw IllegalStateException("link lost (attempt $attempt)")
        emit("reading 2")
    }

    // retry re-subscribes to the flow. Note that what was already emitted
    // is emitted AGAIN - the flow starts over, it does not resume.
    val withRetry = flaky
        .retry(3) { cause ->
            println("    retrying after: ${cause.message}")
            true
        }
        .toList()
    println("  after retry: $withRetry")

    // catch only sees exceptions from UPSTREAM. An exception in the
    // collector is not caught here - a deliberate design decision.
    flow<Int> { throw IllegalStateException("sensor defective") }
        .catch { cause -> println("    caught: ${cause.message}") }
        .collect { println("    never reached") }
}

// ------------------------------------------------------------------ 6
private suspend fun combiningFlows() {
    println("— combining —")

    val temperature = flow {
        delay(30.milliseconds); emit(21.0)
        delay(60.milliseconds); emit(22.5)
    }
    val humidity = flow {
        delay(40.milliseconds); emit(45)
        delay(40.milliseconds); emit(48)
    }

    // zip waits for a partner on both sides - pairs are formed strictly.
    println("  zip: ${temperature.zip(humidity) { t, h -> "$t °C / $h %" }.toList()}")

    // combine emits on EVERY change of either side, using the latest of
    // the other. This is what you want for a dashboard.
    println("  combine: ${temperature.combine(humidity) { t, h -> "$t °C / $h %" }.toList()}")
}
