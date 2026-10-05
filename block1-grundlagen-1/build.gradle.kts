// Block 1 needs nothing beyond Kotlin itself and a test framework.
// That is exactly the message of this file: a Kotlin project starts small.
dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
