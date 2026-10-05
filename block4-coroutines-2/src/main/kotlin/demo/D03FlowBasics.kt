package demo

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demo 3 - Flow basics.
 *
 * A `suspend` function returns ONE value, eventually.
 * A `Flow` returns MANY values, over time.
 *
 * The mental model that carries furthest: **Flow is a Sequence that is
 * allowed to suspend.** Everything you learned about sequences in block 2
 * applies - lazy, only runs when someone collects, operators build a
 * pipeline rather than computing anything.
 */

fun main() = runBlocking {
    coldByDefault()
    println()
    buildingFlows()
    println()
    terminalOperators()
    println()
    hotFlows()
}

// ------------------------------------------------------------------ 1
/**
 * "Cold" means: the code inside `flow { }` runs once per collector, and
 * not at all without one.
 */
private suspend fun coldByDefault() {
    println("- cold -")

    val readings: Flow<Double> = flow {
        println("  flow body starts")
        repeat(3) { i ->
            delay(50.milliseconds)
            emit(20.0 + i)          // emit is a suspend function
        }
        println("  flow body ends")
    }

    println("  flow created - nothing has run yet")

    println("  first collector:")
    readings.collect { println("    got $it") }

    println("  second collector - the body runs AGAIN:")
    readings.collect { println("    got $it") }
}

// ------------------------------------------------------------------ 2
private suspend fun buildingFlows() {
    println("- ways to build a flow -")

    // From fixed values
    println("  flowOf: ${flowOf(1, 2, 3).toList()}")

    // From a collection
    println("  asFlow: ${listOf("cam-04", "rtr-01").asFlow().toList()}")

    // The general case: the flow builder. This is where a flow earns its
    // keep - it may suspend between emissions, which a Sequence cannot.
    val polled = flow {
        repeat(3) { i ->
            delay(30.milliseconds)               // a Sequence could not do this
            emit("poll $i")
        }
    }
    println("  flow { }: ${polled.toList()}")
}

// ------------------------------------------------------------------ 3
/**
 * Terminal operators are the ones that actually start the flow.
 * Everything else only describes what should happen.
 */
private suspend fun terminalOperators() {
    println("- terminal operators -")

    val temperatures = flow {
        repeat(5) { i ->
            delay(20.milliseconds)
            emit(20.0 + i * 2)
        }
    }

    println("  toList: ${temperatures.toList()}")
    println("  first:  ${temperatures.first()}")
    println("  take(2) + toList: ${temperatures.take(2).toList()}")

    // collect with a lambda is the most common one.
    print("  collect: ")
    temperatures.take(3).collect { print("$it ") }
    println()
}

// ------------------------------------------------------------------ 4
/**
 * Hot flows: they exist independently of collectors.
 *
 *   StateFlow  always holds exactly one current value. New collectors
 *              immediately receive that value. Think "observable variable".
 *   SharedFlow has no current value, it broadcasts events. Collectors
 *              only receive what is emitted after they subscribe.
 *
 * The rule of thumb: state -> StateFlow, events -> SharedFlow.
 * Block 4 only introduces them; they matter most in UI work.
 */
private suspend fun hotFlows() = coroutineScope {
    println("- hot flows -")

    // StateFlow
    val deviceState = MutableStateFlow("offline")
    println("  StateFlow current value: ${deviceState.value}")

    val watcher = launch {
        deviceState.collect { println("    state -> $it") }
    }

    delay(20.milliseconds)
    deviceState.value = "connecting"
    delay(20.milliseconds)
    deviceState.value = "online"
    delay(20.milliseconds)
    watcher.cancel()

    // SharedFlow
    println("  SharedFlow:")
    val alarms = MutableSharedFlow<String>()

    val listener = launch {
        alarms.collect { println("    alarm: $it") }
    }
    delay(20.milliseconds)                       // give the collector time to subscribe

    alarms.emit("temperature high")
    alarms.emit("link lost")
    delay(20.milliseconds)
    listener.cancel()

    println(
        """
          Note the difference: a hot flow is never "finished" and its
          collector never returns on its own. Someone has to cancel it -
          which is why hot flows and structured concurrency belong together.
        """.trimIndent()
    )
}
