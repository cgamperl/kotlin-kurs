plugins {
    // Im Root nur deklariert, nicht angewendet - angewendet wird es unten
    // fuer jedes Unterprojekt.
    alias(libs.plugins.kotlin.jvm) apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    // Die Java-Toolchain gilt auch fuer die Kotlin-Kompilierung. Gradle laedt
    // das JDK bei Bedarf selbst herunter - dadurch baut das Projekt auf jedem
    // Teilnehmer-Rechner gleich, egal welches JDK dort gerade JAVA_HOME ist.
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    // Makes non-ASCII output correct when started from the command line.
    // IntelliJ already defaults to UTF-8; in a Windows console run
    // "chcp 65001" once.
    tasks.withType<JavaExec>().configureEach {
        systemProperty("stdout.encoding", "UTF-8")
        systemProperty("stderr.encoding", "UTF-8")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "failed", "skipped")
            showStandardStreams = true
        }
    }

    // ---- nur fuer die Block-Module -------------------------------------
    if (name.startsWith("block")) {

        // "./gradlew :block1-grundlagen-1:run" startet die Uebersicht,
        // "-PmainClass=demo.D02NullabilityKt" startet eine konkrete Demo.
        // In IntelliJ genuegt sonst das gruene Dreieck neben main().
        apply(plugin = "application")
        extensions.configure<JavaApplication> {
            mainClass.set(
                providers.gradleProperty("mainClass").orElse("demo.OverviewKt")
            )
        }

        // The exercise tests are deliberately RED while the TODOs are open.
        // That is why "test" does not run automatically here: otherwise
        // "./gradlew build" would be permanently red during the course and
        // therefore useless as an overall check.
        tasks.named<Test>("test") {
            enabled = false
        }

        // They are still compiled on every build, so a starter that does
        // not compile is caught immediately.
        tasks.named("check") {
            dependsOn("testClasses")
        }

        // The task for the participants: "make this green".
        //
        // The source set is resolved DELIBERATELY outside the register
        // lambda: inside it the receiver is the task, so "extensions" would
        // be the task's extension container instead of the project's.
        val testSourceSet = extensions.getByType<SourceSetContainer>()["test"]

        tasks.register<Test>("exerciseTest") {
            group = "verification"
            description = "Runs the exercise tests of this block."

            testClassesDirs = testSourceSet.output.classesDirs
            classpath = testSourceSet.runtimeClasspath

            // Only the exercises. Block 4 also ships demo specs (Kotest,
            // MockK) under "demo" - those are teaching material and are
            // green from the start, so they must not be mixed into the
            // participants' red/green signal.
            filter {
                includeTestsMatching("exercise.*")
                isFailOnNoMatchingTests = false
            }
        }

        // The counterpart: the runnable demo specs of a block.
        tasks.register<Test>("demoTest") {
            group = "verification"
            description = "Runs the demo specs of this block (block 4: Kotest/MockK)."

            testClassesDirs = testSourceSet.output.classesDirs
            classpath = testSourceSet.runtimeClasspath

            filter {
                includeTestsMatching("demo.*")
                isFailOnNoMatchingTests = false
            }
        }
    }
}
