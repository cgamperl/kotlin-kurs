// Block 4 brings along the test tooling that is itself the subject of this
// block: Kotest as the test framework, MockK for mocking and
// kotlinx-coroutines-test for runTest and virtual time.
dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.junit.platform.launcher)
}
