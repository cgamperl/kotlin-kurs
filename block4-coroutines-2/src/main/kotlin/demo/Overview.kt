package demo

fun main() {
    println(
        """
        Block 4 – Coroutines II & practice
        ==================================
        Runnable demos (src/main/kotlin/demo):
          D01ExceptionHandlingKt   launch vs. async, handlers, supervisorScope
          D02CancellationKt        cooperative cancellation, NonCancellable, timeouts
          D03FlowBasicsKt          cold vs. hot, flow builders, terminal operators
          D04FlowOperatorsKt       map/filter/transform, debounce, flowOn, buffer, catch, zip/combine

        Test demos (src/test/kotlin/demo) - run them, do not start them:
          D05UnitTestingSpec       Kotest styles, MockK, data driven tests, fakes
          D06CoroutineTestingSpec  runTest, virtual time, test dispatchers, setMain

          ./gradlew :block4-coroutines-2:demoTest

        Run a single demo:
          ./gradlew :block4-coroutines-2:run -PmainClass=demo.D03FlowBasicsKt
        """.trimIndent()
    )
}
