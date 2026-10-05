package demo

fun main() {
    println(
        """
        Block 3 - Coroutines I
        ======================
        D01WhyCoroutinesKt         threads vs. coroutines, measured
        D02SuspendBuildersKt       suspend, runBlocking, launch, async/await
        D03CoroutineScopeKt        coroutineScope, custom scopes, GlobalScope
        D04ContextDispatchersKt    Default/IO/Unconfined, withContext, CoroutineName
        D05StructuredConcurrencyKt job hierarchy, cancellation, SupervisorJob

        Run a single demo:
          ./gradlew :block3-coroutines-1:run -PmainClass=demo.D02SuspendBuildersKt
        """.trimIndent()
    )
}
