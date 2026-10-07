# Block 4 – Coroutines II & Praxis

## Demos

### Lauffähige Demos (`src/main/kotlin/demo/`)

| Datei | Thema |
| --- | --- |
| `D01ExceptionHandling.kt` | `launch` vs. `async`, `CoroutineExceptionHandler`, `supervisorScope` |
| `D02Cancellation.kt` | kooperatives Abbrechen, `NonCancellable`, Timeouts |
| `D03FlowBasics.kt` | cold vs. hot, `flow { }`, terminale Operatoren, `StateFlow`/`SharedFlow` |
| `D04FlowOperators.kt` | `map`/`filter`/`transform`, `debounce`, `flowOn`, `buffer`, `catch`, `retry`, `zip`/`combine` |
| `DeviceService.kt` | Testobjekt für die beiden Test-Demos (keine Demo) |

```bash
./gradlew :block4-coroutines-2:run -PmainClass=demo.D03FlowBasicsKt
```

### Test-Demos (`src/test/kotlin/demo/`)

Diese beiden sind selbst Tests. Sie werden **ausgeführt**, nicht gestartet –
und sie sind von Anfang an grün.

| Datei | Thema |
| --- | --- |
| `D05UnitTestingSpec.kt` | Kotest-Styles, MockK, datengetriebene Tests, handgeschriebene Fakes |
| `D06CoroutineTestingSpec.kt` | `runTest`, virtuelle Zeit, Test-Dispatcher, `setMain` |

```bash
./gradlew :block4-coroutines-2:demoTest
```

> Sie laufen **nicht** unter `exerciseTest` mit – sonst wäre das Rot/Grün der
> Teilnehmer mit fremden Ergebnissen vermischt.

## Übungen

```bash
./gradlew :block4-coroutines-2:exerciseTest                            # alle
./gradlew :block4-coroutines-2:exerciseTest --tests "exercise.E2*"     # eine
```

Alle Übungstests nutzen `runTest` – die gesamte Suite läuft trotz Sekunden an
simulierter Wartezeit in unter einer Sekunde.

### Übung 1 – Exceptions, Retry, Cancellation (`E1Resilience.kt`)

- **1a** `readStatusWithRetry` – exponentielles Backoff (100, 200, 400 …),
  **kein** Backoff nach dem letzten Versuch
- **1b** `readStatusOrNull` – Timeout über die **gesamte** Retry-Folge
- **1c** `readAllStatuses` – mehrere Geräte, Teilausfall erlaubt
- **1d** `pollUntilCancelled` – Aufräumen, das Cancellation überlebt

> **1c ist der Kern:** `coroutineScope` bricht bei einem Fehler alle
> Geschwister ab – genau das Gegenteil des Gewünschten. Hier gehört
> `supervisorScope` hin.

> **1d braucht `NonCancellable`:** In einer abgebrochenen Coroutine scheitert
> jeder suspendierende Aufruf sofort. Ohne `withContext(NonCancellable)`
> läuft das `finally` zwar an, kommt aber über sein `delay` nicht hinaus.

### Übung 2 – Flow-Pipeline (`E2FlowPipeline.kt`)

- **2a** `sensorFlow` – kalter Flow aus einer Liste
- **2b** `anomalyMessages` – `filter` + `map`
- **2c** `runningAverage` – `runningFold`; **Achtung:** der emittiert auch den
  Startwert, also `drop(1)`
- **2d** `withFallback` – `catch` darf selbst emittieren
- **2e** `summarise` – terminaler Operator mit `fold`

### Übung 3 – Tests selbst schreiben (`E3AlarmService.kt`)

Die einzige Übung ohne TODOs im Produktivcode: Der Service ist fertig, **die
Tests fehlen**. Ein ausgearbeitetes Beispiel steht in
`src/test/kotlin/exercise/E3AlarmServiceTest.kt`, die Checkliste ebenfalls.

Abzudecken sind:

| Bereich | Fälle |
| --- | --- |
| Schwellwerte | 59 / 60 / 89 / 90 / 100 |
| Meldung | Name aus dem Lookup, Rückfall auf die Geräte-Id |
| Sink | genau einmal veröffentlicht, nichts unterhalb der Schwelle |
| Retry | zweiter Versuch, 200 ms Wartezeit (`currentTime`), Fehler beim zweiten Mal |
| Validierung | `IllegalArgumentException` außerhalb 0..100 |
| `raiseAll` | nur ausgelöste Alarme, Eingabereihenfolge, leere Map |

Diese Übung wird **nicht automatisch bewertet** – das geht bei einer
Testaufgabe nicht sinnvoll. Die Musterlösung
(`solutions/src/test/kotlin/block4/S3AlarmServiceTest.kt`) dient als Vergleich
und zeigt, wie eine Test-Suite strukturiert werden kann.

### Übung 4 – Praxisprojekt

Siehe `praxisprojekt/README.md`.

## Musterlösungen

`solutions/src/main/kotlin/block4/`

```bash
./gradlew :solutions:test --tests "block4.*"
```

## Weiterführend

`CHEATSHEET.md` – Exceptions, Cancellation, Flow und Testing im Vergleich.
