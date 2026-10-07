# Block 1 – Setup & Sprachgrundlagen I

## Demos (Live-Coding)

Jede Demo ist eine eigenständig startbare Datei unter `src/main/kotlin/demo/`.
In IntelliJ genügt das grüne Dreieck neben `main()`.

| Datei | Thema |
| --- | --- |
| `D01ProjectSetup.kt` | Top-Level-Funktionen, `main()` ohne Klasse, `const val` |
| `D02Nullability.kt` | Safe Call, Elvis, Smart Cast, `let`, `requireNotNull`, Platform Types |
| `D03TypeInference.kt` | Typinferenz – und wo man Typen trotzdem hinschreibt |
| `D04Immutability.kt` | `val`/`var`, `List` vs. `MutableList`, defensive Kopie |
| `D05DataClasses.kt` | Datenklassen, `copy`, Destructuring, `@JvmInline value class` |
| `D06Visibility.kt` | `public`/`private`/`protected`/`internal` |
| `D07Delegation.kt` | Interface-Delegation `by`, `by lazy`, `observable`, `vetoable` |
| `D08Idioms.kt` | Default Args, String Templates, `when`, Ranges, `takeIf`, `if` als Ausdruck |
| `D09ContextParameters.kt` | Context Parameters (seit Kotlin 2.4 stabil) |

Start über die Kommandozeile:

```bash
./gradlew :block1-grundlagen-1:run                                   # Übersicht
./gradlew :block1-grundlagen-1:run -PmainClass=demo.D05DataClassesKt # eine Demo
```

## Übungen

Alle Übungen liegen unter `src/main/kotlin/exercise/`. Prüfen mit:

```bash
./gradlew :block1-grundlagen-1:exerciseTest                          # alle
./gradlew :block1-grundlagen-1:exerciseTest --tests "exercise.E2*"   # eine
```

> **So sind die Übungen gebaut:** Alle Deklarationen – Klassen, Signaturen,
> Rückgabetypen – sind bereits vorhanden; zu füllen sind nur die `TODO()`-Rümpfe.
> Dadurch kompiliert das Projekt jederzeit, und die Tests laufen von der
> ersten Minute an (zunächst rot).

### Übung 1 – Nullability (`E1Nullability.kt`)

Ein Gerät meldet Telemetrie als lose Textwerte. Felder können fehlen, `null`
sein oder Unsinn enthalten. Daraus sind verlässliche Werte zu machen.

- **1a** `readTemperature` – `Double?`, null bei fehlend/ungültig
- **1b** `readDeviceName` – getrimmt, Standardwert `"unknown device"`
- **1c** `readPort` – mit Bereichsprüfung `1..65535` und Standardwert
- **1d** `describe` – setzt die drei zusammen

**Regeln:** kein `!!` (ein Test prüft die Quelldatei darauf nach), kein
`try/catch` um Zahlkonvertierungen – nimm die `...OrNull`-Varianten.

### Übung 2 – Datenklassen & Value Classes (`E2DataClasses.kt`)

- **2a** `SensorId` darf nicht leer sein → `require` im `init`
- **2b** `Percent` liegt in `0..100` → `require` im `init`
- **2c** `Percent.isCritical` ab 90
- **2d** `Reading.withUtilisation` – neues Objekt über `copy()`, Original unverändert
- **2e** `Reading.label` – `"cam-04: 42 % (source: device)"`, bei kritisch mit `" [critical]"`

Zu beachten ist der Test `ignores the note when comparing`: Er zeigt, warum
`note` im Klassenrumpf etwas anderes ist als ein Konstruktor-Property.

### Übung 3 – Idiome: Refactoring (`E3Idioms.kt`)

**Die Tests sind hier von Anfang an grün.** Der Code funktioniert – er nutzt
nur nichts von dem, was Kotlin für diese sechs Fälle anbietet.

Alle sechs Funktionen sind idiomatisch umzuschreiben; die Tests müssen
durchgehend grün bleiben.

| | vorher | nachher |
| --- | --- | --- |
| 3a | if/else-Kaskade | `when` als Ausdruck |
| 3b | `StringBuilder` | String-Template |
| 3c | drei Überladungen | eine Funktion mit Default-Argumenten |
| 3d | `a >= 10 && a <= 90` | `a in 10..90` |
| 3e | verschachtelte null-Prüfungen | Safe Call + `takeIf` + Elvis |
| 3f | `var` mit nachträglicher Zuweisung | `if` als Ausdruck, `val` |

Wenn 3c fertig ist: den auskommentierten Test `allows named arguments`
einkommentieren. Er läuft erst, wenn es nur noch eine Funktion gibt.

Ziel: keine Funktion braucht mehr ein `return` mitten im Rumpf.

### Übung 4 – Zusammenfassende Übung: Geräte-Inventar (`E4Inventory.kt`)

Alles aus Block 1 in einem Stück.

- **4a** `report` mit `by lazy` – `"Inventory: 4 devices, 1 of them critical"`
- **4b** `find` – gibt `null` zurück bei unbekannter Kennung
- **4c** `locationOf` – zwei verschiedene Fehlergründe, ein Ergebnis
- **4d** `state` – `when` als Ausdruck inkl. null-Fall
- **4e** `department` – `when` über ein Enum, **ohne** `else`

Der Test `computes the report only once` prüft mit `assertSame`, dass
tatsächlich `by lazy` verwendet wurde und nicht ein `get()`.

## Musterlösungen

`solutions/src/main/kotlin/block1/` – jede Lösung ist kommentiert und erklärt,
warum sie so aussieht, wie sie aussieht.

```bash
./gradlew :solutions:test --tests "block1.*"
```

## Weiterführend

`CHEATSHEET.md` in diesem Ordner: Gegenüberstellung Kotlin ↔ C# ↔ C++ zu
allen Themen dieses Blocks.
