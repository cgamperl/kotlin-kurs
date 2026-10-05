# Praxisprojekt – Monitoring-Dashboard

Das Abschlussprojekt des Kurses. Die Gruppen arbeiten selbstständig und
fragen bei Bedarf nach; am Ende stellt jede Gruppe ihre Lösung kurz vor.

**Code:** `src/main/kotlin/exercise/E4MonitoringDashboard.kt`
**Akzeptanztests:** `src/test/kotlin/exercise/E4MonitoringDashboardTest.kt`

```bash
./gradlew :block4-coroutines-2:exerciseTest --tests "exercise.E4*"
```

## Die Aufgabe

Aufgabe ist der Kern eines Geräte-Monitorings. Ein `DeviceGateway` liefert
Gerätestatus und Messwert-Ströme – und verhält sich dabei so, wie sich echte
Systeme verhalten: mal langsam, mal gar nicht, mal einwandfrei.

Der `MonitoringService` soll daraus etwas machen, worauf sich der Rest der
Anwendung verlassen kann.

## Die fünf Aufgaben

| # | Funktion | Worum es geht |
| --- | --- | --- |
| 1 | `report` | Eine Geräteabfrage in drei mögliche Ergebnisse überführen |
| 2 | `dashboard` | Alle Geräte gleichzeitig, Teilausfall erlaubt |
| 3 | `alerts` | Messwert-Ströme aller Geräte zu einem Alarm-Strom vereinen |
| 4 | `summarise` | Eine Zusammenfassungszeile über erschöpfendes `when` |
| 5 | `levelOf` | Auslastung auf eine Stufe abbilden |

Empfohlene Reihenfolge: **5 → 1 → 2 → 4 → 3**. Aufgabe 5 ist in zwei Minuten
erledigt und macht die erste Aufgabe sofort testbar; Aufgabe 3 ist die
interessanteste und darf in Ruhe kommen.

## Akzeptanzkriterien

Die Tests prüfen genau diese Punkte:

**Aufgabe 1 – `report`**
- Gerät antwortet → `DeviceReport.Ok` mit passender Stufe
- Gerät wirft → `DeviceReport.Unreachable` mit der Fehlermeldung
  (`"unknown error"`, wenn die Meldung `null` ist)
- Gerät ist zu langsam → `DeviceReport.TimedOut`, und zwar **nach dem
  Timeout, nicht nach der vollen Latenz** (der Test prüft `currentTime`)

**Aufgabe 2 – `dashboard`**
- Ergebnisreihenfolge = Eingabereihenfolge
- Ein defektes und ein zu langsames Gerät beeinträchtigen die anderen nicht
- Alle Geräte werden gleichzeitig abgefragt: vier Geräte, davon eines mit
  5 s Latenz, ergeben bei 500 ms Timeout genau 500 ms – nicht 5300 ms
- Leere Eingabe ergibt leere Liste

**Aufgabe 3 – `alerts`**
- Nur Messwerte ab dem Schwellwert werden zu Meldungen `"cam-04: 91.0"`
- Alle Geräte werden **gleichzeitig** beobachtet: Ein schnelles Gerät darf
  nicht auf ein langsames warten müssen
- Nichts über dem Schwellwert → leerer Strom

**Aufgabe 4 – `summarise`**
- `"5 devices: 3 ok, 1 unreachable, 1 timed out"`
- Leere Liste → `"0 devices: 0 ok, 0 unreachable, 0 timed out"`

**Aufgabe 5 – `levelOf`**
- `< 10` IDLE, `10..59` NORMAL, `60..89` HIGH, `>= 90` CRITICAL

## Stolperstellen (bewusst eingebaut)

1. **`coroutineScope` gegen `supervisorScope` in Aufgabe 2.** Wenn `report`
   jemals etwas propagiert, reißt `coroutineScope` das ganze Dashboard mit.
2. **Der Fang-Block in Aufgabe 1.** `catch (e: Exception)` schluckt auch die
   `TimeoutCancellationException` – und damit funktioniert das Timeout nicht
   mehr. Zu fangen ist der konkret erwartete Typ.
3. **`merge` gegen `flatMapConcat` in Aufgabe 3.** `flatMapConcat` arbeitet
   die Ströme nacheinander ab. Bei einem endlosen Strom kommt der zweite
   niemals an die Reihe.
4. **`count { it is … }` gegen `when` in Aufgabe 4.** Beides funktioniert,
   aber nur das erschöpfende `when` meldet sich beim Compiler, wenn eines
   Tages ein vierter Report-Typ dazukommt.

## Ausbaustufen

Optionale Erweiterungen für Gruppen, die früher fertig werden. Keine davon
wird durch Tests geprüft:

1. **Wiederholung:** `report` versucht es bei `Unreachable` einmal erneut,
   bevor es aufgibt. Wie verträgt sich das mit dem Gesamttimeout?
2. **Zwischenspeicher:** Ein erfolgreicher Report gilt 30 Sekunden.
   Woran lässt sich die Uhrzeit aufhängen, damit es testbar bleibt? (Stichwort:
   nicht `System.currentTimeMillis()` direkt aufrufen.)
3. **Gedrosselte Alarme:** Pro Gerät höchstens ein Alarm je 10 Sekunden.
   `debounce` oder `sample`? Was ist der Unterschied?
4. **Live-Dashboard:** `dashboard` als `Flow<List<DeviceReport>>`, der sich
   alle 5 Sekunden aktualisiert. `StateFlow` oder `flow { while(true) … }`?
5. **Sauberes Herunterfahren:** Ein `MonitoringService` mit eigenem Scope und
   einer `close()`-Funktion, die alles abbricht und auf das Aufräumen wartet.

## Präsentation

Fünf bis zehn Minuten je Gruppe. Interessant sind nicht die grünen Tests,
sondern:

- An welcher Stelle gab es den ersten Irrtum?
- Welche Entscheidung war nicht eindeutig, und wie wurde sie begründet?
- Was wäre bei einem Produktiveinsatz anders zu machen?
