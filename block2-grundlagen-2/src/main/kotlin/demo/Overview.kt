package demo

fun main() {
    println(
        """
        Block 2 – Language basics II
        ============================
        D01GenericsKt          generics, in/out variance, where, reified
        D02EnumClassesKt       enums with properties, entries, exhaustive when
        D03OopKt               constructors, properties, final by default, interfaces
        D04ObjectsKt           object, companion object, factory, object expression
        D05SealedClassesKt     sealed interfaces, state modelling, Outcome<T>
        D06FunctionalKt        collections API, extension functions & properties
        D07LambdasKt           lambdas, trailing lambda, function references, inline
        D08ScopeFunctionsKt    let, run, with, apply, also
        D09SequencesKt         sequences vs. collections, lazy evaluation

        Run a single demo:
          ./gradlew :block2-grundlagen-2:run -PmainClass=demo.D05SealedClassesKt
        """.trimIndent()
    )
}
