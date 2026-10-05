package demo

/**
 * Demo 8 – Scope functions: let, run, with, apply, also.
 *
 * At heart all five do the same thing: they run a block on an object.
 * They differ in exactly two answers:
 *
 *   1. What is the object called inside the block?  -> `it` (parameter) or `this` (receiver)
 *   2. What comes out?                              -> the object itself or the block's result
 *
 *   ┌─────────┬────────────┬──────────────────────┬──────────────────────────┐
 *   │         │ object as  │ returns              │ typical use              │
 *   ├─────────┼────────────┼──────────────────────┼──────────────────────────┤
 *   │ let     │ it         │ block result         │ null handling, transform │
 *   │ run     │ this       │ block result         │ computation on an object │
 *   │ with    │ this       │ block result         │ several accesses in a row│
 *   │ apply   │ this       │ the object           │ configuring              │
 *   │ also    │ it         │ the object           │ side effect in a chain   │
 *   └─────────┴────────────┴──────────────────────┴──────────────────────────┘
 *
 * Mnemonic: if the name ends in "ply"/"lso" (apply/also), the object comes
 * back - those two are meant for chains. The other three return the result
 * of the block.
 */

class ConnectionSetup {
    var target: String = ""
    var port: Int = 9000
    var timeoutSeconds: Int = 30
    var tls: Boolean = false

    fun describe(): String =
        "${if (tls) "tls" else "tcp"}://$target:$port (timeout ${timeoutSeconds}s)"
}

fun main() {
    letExample()
    println()
    runExample()
    println()
    withExample()
    println()
    applyExample()
    println()
    alsoExample()
    println()
    whichOne()
}

// ------------------------------------------------------------------ let
private fun letExample() {
    println("— let —")

    // The most common use: run the block only when not null.
    val found: Device? = sampleDevices.firstOrNull { it.id == DeviceId("cam-04") }
    found?.let { println("found: ${it.name}") }

    // Second use: transform a value and give it a name on the way.
    val message = found?.let { device ->
        "${device.name} is running at ${device.utilisation} %"
    } ?: "device not found"
    println(message)

    // let also breaks up long chains without introducing a variable:
    val result = sampleDevices
        .filter { it.utilisation > 50 }
        .let { filtered -> "${filtered.size} of ${sampleDevices.size}" }
    println(result)
}

// ------------------------------------------------------------------ run
private fun runExample() {
    println("— run —")

    val device = sampleDevices[1]

    // `this` instead of `it`: several accesses without a prefix, and the
    // block's value is the result.
    val report = device.run {
        "$name ($type) sits in $location and runs at $utilisation %"
    }
    println(report)

    // run without a receiver: a block that yields a value - useful for
    // wrapping a computation together with its helper variables.
    val threshold = run {
        val base = sampleDevices.map { it.utilisation }.average()
        (base * 1.2).toInt()
    }
    println("dynamic threshold: $threshold")
}

// ----------------------------------------------------------------- with
private fun withExample() {
    println("— with —")

    val device = sampleDevices[2]

    // Semantically the same as run, only written differently: the object is
    // an argument rather than a receiver. Use it when the object is already
    // at hand and definitely not null.
    with(device) {
        println("id:          $id")
        println("name:        $name")
        println("location:    $location")
        println("utilisation: $utilisation %")
    }
}

// ---------------------------------------------------------------- apply
private fun applyExample() {
    println("— apply —")

    // The common case: configure an object and receive it back.
    // Without apply you would need a variable and four assignments below it.
    val connection = ConnectionSetup().apply {
        target = "control-1"
        port = 9100
        tls = true
        timeoutSeconds = 5
    }

    println(connection.describe())

    // Because apply returns the object, it can be passed on directly:
    println(ConnectionSetup().apply { target = "control-2" }.describe())
}

// ----------------------------------------------------------------- also
private fun alsoExample() {
    println("— also —")

    // A side effect in the middle of a chain, without breaking it.
    // Typically logging or an intermediate check.
    val critical = sampleDevices
        .also { println("incoming: ${it.size} devices") }
        .filter { it.utilisation >= 90 }
        .also { println("of those critical: ${it.size}") }
        .map { it.name }

    println(critical)
}

// --------------------------------------------------------------- choosing
private fun whichOne() {
    println("— how to choose —")
    println(
        """
        Do I need the object afterwards?
          yes -> apply (configure, this) or also (observe, it)
          no  -> let (it) / run (this) / with (this)

        Is the value nullable?
          yes -> let behind a ?.
          no  -> run or with

        Do the names inside the block shadow something?
          yes -> let with a named parameter, not run/with
        """.trimIndent()
    )
}
