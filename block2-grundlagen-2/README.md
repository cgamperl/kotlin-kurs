# Block 2 – Sprachgrundlagen II

## Demos (Live-Coding)

`src/main/kotlin/demo/` – jede Datei ist eigenständig startbar.

| Datei | Thema |
| --- | --- |
| `Domain.kt` | gemeinsames Modell für diesen Block (keine Demo) |
| `D01Generics.kt` | generische Funktionen/Klassen, `in`/`out`-Varianz, `where`, `reified` |
| `D02EnumClasses.kt` | Enums mit Properties und Methoden, `entries`, erschöpfendes `when` |
| `D03Oop.kt` | Konstruktoren, Properties, **final by default**, Interfaces, Explicit Backing Fields |
| `D04Objects.kt` | `object`, `companion object`, Factory, `invoke`, Objekt-Expression |
| `D05SealedClasses.kt` | Sealed Interfaces, Zustandsmodellierung, generisches `Outcome<T>` |
| `D06Functional.kt` | Collections-API, Extension Functions **und** Properties |
| `D07Lambdas.kt` | Lambdas, Trailing Lambda, Funktionsreferenzen, `inline` |
| `D08ScopeFunctions.kt` | `let`, `run`, `with`, `apply`, `also` mit Entscheidungstabelle |
| `D09Sequences.kt` | Sequences vs. Collections, Lazy Evaluation, Messung |

```bash
./gradlew :block2-grundlagen-2:run                                      # Übersicht
./gradlew :block2-grundlagen-2:run -PmainClass=demo.D09SequencesKt      # eine Demo
```

> **Tipp für D09:** Die Demo misst live und liefert absichtlich auch den
> Gegenbeweis mit – bei kleinen Datenmengen ist die Liste schneller als die
> Sequence.

## Übungen

```bash
./gradlew :block2-grundlagen-2:exerciseTest                            # alle
./gradlew :block2-grundlagen-2:exerciseTest --tests "exercise.E3*"     # eine
```

### Übung 1 – Klassenhierarchie übersetzen (`E1Oop.kt`)

Die folgende C#-Hierarchie ist nach Kotlin zu übertragen. Das Gerüst steht
bereits in der Datei; zu füllen sind die Rümpfe. Dabei zeigt sich, was in
Kotlin anders heißt oder ganz entfällt.

```csharp
public abstract class Component
{
    public string Id { get; }
    protected Component(string id) => Id = id;

    public abstract int MaintenanceIntervalMonths { get; }

    public virtual string SelfTest() => $"{Id}: ok";

    public override string ToString() => $"{GetType().Name}({Id})";
}

public interface ICalibratable
{
    double Offset { get; }
    void Calibrate(double value);
}

public sealed class Camera : Component
{
    public string Resolution { get; }
    public Camera(string id, string resolution) : base(id) => Resolution = resolution;

    public override int MaintenanceIntervalMonths => 6;
    public override string SelfTest() => $"{Id}: video signal ok ({Resolution})";
}

public sealed class Sensor : Component, ICalibratable
{
    public string Unit { get; }
    public double Offset { get; private set; }

    public Sensor(string id, string unit) : base(id) => Unit = unit;

    public override int MaintenanceIntervalMonths => 24;
    public void Calibrate(double value) => Offset += value;
    public override string SelfTest() => $"{Id}: measuring in {Unit}, offset {Offset}";
}
```
Beim Übersetzen sind vier Stellen zu beachten:

| C# | Kotlin |
| --- | --- |
| `public abstract int X { get; }` | `abstract val x: Int` |
| `virtual` | `open` (ohne das Schlüsselwort geht gar nichts) |
| Konstruktor + Zuweisung | Primärkonstruktor in der Klassenkopfzeile |
| `{ get; private set; }` | `var x = …` mit `private set` |

Zusätzlich: **1f** `maintenanceOverview` – eine Zeile je Komponente, mit `\n`
verbunden.

### Übung 2 – Sealed Classes: Zustände (`E2Sealed.kt`)

- **2a** `displayText` – erschöpfendes `when` **ohne** `else`
- **2b** `measurementsOrNull` – Typtest mit Smart Cast
- **2c** `isFinal` – bewusst als `when`, nicht als `!=`
- **2d** `isWorthRetrying` – Bedingungen innerhalb der `when`-Zweige

### Übung 3 – Funktionale Pipeline (`E3Functional.kt`)

Telemetrie auswerten. **Ohne eine einzige Schleife und ohne `var` lösbar.**

- **3a** `averagePerDevice` – `groupBy` + `mapValues`
- **3b** `peakPerDevice` – dasselbe Gerüst, andere Aggregation
- **3c** `outliers` – `filter`
- **3d** `topDevices` – auf 3a aufbauend, sortieren und abschneiden
- **3e** `Measurement.isNotable` – Extension **Property**
- **3f** `List<Measurement>.summarise` – Extension **Function**

### Übung 4 – Scope Functions (`E4ScopeFunctions.kt`)

**Die Tests sind von Anfang an grün.** Jede Funktion ist auf die angegebene
Scope Function umzubauen; die Tests müssen grün bleiben.

| | Aufgabe | Scope Function |
| --- | --- | --- |
| 4a | null-Prüfung mit Zwischenvariable | `let` |
| 4b | Objekt anlegen und konfigurieren | `apply` |
| 4c | fünfmal derselbe Empfänger | `with` |
| 4d | Protokollierung unterbricht die Kette | `also` |
| 4e | Berechnung auf einem Objekt | `run` |

### Übung 5 – Abschlussprojekt (`E5Capstone.kt`)

Telemetrie-Auswertung mit allem aus Block 2.

- **5a** `span` – generische Funktion mit Typschranke `T : Comparable<T>`
- **5b** `movingAverage` – Extension mit `windowed`
- **5c** `firstAnomalies` – **auf einer `Sequence`**; ein Test prüft mit einer
  unendlichen Sequence nach, dass nicht vorher materialisiert wird
- **5d** `reportLine` – erschöpfendes `when`
- **5e** `analysisPerDevice` – alles zusammen

## Musterlösungen

`solutions/src/main/kotlin/block2/`

```bash
./gradlew :solutions:test --tests "block2.*"
```

## Weiterführend

`CHEATSHEET.md` – Kotlin ↔ C# ↔ C++ zu allen Themen dieses Blocks.
