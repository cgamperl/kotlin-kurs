# Cheat-Sheet Block 1 — Kotlin ↔ C# ↔ C++

Gedacht zum Nachschlagen während des Kurses. Die Spalten C# und C++ nennen das
jeweils *nächstliegende* Gegenstück, nicht immer ein exaktes.

## Deklarationen

| Kotlin | C# | C++ |
| --- | --- | --- |
| `val x = 1` | `readonly`/`const`, lokal `var` ohne Neuzuweisung | `const auto x = 1;` |
| `var x = 1` | `var x = 1;` | `auto x = 1;` |
| `const val X = 1` (nur top-level/companion) | `const int X = 1;` | `constexpr int X = 1;` |
| Funktion ohne Klasse (top-level) | statische Methode in einer Klasse nötig | freie Funktion – vertraut |
| `fun f(x: Int) = x * x` | `int F(int x) => x * x;` | `auto f(int x) { return x * x; }` |
| Typ steht **hinter** dem Namen | Typ steht davor | Typ steht davor |

## Nullability

| Kotlin | C# | C++ |
| --- | --- | --- |
| `String` (kann nie null sein) | `string` mit aktivierten NRTs | `std::string` (Wert) |
| `String?` | `string?` | `std::optional<std::string>` / Zeiger |
| `a?.b` | `a?.b` | `if (a) a->b` bzw. `a.and_then(...)` |
| `a ?: b` | `a ?? b` | `a.value_or(b)` |
| `a!!` | `a!` (null-forgiving) | `*a` ohne Prüfung |
| `requireNotNull(a) { "…" }` | `ArgumentNullException.ThrowIfNull(a)` | `assert(a)` |
| Smart Cast nach `if (a != null)` | Flow-Analyse der NRTs | kein Äquivalent |

**Wichtig:** Nullability ist Teil des Typsystems. `String` und `String?` sind
zwei verschiedene Typen; der Compiler erzwingt die Unterscheidung. Die
einzige Lücke sind Platform Types aus Java-Interop — dort erzwingt Kotlin
nichts, weshalb an der Systemgrenze ein expliziter Typ gehört.

⚠️ **Die häufigste Stolperstelle in der Übung:** In einer Kette bleibt der Typ
nullable, solange nicht der Elvis-Operator greift.

```kotlin
raw["name"]?.trim().takeIf { … }    // Fehler: takeIf auf String?
raw["name"]?.trim()?.takeIf { … }   // richtig: der Safe Call wird durchgereicht
```

## Unveränderlichkeit

| Kotlin | C# | C++ |
| --- | --- | --- |
| `val` (Referenz nicht neu zuweisbar) | `readonly` Feld | `T* const` |
| `List<T>` (Read-only-**Sicht**) | `IReadOnlyList<T>` | `const std::vector<T>&` |
| `MutableList<T>` | `List<T>` | `std::vector<T>` |
| `listOf(...)` (unveränderlich) | `ImmutableList.Create(...)` | – |
| `.toList()` (defensive Kopie) | `.ToList()` | Kopierkonstruktor |

⚠️ Gemeinsame Falle in allen drei Sprachen: Eine Read-only-**Sicht** ist keine
Kopie. `List<T>` in Kotlin und `IReadOnlyList<T>` in C# zeigen beide
Änderungen am zugrundeliegenden Objekt.

## Datenklassen und Wertetypen

| Kotlin | C# | C++ |
| --- | --- | --- |
| `data class P(val x: Int)` | `record P(int X);` | `struct P { int x; };` + `operator==` |
| generiert `equals`/`hashCode` | generiert `Equals`/`GetHashCode` | seit C++20: `= default` für `operator==` |
| `copy(x = 1)` | `with { X = 1 }` | manuell |
| Destructuring `val (a, b) = p` | Deconstruct-Methode | `auto [a, b] = p;` (structured bindings) |
| `@JvmInline value class Id(val s: String)` | `readonly record struct Id(string S);` | Strong-Typedef-Idiom, oft per Template |

