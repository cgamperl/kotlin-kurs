# Cheat-Sheet Block 4 — Fehler, Flow und Tests: Kotlin ↔ C# ↔ C++

## Exceptions in Coroutines

Die eine Regel, aus der fast alles folgt:

| Builder | Verhalten bei einer Exception |
| --- | --- |
| `launch` | **Fehler** – wandert sofort nach oben, bricht Geschwister ab |
| `async` | **Ergebnis** – liegt im `Deferred`, wird erst bei `await()` geworfen |

| Kotlin | C# | C++ |
| --- | --- | --- |
| `try/catch` in der Coroutine | `try/catch` im `async`-Methodenrumpf | `try/catch` im Thread-Körper |
| `deferred.await()` wirft | `await task` wirft | `future.get()` wirft |
| `CoroutineExceptionHandler` | `TaskScheduler.UnobservedTaskException` | `std::set_terminate` |
| Fehler bricht Geschwister ab | Tasks sind unabhängig | Threads sind unabhängig |
| `supervisorScope` – unabhängig | Standardverhalten | Standardverhalten |

`CoroutineExceptionHandler` greift nur bei `launch` und nur bei einer
**Wurzel**-Coroutine – innerhalb von `supervisorScope` ist jedes Kind eine
Wurzel, deshalb funktioniert er dort am Kind.

## Cancellation

**Cancellation ist kooperativ.** Abbrechen setzt ein Flag; erzwungen wird
nichts – ein hart abgebrochener Thread hinterlässt gehaltene Sperren und
halb geschriebene Daten.

| Kotlin | C# | C++ |
| --- | --- | --- |
| `job.cancel()` | `cts.Cancel()` | `jthread::request_stop()` (C++20) |
| `isActive` | `token.IsCancellationRequested` | `stop_token::stop_requested()` |
| `ensureActive()` | `token.ThrowIfCancellationRequested()` | – |
| `yield()` | `await Task.Yield()` | `std::this_thread::yield()` |
| `CancellationException` | `OperationCanceledException` | – |
| `finally { }` | `finally { }` | RAII-Destruktor |
| `withContext(NonCancellable)` | – | – |

Eine Coroutine merkt ihre Cancellation nur an einem **Suspendierungspunkt**
oder wenn sie selbst nachsieht. Reine Rechenschleifen laufen weiter.

⚠️ **Die zwei Fallen:**

```kotlin
// 1. Verschluckt die Cancellation - die Coroutine lässt sich nicht stoppen
try { ... } catch (e: Exception) { log(e) }

// 2. Das finally läuft an, kommt aber nicht über sein delay hinaus
finally { delay(100); close() }
finally { withContext(NonCancellable) { delay(100); close() } }   // richtig
```

## Flow

| Kotlin | C# | C++ |
| --- | --- | --- |
| `Flow<T>` | `IAsyncEnumerable<T>` | – |
| `flow { emit(x) }` | `async IAsyncEnumerable` + `yield return` | Coroutine-Generator (C++20) |
| `collect { }` | `await foreach` | – |
| `StateFlow<T>` | `BehaviorSubject<T>` (Rx) | – |
| `SharedFlow<T>` | `Subject<T>` (Rx) | – |
| cold (pro Collector neu) | `IAsyncEnumerable` ist auch cold | – |

`Flow` verhält sich wie eine `Sequence`, die suspendieren darf: lazy, nichts
läuft ohne terminalen Operator, und Operatoren bauen nur die Kette auf.

### Operatoren

| | vertraut aus Block 2 | neu, weil es um Zeit oder Kontext geht |
| --- | --- | --- |
| | `map`, `filter`, `take`, `drop` | `debounce`, `sample` |
| | `fold`, `runningFold` (= `scan`) | `flowOn`, `buffer`, `conflate` |
| | `onEach` (das `also` der Flows) | `catch`, `retry`, `retryWhen` |
| | `zip` | `combine`, `merge`, `flatMapConcat`/`flatMapMerge` |

| Operator | Wirkung |
| --- | --- |
| `zip` | wartet auf beide Seiten, bildet strenge Paare |
| `combine` | emittiert bei **jeder** Änderung, mit dem letzten Wert der anderen |
| `merge` | abonniert alle Quellen gleichzeitig, Reihenfolge ist zufällig |
| `flatMapConcat` | arbeitet die Quellen **nacheinander** ab |
| `flowOn` | wechselt den Dispatcher für alles **stromaufwärts** |
| `buffer` | Produzent läuft dem Konsumenten voraus |
| `catch` | fängt nur Fehler von **stromaufwärts**, darf selbst emittieren |
| `retry(n)` | abonniert neu – bereits emittierte Werte kommen **erneut** |

⚠️ `debounce` ist noch `@FlowPreview`. Die meisten Operatoren sind stabil; die
zeitbezogenen sind die Ausnahme.

## Testing

| Kotlin (Kotest) | C# (xUnit/NUnit) | C++ (GoogleTest) |
| --- | --- | --- |
| `class X : StringSpec({ "name" { } })` | `[Fact] public void Name()` | `TEST(Suite, Name)` |
| `DescribeSpec` – `describe`/`it` | – | – |
| `a shouldBe b` | `Assert.Equal(b, a)` | `EXPECT_EQ(b, a)` |
| `shouldThrow<E> { }` | `Assert.Throws<E>` | `EXPECT_THROW` |
| `withData(1, 2, 3) { }` | `[Theory]` + `[InlineData]` | `TEST_P` / `INSTANTIATE_TEST_SUITE_P` |
| keine Annotationen, normale Klassen | Attribute | Makros |

| MockK | Moq (C#) | GoogleMock |
| --- | --- | --- |
| `mockk<T>()` | `new Mock<T>()` | `MockT mock;` |
| `every { m.f() } returns x` | `mock.Setup(...).Returns(x)` | `ON_CALL(...).WillByDefault(...)` |
| `coEvery { }` (für `suspend`) | – | – |
| `verify(exactly = 1) { }` | `mock.Verify(..., Times.Once)` | `EXPECT_CALL(...).Times(1)` |
| `coVerify { }` | – | – |
| `slot<T>()` + `capture(slot)` | `It.Is<T>` / Callback | `SaveArg` |
| `throws E andThenJust Runs` | `SetupSequence` | `WillOnce().WillRepeatedly()` |

### Coroutine-Tests

Das zentrale Werkzeug ist die **virtuelle Zeit**.

```kotlin
runTest {
    service.pingWithRetry(id)     // wartet intern 1 s + 2 s + 3 s
    currentTime shouldBe 6000     // … in Mikrosekunden echter Laufzeit
}
```

| Werkzeug | Wofür |
| --- | --- |
| `runTest { }` | Test-Coroutine mit virtueller Zeit |
| `currentTime` | aktueller Stand der virtuellen Uhr (Extension – **importieren!**) |
| `advanceTimeBy(ms)` | Uhr gezielt vorstellen |
| `advanceUntilIdle()` | alles Anstehende abarbeiten |
| `runCurrent()` | nur das Fällige, ohne Zeit zu bewegen |
| `StandardTestDispatcher` | Voreinstellung: Coroutines werden **eingereiht** |
| `UnconfinedTestDispatcher` | Coroutines starten sofort |
| `Dispatchers.setMain` / `resetMain` | für Code, der `Dispatchers.Main` fest verdrahtet |

⚠️ `currentTime` ist eine **Extension Property** und braucht einen eigenen
Import: `import kotlinx.coroutines.test.currentTime`.
