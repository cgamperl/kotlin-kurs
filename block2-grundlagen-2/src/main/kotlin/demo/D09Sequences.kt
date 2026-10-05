package demo

/**
 * Demo 9 – Sequences vs. collections (lazy evaluation).
 *
 * The collection operations are **eager**: every step of a chain
 * immediately produces a complete new list. Three operations over a
 * million elements means three million-element lists.
 *
 * A `Sequence` works **element by element and on demand**: each element
 * travels through the whole chain before the next one starts - and only as
 * many elements as are actually needed at the end.
 */

fun main() {
    makeTheOrderVisible()
    println()
    shortCircuiting()
    println()
    infiniteSequences()
    println()
    measuring()
    println()
    whenIsItWorthIt()
}

// ------------------------------------------------------------------ 1
/**
 * The difference is easiest to see in the order of execution.
 */
private fun makeTheOrderVisible() {
    val numbers = listOf(1, 2, 3, 4)

    println("— list (eager) —")
    numbers
        .map { println("  map $it"); it * 2 }
        .filter { println("  filter $it"); it > 4 }
        .forEach { println("  result $it") }
    // Output: ALL maps first, then ALL filters, then all results.

    println()
    println("— sequence (on demand) —")
    numbers.asSequence()
        .map { println("  map $it"); it * 2 }
        .filter { println("  filter $it"); it > 4 }
        .forEach { println("  result $it") }
    // Output: map 1, filter 2, map 2, filter 4, … - each element goes
    // through the chain on its own.
}

// ------------------------------------------------------------------ 2
/**
 * The practical consequence: a sequence stops working as soon as it has
 * enough elements.
 */
private fun shortCircuiting() {
    println("— short circuiting —")

    val devices = sampleDevices

    var listChecked = 0
    val firstTwoFromList = devices
        .map { listChecked++; it.name }
        .filter { it.startsWith("C") || it.startsWith("R") }
        .take(2)
    println("list:     $firstTwoFromList (checked: $listChecked of ${devices.size})")

    var sequenceChecked = 0
    val firstTwoFromSequence = devices.asSequence()
        .map { sequenceChecked++; it.name }
        .filter { it.startsWith("C") || it.startsWith("R") }
        .take(2)
        .toList()
    println("sequence: $firstTwoFromSequence (checked: $sequenceChecked of ${devices.size})")

    // Note: `toList()` is the terminal operation that makes the sequence
    // run at all. Without it nothing happens.
}

// ------------------------------------------------------------------ 3
/**
 * Something lists fundamentally cannot do: infinite series.
 */
private fun infiniteSequences() {
    println("— infinite series —")

    val samplingPoints = generateSequence(0) { it + 5 }        // 0, 5, 10, 15, …
    println(samplingPoints.take(6).toList())

    // generateSequence ends as soon as the lambda returns null.
    val decaying = generateSequence(100.0) { previous ->
        val next = previous * 0.6
        if (next < 1.0) null else next
    }
    println(decaying.map { "%.1f".format(it) }.toList())

    // Useful for simulated measurement series:
    val simulated = generateSequence(20.0) { it + (-2..2).random() }
        .take(8)
        .map { "%.0f".format(it) }
        .toList()
    println("simulated temperature series: $simulated")
}

// ------------------------------------------------------------------ 4
private fun measuring() {
    println("— measuring —")

    val large = (1..2_000_000).toList()

    val (listResult, listMillis) = measure {
        large.map { it * 2 }.filter { it % 3 == 0 }.take(5)
    }
    println("list:     $listResult in $listMillis ms")

    val (sequenceResult, sequenceMillis) = measure {
        large.asSequence().map { it * 2 }.filter { it % 3 == 0 }.take(5).toList()
    }
    println("sequence: $sequenceResult in $sequenceMillis ms")

    // And now the counter-example - a small data set where everything is
    // needed anyway:
    val small = (1..20).toList()
    val (_, smallList) = measure { small.map { it * 2 }.filter { it % 3 == 0 } }
    val (_, smallSequence) = measure { small.asSequence().map { it * 2 }.filter { it % 3 == 0 }.toList() }
    println("small, fully consumed – list: $smallList ms, sequence: $smallSequence ms")
}

private inline fun <T> measure(block: () -> T): Pair<T, Double> {
    val start = System.nanoTime()
    val result = block()
    return result to (System.nanoTime() - start) / 1_000_000.0
}

// ------------------------------------------------------------------ 5
private fun whenIsItWorthIt() {
    println("— rules of thumb —")
    println(
        """
        Reach for a sequence when:
          - there are many elements AND several processing steps
          - only part of the result is needed (take, first, any)
          - the source is infinite or of unknown size
          - the elements are expensive to produce

        Stay with a list when:
          - there are few elements (below a few thousand)
          - there is only a single step
          - every element is needed anyway
          - the intermediate result is used more than once

        Sequences are not a free win: every element passes through several
        lambda calls that are not inlined. For small collections the list
        is regularly faster.
        """.trimIndent()
    )
}
