// From here on the first real external dependency joins in.
// Important for the participants: coroutines are NOT a language feature in
// the sense of a runtime - the compiler understands "suspend", but
// launch/async/Dispatchers come from the kotlinx-coroutines library.
dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