Value Classes machen aus einem `String` einen eigenen Typ, damit
`label(a, b)` nicht mit vertauschten Argumenten aufrufbar ist. Zur Laufzeit
kostet das in der Regel nichts – der Wrapper wird wegoptimiert.

⚠️ Nur Properties im **Primärkonstruktor** zählen für `equals`, `hashCode`,
`toString`, `copy` und Destructuring. Ein `var note` im Klassenrumpf ist für
all das unsichtbar.

## Sichtbarkeit

| Kotlin | C# | C++ |
| --- | --- | --- |
| `public` (Voreinstellung!) | `public` (Voreinstellung: `internal`/`private`) | `public:` |
| `private` (Klasse **oder Datei**) | `private` | `private:` |
| `protected` (nur Unterklassen) | `protected` | `protected:` |
| `internal` (Gradle-Modul) | `internal` (Assembly) | kein Äquivalent |
| – (kein paketprivat) | – | `friend` |

Zwei Stolpersteine: Kotlins Voreinstellung ist `public`, und `protected`
umfasst – anders als in Java – **nicht** das Paket.

## Delegation

| Kotlin | C# | C++ |
| --- | --- | --- |
| `class A(b: B) : I by b` | jede Methode manuell weiterleiten | Weiterleitung oder private Vererbung |
| `val x by lazy { … }` | `Lazy<T>` | `std::once_flag` / statisch lokal |
| `var x by Delegates.observable(…)` | `INotifyPropertyChanged` von Hand | – |
| `var x by Delegates.vetoable(…)` | Setter mit Validierung | Setter mit Validierung |

Die Interface-Delegation erzeugt die Weiterleitungsmethoden zur Compile-Zeit.
Kommt dem Interface später eine Methode hinzu, muss nichts nachgezogen werden.

## Idiome

| Kotlin | C# | C++ |
| --- | --- | --- |
| Default-Argumente | optionale Parameter | Default-Argumente |
| Benannte Argumente | benannte Argumente | designated initializers (C++20, nur Aggregate) |
| `"Text $wert"` | `$"Text {wert}"` | `std::format("Text {}", wert)` |
| `"""…"""` | `"""…"""` (C# 11) | Rohstring `R"(…)"` |
| `when` **als Ausdruck** | `switch`-Ausdruck | `switch`-Anweisung (kein Ausdruck) |
| `if` als Ausdruck | ternärer Operator `?:` | ternärer Operator `?:` |
| `for (i in 1..10)` | `for (int i = 1; i <= 10; i++)` | `for (int i = 1; i <= 10; ++i)` |
| `x in 1..10` | `x is >= 1 and <= 10` | `1 <= x && x <= 10` |
| `repeat(3) { … }` | `for` | `for` |
| `x.takeIf { … }` | – | – |

Weil `if` und `when` Ausdrücke sind, gibt es in Kotlin keinen ternären
Operator – er wäre überflüssig.

## Context Parameters (Kotlin 2.4, stabil)

```kotlin
context(log: AuditLog)
fun startDevice(device: Device) { log.write("…") }
```

Es gibt weder in C# noch in C++ ein direktes Gegenstück. Am nächsten kommen:

| Ansatz | Problem |
| --- | --- |
| Parameter durchreichen | jede Signatur auf dem Weg trägt ihn mit |
| Feld in der Klasse | Zustand, schlechter testbar |
| `AsyncLocal<T>` (C#) / TLS (C++) | erst zur Laufzeit sichtbar, nicht statisch geprüft |
| Ambient Context / Service Locator | unsichtbare Abhängigkeit |

Context Parameters stehen in der Signatur, werden statisch geprüft – und am
Aufrufort trotzdem nicht übergeben.

## Ausblick: Collection Literals

Kotlin 2.4 kennt experimentell (`-Xcollection-literals`):

```kotlin
val shapes = ["triangle", "square"]
```

Das entspricht den *collection expressions* aus C# 12 (`int[] x = [1, 2, 3];`)
und `std::initializer_list` in C++. Im Kurs verwenden wir es nicht – es ist
noch experimentell.
