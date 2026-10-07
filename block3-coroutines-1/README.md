# Block 3 – Coroutines I

## Demos (Live-Coding)

`src/main/kotlin/demo/` – jede Datei ist eigenständig startbar.

| Datei | Thema |
| --- | --- |
| `D01WhyCoroutines.kt` | Threads vs. Coroutines, **gemessen** |
| `D02SuspendBuilders.kt` | `suspend`, `runBlocking`, `launch`, `async`/`await` |
| `D03CoroutineScope.kt` | `coroutineScope`, eigener Scope, `GlobalScope` als Anti-Pattern |
| `D04ContextDispatchers.kt` | `Default`/`IO`/`Unconfined`, `withContext`, `CoroutineName` |
| `D05StructuredConcurrency.kt` | Job-Hierarchie, Cancellation, `SupervisorJob` |

```bash
./gradlew :block3-coroutines-1:run                                          # Übersicht
./gradlew :block3-coroutines-1:run -PmainClass=demo.D01WhyCoroutinesKt      # eine Demo
```

## Übungen

```bash
./gradlew :block3-coroutines-1:exerciseTest                            # alle
./gradlew :block3-coroutines-1:exerciseTest --tests "exercise.E2*"     # eine
```

### Übung 1 (geführt) – sequenziell zu parallel (`E1Parallel.kt`)

Sechs Geräte abfragen, 200 ms je Abfrage.

- **1a** `collectStatusesInParallel` – 1200 ms sollen zu ~200 ms werden
- **1b** `deviceSummary` – Status und Auslastung eines Geräts gleichzeitig
- **1c** `allSummaries` – beides kombiniert, zwölf Aufrufe in ~200 ms

Die Tests messen mit. Der klassische Fehler – `async { … }.await()` in einer
Zeile – fällt dabei sofort auf.

### Übung 2 – Dispatchers (`E2Dispatchers.kt`)

Blockierende Alt-Bibliotheken coroutine-tauglich machen.

- **2a** `readConfiguration` – blockierendes I/O auf den passenden Dispatcher
- **2b** `readConfigurations` – mehrere gleichzeitig
- **2c** `checksum` – CPU-Arbeit, anderer Dispatcher

> **Der entscheidende Test** heißt `does not block the calling thread`. Er
> lässt eine Ticker-Coroutine auf dem Event-Loop von `runBlocking` mitlaufen.
> Wird der blockierende Aufruf direkt gemacht, kommt der Ticker nie dran und
> der Test scheitert. Das ist genau der Fehler, den man in Produktion erst
> merkt, wenn das UI steht.

### Übung 3 (frei) – Monitoring-Dashboard (`E3Dashboard.kt`)

Drei Quellen je Gerät, eine davon darf zu langsam sein.

- **3a** `deviceSnapshot` – alle drei parallel, Timeout nur auf die Temperatur
- **3b** `dashboard` – alle Geräte gleichzeitig
- **3c** `renderLine` – erschöpfendes `when`
- **3d** `countIncomplete`

> **Die Falle in 3a** ist die inhaltlich wichtigste Stelle des Blocks: Ein
> `withTimeoutOrNull` um `await()` bricht das *Warten* ab, nicht die Anfrage.
> Der `async`-Job bleibt Kind des Scopes, und `coroutineScope` wartet auf
> seine Kinder – die Funktion braucht dann trotzdem die vollen zwei Sekunden.
> Der Test `does not wait longer than the timeout allows` deckt das auf.
> Die Lösung: Das Timeout gehört **in** den `async`.

## Musterlösungen

`solutions/src/main/kotlin/block3/`

```bash
./gradlew :solutions:test --tests "block3.*"
```

## Weiterführend

`CHEATSHEET.md` – `suspend` gegen `async`/`await` gegen `std::async`.
