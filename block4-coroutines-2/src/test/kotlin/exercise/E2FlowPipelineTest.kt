@file:OptIn(ExperimentalCoroutinesApi::class)

package exercise

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest

class E2FlowPipelineTest : StringSpec({

    // ----------------------------------------------------------------- 2a
    "emits every reading" {
        runTest {
            sensorFlow(sampleEvents).toList() shouldContainExactly sampleEvents
        }
    }

    "waits between the emissions" {
        runTest {
            sensorFlow(sampleEvents, intervalMillis = 100).toList()

            currentTime shouldBe 500          // five readings at 100 ms each
        }
    }

    "is cold and can be collected more than once" {
        runTest {
            val flow = sensorFlow(sampleEvents, intervalMillis = 0)

            // A cold flow restarts for every collector, so both runs see
            // the complete list.
            flow.toList() shouldContainExactly sampleEvents
            flow.toList() shouldContainExactly sampleEvents
        }
    }

    "handles an empty list" {
        runTest {
            sensorFlow(emptyList()).toList() shouldContainExactly emptyList()
        }
    }

    // ----------------------------------------------------------------- 2b
    "reports only the anomalies" {
        runTest {
            val messages = sensorFlow(sampleEvents, intervalMillis = 0)
                .anomalyMessages(threshold = 70.0)
                .toList()

            messages shouldContainExactly listOf(
                "sen-12: 71.2 above 70.0",
                "sen-12: 85.0 above 70.0",
            )
        }
    }

    "includes the threshold itself" {
        runTest {
            val messages = sensorFlow(listOf(SensorEvent("a", 70.0)), intervalMillis = 0)
                .anomalyMessages(threshold = 70.0)
                .toList()

            messages shouldContainExactly listOf("a: 70.0 above 70.0")
        }
    }

    // ----------------------------------------------------------------- 2c
    "emits the running average" {
        val events = listOf(
            SensorEvent("a", 10.0),
            SensorEvent("a", 20.0),
            SensorEvent("a", 30.0),
        )

        runTest {
            val averages = sensorFlow(events, intervalMillis = 0).runningAverage().toList()

            averages.size shouldBe 3
            averages[0] shouldBe (10.0 plusOrMinus 0.0001)
            averages[1] shouldBe (15.0 plusOrMinus 0.0001)
            averages[2] shouldBe (20.0 plusOrMinus 0.0001)
        }
    }

    "emits nothing for an empty flow" {
        runTest {
            sensorFlow(emptyList(), intervalMillis = 0).runningAverage().toList().size shouldBe 0
        }
    }

    // ----------------------------------------------------------------- 2d
    "replaces a failure with the fallback" {
        val fallback = SensorEvent("sen-12", -1.0)

        val failing = flow {
            emit(SensorEvent("sen-12", 21.4))
            emit(SensorEvent("sen-12", 22.8))
            throw IllegalStateException("sensor defective")
        }

        runTest {
            val result = failing.withFallback(fallback).toList()

            result shouldContainExactly listOf(
                SensorEvent("sen-12", 21.4),
                SensorEvent("sen-12", 22.8),
                fallback,
            )
        }
    }

    "leaves a healthy flow untouched" {
        runTest {
            val result = sensorFlow(sampleEvents, intervalMillis = 0)
                .withFallback(SensorEvent("x", -1.0))
                .toList()

            result shouldContainExactly sampleEvents
        }
    }

    // ----------------------------------------------------------------- 2e
    "summarises the stream" {
        runTest {
            val summary = sensorFlow(sampleEvents, intervalMillis = 0).summarise()

            summary.count shouldBe 5
            summary.maximum shouldBe (85.0 plusOrMinus 0.0001)
            summary.average shouldBe (44.9 plusOrMinus 0.0001)
        }
    }

    "summarises an empty stream" {
        runTest {
            sensorFlow(emptyList(), intervalMillis = 0).summarise() shouldBe
                StreamSummary(0, 0.0, 0.0)
        }
    }

    // --------------------------------------------------------- everything
    "the whole pipeline works end to end" {
        runTest {
            val messages = sensorFlow(sampleEvents, intervalMillis = 100)
                .withFallback(SensorEvent("x", -1.0))
                .anomalyMessages(threshold = 70.0)
                .toList()

            messages.size shouldBe 2
            currentTime shouldBe 500
        }
    }
})
