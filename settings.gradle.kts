plugins {
    // Resolves the JDK for the toolchain declared in build.gradle.kts.
    // Without a toolchain repository Gradle still auto-provisions a JDK, but
    // warns that this is deprecated - and Gradle 10 will refuse to do it.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kotlin-kurs"

// One module per block. The modules build on each other by giving each one
// only the dependencies its block actually needs:
//   block 1 + 2 : Kotlin and tests only
//   block 3     : + coroutines
//   block 4     : + coroutines-test, Kotest, MockK
// That way it is visible at every point what a topic costs in infrastructure.
include(
    "block1-grundlagen-1",
    "block2-grundlagen-2",
    "block3-coroutines-1",
    "block4-coroutines-2",
    "solutions",
)
