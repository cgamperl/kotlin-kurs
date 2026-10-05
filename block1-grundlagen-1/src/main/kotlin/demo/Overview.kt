package demo

/**
 * Entry point when the module is started without naming a demo.
 * Lists the demos of this block in the order of the agenda.
 */
fun main() {
    println(
        """
        Block 1 – Setup & language basics I
        ===================================
        D01ProjectSetupKt        project setup, top-level functions
        D02NullabilityKt         safe calls, Elvis, smart cast, !!
        D03TypeInferenceKt       type inference, and where to write types anyway
        D04ImmutabilityKt        val/var, List vs. MutableList, defensive copy
        D05DataClassesKt         data classes, copy, destructuring, value classes
        D06VisibilityKt          public/private/protected/internal
        D07DelegationKt          interface delegation, by lazy, observable
        D08IdiomsKt              default args, templates, when, ranges, takeIf
        D09ContextParametersKt   context parameters (stable since Kotlin 2.4)

        Run a single demo:
          ./gradlew :block1-grundlagen-1:run -PmainClass=demo.D05DataClassesKt

        In IntelliJ the green arrow next to main() is enough.
        """.trimIndent()
    )
}
