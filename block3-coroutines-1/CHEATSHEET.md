# Cheat-Sheet Block 3 — Nebenläufigkeit: Kotlin ↔ C# ↔ C++

## Das Grundmodell

| | Kotlin | C# | C++ |
| --- | --- | --- | --- |
| Einheit | Coroutine | `Task` | `std::thread` / `std::async` |
| Kosten | ~einige Dutzend Byte, Heap | Task-Objekt + Threadpool | ~1 MB Stack je Thread |
| Wechsel | normaler Methodenaufruf | Threadpool-Scheduling | Kernel-Kontextwechsel |
| 200.000 Stück | ~1 Sekunde | machbar | `OutOfMemoryError` |
| Markierung | `suspend fun` | `async Task<T>` | – |
| Warten | `await()` / direkter Aufruf | `await` | `.get()` (**blockiert**) |

Zwei Punkte, die den Tabellen nicht anzusehen sind:

- `await()` **suspendiert** die Coroutine und gibt den Thread frei; ein
  blockierendes Warten belegt ihn weiter.
- Eine `suspend`-Funktion ist für sich genommen **sequenziell**.
  Nebenläufigkeit entsteht erst durch `launch` oder `async`.

## Builder

| Kotlin | Bedeutung | C#-Entsprechung |
| --- | --- | --- |
| `runBlocking { }` | Brücke von blockierend zu suspendierend | `.GetAwaiter().GetResult()` |
| `launch { }` | Feuern und vergessen, liefert `Job` | `_ = Task.Run(...)` |
| `async { }` | Berechnung, liefert `Deferred<T>` | `Task.Run(...)` |
| `withContext(d) { }` | Dispatcher wechseln, kein neues Kind | `ConfigureAwait` (nur entfernt ähnlich) |
| `coroutineScope { }` | Scope, wartet auf alle Kinder | `Task.WhenAll` (ohne Cancellation) |
| `supervisorScope { }` | wie oben, Kinder scheitern unabhängig | – |

`runBlocking` gehört in `main()` und in Tests – nicht in Produktionscode.

## Sequenziell vs. parallel

```kotlin
// sequenziell: 200 + 200 = 400 ms
val a = fetchA()
val b = fetchB()

// parallel: ~200 ms
val a = async { fetchA() }
val b = async { fetchB() }
a.await() to b.await()

// FALSCH - wieder sequenziell, weil zu früh gewartet wird
val a = async { fetchA() }.await()
val b = async { fetchB() }.await()
```

Die dritte Variante kompiliert fehlerfrei, führt die beiden Aufrufe aber
weiterhin nacheinander aus.

## Scope und Structured Concurrency

Drei Regeln:

1. Jede Coroutine hat einen Eltern-Scope.
2. Ein Eltern-Scope wird nicht fertig, bevor alle Kinder fertig sind.
3. Abbruch wandert **abwärts**, Fehler wandern **aufwärts** und dann wieder abwärts.

| Kotlin | Wirkung |
| --- | --- |
| `coroutineScope { }` | Fehler eines Kindes bricht die Geschwister ab |
| `supervisorScope { }` | Kinder scheitern unabhängig voneinander |
| `CoroutineScope(SupervisorJob())` | eigener Scope mit Lebenszyklus |
| `scope.cancel()` | bricht alle Coroutines des Scopes ab |
| `GlobalScope` | lebt so lange wie der Prozess – **Anti-Pattern** |

Faustregel: Lässt sich nicht benennen, **welche Instanz** eine Coroutine
abbricht, ist der Scope falsch gewählt.

## Dispatchers

| Kotlin | Pool | Wofür | C# |
| --- | --- | --- | --- |
| `Dispatchers.Default` | so viele Threads wie Kerne | CPU-Arbeit | Threadpool |
| `Dispatchers.IO` | elastisch, 64+ | blockierendes I/O | Threadpool + `LongRunning` |
| `Dispatchers.Unconfined` | keiner | Spezialfälle | – |
| `Dispatchers.Main` | UI-Thread | UI-Updates | `SynchronizationContext` |

```kotlin
// So wird eine blockierende Bibliothek coroutine-tauglich:
suspend fun readConfig(id: String) = withContext(Dispatchers.IO) {
    legacyBlockingRead(id)      // Thread.sleep, JDBC, File-I/O
}
```

Der Wrapper gehört **an den blockierenden Aufruf**, nicht an jede Aufrufstelle.
Danach hält die `suspend`-Funktion ihr Versprechen, den Aufrufer nicht zu
blockieren.

⚠️ `IO` und `Default` teilen sich denselben Pool. Die Threadnamen sehen
identisch aus (`DefaultDispatcher-worker-N`) – man kann die Wahl also nicht
am Namen ablesen und auch nicht testen.

## Timeouts

| Kotlin | Verhalten | C# |
| --- | --- | --- |
| `withTimeout(ms) { }` | wirft `TimeoutCancellationException` | `CancellationTokenSource(ms)` |
| `withTimeoutOrNull(ms) { }` | liefert `null` | – |

⚠️ `TimeoutCancellationException` ist eine `CancellationException`. Ein
`catch (e: Exception)` schluckt sie – und damit hört das Timeout auf zu wirken.

## Die Stolperfallen auf einen Blick

| Falle | Symptom | Abhilfe |
| --- | --- | --- |
| `async { }.await()` in einer Zeile | kein Geschwindigkeitsgewinn | beide `async` vor dem ersten `await` |
| blockierender Aufruf in einer Coroutine | UI/Event-Loop steht | `withContext(Dispatchers.IO)` |
| Timeout um `await()` statt um `async` | Scope wartet trotzdem zu Ende | Timeout **in** den `async` |
| Busy-Loop im `runBlocking`-Scope | hängt komplett | `Dispatchers.Default` |
| `catch (e: Exception)` | Coroutine lässt sich nicht abbrechen | konkreten Typ fangen |
| `GlobalScope.launch` | Coroutine läuft bis zum Prozessende weiter | Scope mit Lebenszyklus |
