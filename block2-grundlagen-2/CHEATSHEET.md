# Cheat-Sheet Block 2 — Kotlin ↔ C# ↔ C++

## Generics

| Kotlin | C# | C++ |
| --- | --- | --- |
| `fun <T> f(x: T)` | `void F<T>(T x)` | `template<class T> void f(T x)` |
| `<T : Comparable<T>>` | `where T : IComparable<T>` | Concept / `requires` (C++20) |
| `where T : A, T : B` | `where T : A, B` | `requires A<T> && B<T>` |
| `interface Q<out T>` (Kovarianz) | `interface IQ<out T>` | – |
| `interface S<in T>` (Kontravarianz) | `interface IS<in T>` | – |
| `List<out E>` ist kovariant | `IEnumerable<out T>` | – |
| `inline fun <reified T>` | `typeof(T)` immer verfügbar | Typ ist immer bekannt |
| Type Erasure zur Laufzeit | Reified Generics (echte Laufzeittypen) | Monomorphisierung zur Compile-Zeit |

Kotlin-Generics werden **einmal** übersetzt, nicht pro Typ instanziiert. Was
der Compiler prüfen soll, muss deshalb in der Schranke stehen
(`: Comparable<T>`). Typparameter sind auf der JVM zur Laufzeit gelöscht –
deshalb braucht `is T` das Paar `inline` + `reified`.

## Enum Classes

| Kotlin | C# | C++ |
| --- | --- | --- |
| `enum class X(val v: Int)` | Enum + Attribut oder Extension-Methode | `enum class` (nur Werte) |
| Methoden in der Enum | nur über Extension-Methoden | nur über freie Funktionen |
| Implementierung je Konstante | – | – |
| `X.entries` | `Enum.GetValues<X>()` | – |
| `X.valueOf("A")` | `Enum.Parse<X>("A")` | – |
| `when` erschöpfend geprüft | `switch`-Ausdruck warnt | keine Prüfung |

Kotlin-Enums sind vollwertige Klassen. Verhalten lässt sich deshalb an die
Konstante binden, statt es in ein `when` auszulagern.

## OOP

| Kotlin | C# | C++ |
| --- | --- | --- |
| Klassen **final by default** | Klassen offen, Methoden final | Klassen offen, Methoden nicht virtuell |
| `open class` / `open fun` | `virtual` (Methoden) | `virtual` |
| `override` ist **Pflicht** | `override` Pflicht | `override` optional (empfohlen) |
| Primärkonstruktor im Klassenkopf | Konstruktor als Methode | Konstruktor als Methode |
| `init { }` | Konstruktorrumpf | Initialisierungsliste + Rumpf |
| `constructor(...) : this(...)` | `: this(...)` | delegierender Konstruktor |
| `val x get() = …` | `int X => …` | Getter-Methode |
| `var x` mit `private set` | `{ get; private set; }` | privates Feld + Getter |
| `field` im Setter | Backing Field automatisch | explizites Feld |
| Explicit Backing Field (2.4) | zwei Properties nötig | zwei Member nötig |
| eine Basisklasse, viele Interfaces | dito | Mehrfachvererbung erlaubt |
| Interface mit Default-Implementierung | ab C# 8 | rein virtuell + Default im Basistyp |

**Zu beachten:** `final by default`. Vererbung und Überschreiben verlangen in
Kotlin ein ausdrückliches `open`.

## object und companion object

| Kotlin | C# | C++ |
| --- | --- | --- |
| `object X { }` | `static class` oder Singleton-Muster | Meyers Singleton (`static` lokal) |
| threadsichere Lazy-Initialisierung ohne Zutun | `Lazy<T>` bzw. `static` | C++11 garantiert es für statische Lokale |
| `companion object` | `static` Member | `static` Member |
| `companion object` kann Interfaces implementieren | statische Member können das nicht | – |
| `operator fun invoke` | – | `operator()` |
| `object : I { }` (Objekt-Expression) | anonyme Klasse / Lambda | Lambda oder lokale Struktur |

## Sealed Classes

