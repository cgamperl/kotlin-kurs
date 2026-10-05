// Block 2 keeps the same dependencies as block 1 - everything shown here
// (generics, sealed classes, collections, sequences) lives in the standard
// library.
dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