| Kotlin | C# | C++ |
| --- | --- | --- |
| `sealed interface S` | `abstract` + `sealed` (C# 9+) | `std::variant<A, B, C>` |
| `data object A : S` | Singleton-Record | leerer Struct-Alternativtyp |
| `data class B(val x: Int) : S` | `record B(int X) : S` | Struct mit Feldern |
| `when` **ohne** `else`, erschöpfend geprüft | Pattern Matching, warnt | `std::visit` + Overload-Set |
| Smart Cast nach `is` | Pattern `is B b` | `std::get_if` |

**Unterschied zum Enum:** Ein Enum beschreibt eine feste Menge von **Werten**,
eine Sealed Class eine feste Menge von **Typen** – und jeder Typ trägt eigene
Daten. Für Zustandsmodellierung ist das der Unterschied: Der Fehlerfall trägt
seine Meldung, der Erfolgsfall seine Daten.

## Collections

| Kotlin | C# (LINQ) | C++ (`<algorithm>` / `<ranges>`) |
| --- | --- | --- |
| `map { }` | `Select` | `std::transform` / `views::transform` |
| `filter { }` | `Where` | `std::copy_if` / `views::filter` |
| `flatMap { }` | `SelectMany` | `views::join` |
| `fold(init) { }` | `Aggregate` | `std::accumulate` |
| `reduce { }` | `Aggregate` ohne Startwert | `std::reduce` |
| `groupBy { }` | `GroupBy` | – (manuell über `std::map`) |
| `associateBy { }` | `ToDictionary` | – |
| `partition { }` | – (zweimal `Where`) | `std::partition` |
| `sumOf { }` | `Sum` | `std::accumulate` |
| `any` / `all` / `none` | `Any` / `All` / `!Any` | `any_of` / `all_of` / `none_of` |
| `firstOrNull { }` | `FirstOrDefault` | `std::find_if` |
| `maxByOrNull { }` | `MaxBy` | `std::max_element` |
| `sortedWith(compareBy(...))` | `OrderBy().ThenBy()` | `std::sort` mit Komparator |
| `chunked(n)` / `windowed(n)` | – | `views::chunk` / `views::slide` (C++23) |
| `zip(other)` | `Zip` | `views::zip` (C++23) |
| `distinctBy { }` | `DistinctBy` | – |

⚠️ Kotlin-Collections-Operationen sind **eifrig**: Jeder Schritt erzeugt
sofort eine neue Liste. Das lazy Gegenstück heißt `Sequence`.

## Extension Functions

| Kotlin | C# | C++ |
| --- | --- | --- |
| `fun Device.f() = …` | `static void F(this Device d)` | freie Funktion (ADL) |
| `val Device.p get() = …` | keine Extension-Properties bis C# 13 | – |
| statisch aufgelöst | statisch aufgelöst | statisch aufgelöst |
| kein Zugriff auf `private` | kein Zugriff auf `private` | `friend` nötig |

In allen drei Sprachen gilt: Eine Extension **überschreibt keine Methode**. Bei
gleicher Signatur gewinnt immer die Member-Funktion.

## Lambdas

| Kotlin | C# | C++ |
| --- | --- | --- |
| `{ x -> x * 2 }` | `x => x * 2` | `[](int x) { return x * 2; }` |
| `{ it * 2 }` | – | – |
| Trailing Lambda hinter den Klammern | – | – |
| `(Int) -> Boolean` | `Func<int, bool>` | `std::function<bool(int)>` |
| `::name` | `nameof` / Methodengruppe | `&Klasse::name` |
| `::Klasse` (Konstruktor) | – | – |
| `inline fun` – kein Objekt zur Laufzeit | Delegate-Allokation | oft wegoptimiert |
| Capture immer by reference | Capture by reference | `[=]` / `[&]` explizit |

Durch die **Trailing-Lambda-Konvention** sieht `repeat(3) { }` wie ein
Sprachkonstrukt aus, ist aber nur eine Funktion mit einem Lambda als letztem
Parameter.

## Scope Functions

Kein Gegenstück in C# oder C++ – am nächsten kommt C# `with { }` für Records
(entspricht grob `copy()`, nicht `apply`).

| | Objekt im Block | Rückgabe | Einsatz |
| --- | --- | --- | --- |
| `let` | `it` | Blockergebnis | null-Behandlung, Umformen |
| `run` | `this` | Blockergebnis | Berechnung auf einem Objekt |
| `with` | `this` | Blockergebnis | mehrere Zugriffe am Stück |
| `apply` | `this` | **das Objekt** | konfigurieren |
| `also` | `it` | **das Objekt** | Seiteneffekt in einer Kette |

Merkhilfe: `apply` und `also` geben das Objekt zurück und eignen sich damit
für Ketten. Die anderen drei liefern das Ergebnis des Blocks.

## Sequences

| Kotlin | C# | C++ |
| --- | --- | --- |
| `List<T>` (eifrig) | materialisierte `List<T>` | `std::vector<T>` |
| `Sequence<T>` (lazy) | `IEnumerable<T>` (deferred) | `std::ranges::views` |
| `asSequence()` | ist schon lazy | `views::all` |
| `generateSequence { }` | `yield return` in Schleife | `views::iota` |
| `toList()` (terminal) | `ToList()` | `ranges::to` (C++23) |

Wann lohnt sich `Sequence`? Bei **vielen Elementen und mehreren Schritten**,
oder wenn nur ein Teil gebraucht wird (`take`, `first`, `any`). Bei kleinen
Mengen ist die Liste schneller – die Demo `D09Sequences.kt` misst beides.
