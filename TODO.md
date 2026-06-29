# TODO

Aktualisiert: 2026-06-29

## Aktueller Stand (Kurzfassung)

- **Läuft:** Port auf NeoForge 21.10.64 abgeschlossen; sechs Einblockreaktoren; Energie
  (FE-Kabel, Akku); Strahlung; MVP-Multiblock-Reaktor (3×3×3/5×5×5/7×7×7, Energie über alle
  6 Seiten, Hopper-Brennstoff/-Abfall, Auto-Abschaltung); **Bauroboter** (automatischer
  3×3×3-Aufbau aus Inventar + Energie); **konfigurierbare Redstone-Modi (4) und
  Komparator-Modi (4)** im Reaktor-GUI (v1.2.0).
- **Fertig (Doku war veraltet):** alle Item- und Block-Texturen sowie das Mod-Icon vorhanden;
  klassengenaue technische Referenz `docs/REFERENCE.md`.
- **Offen:** `.ogg`-Sounddateien; Multiblock-Phasen A–C (Ports, rechteckige Hülle, präzise
  Fehler, Steuerstab-Regler, eigenständiges Controller-GUI, Zustandsautomat); Unit-/GameTests;
  Balance.

## Empfohlene Reihenfolge

1. **Smoke-Tests** des bestehenden MVP (Abschnitt „Sofort") — Basis verifizieren.
2. **Phase A** — MVP-Lücken schließen (Ports, rechteckige Hülle 3–9, präzise Fehler).
3. **Phase B** — Steuerstab-Regler, Controller-GUI, Redstone-Port.
4. **Phase C** — Zustandsautomat, Nachzerfallswärme, beschädigte Kerne.
5. **Polish/Release** — `.ogg`-Sounds, Unit-/GameTests, Guide/ROADMAP nachziehen → v1.0.0.

## Sofort (Smoke-Tests des bestehenden MVP)

- [ ] Modularen Reaktor im Spiel mit 3×3×3-, 5×5×5- und 7×7×7-Hülle testen.
- [ ] Prüfen, ob Assemblierung, erneute Prüfung nach 100 Ticks und Disassemblierung
      bei beschädigter Hülle korrekt funktionieren.
- [ ] Brennstoffzufuhr von oben und Abfallentnahme von unten mit Hoppern testen.
- [ ] FE-Ausgabe an Kabel, Akku und mindestens einen externen Verbraucher testen.

---

## Phase A — MVP-Lücken schließen

*Ziel: Energie nur über Ports; Item-Ports statt Hopper-Heuristik;
rechteckige Hüllen von 3×3×3 bis 9×9×9.*

### A3 — Rechteckige Hülle (3–9 Blöcke je Achse)
- [ ] `ReactorValidator.find()` auf BFS-basierte Grenzerkennung umstellen
      (statt hartkodierter 3/5/7-Würfel-Versuche).
- [ ] `ReactorLayout` um `relMinX/Y/Z` und `sizeX/Y/Z` erweitern;
      `outerSize`-Feld entfernen.
- [ ] Maximale Suchgrenze: 729 Blöcke (9×9×9).
- [ ] Revalidierung im Tick-Loop auf gespeicherte Grenzen umstellen
      (kein `facing`-Parameter mehr nötig).
- [ ] NBT-Migration: altes `ReactorSize`-Feld beim Laden erkennen und in neue
      Felder umrechnen (rückwärtskompatibel).

### A1 — `reactor_energy_port` (neuer Block)
- [ ] Block und BlockEntity registrieren (`ModBlocks`, `ModBlockEntities`).
- [ ] BlockEntity delegiert FE-Zugriff an Controller-BE; kein eigener Speicher.
- [ ] Controller-BE: Direktabgabe über alle 6 Seiten entfernen; FE nur noch über
      Energie-Ports.
- [ ] `ReactorValidator` akzeptiert Energie-Port als gültigen Hüllenblock.
- [ ] Datagen: Modell, Blockstate, Loot-Tabelle, Sprache (de/en), pickaxe-Tag.

### A2 — `reactor_item_port` (neuer Block)
- [ ] Block mit `BlockState`-Property `ItemPortMode` (FUEL_INPUT, WASTE_OUTPUT, DISABLED).
- [ ] Rechtsklick ohne Schraubenschlüssel wechselt den Modus.
- [ ] BlockEntity delegiert Slot-Zugriff an Controller-BE (Slot 0 = Brennstoff,
      Slot 1 = Abfall).
- [ ] Hopper-Regelung am Controller-BE entfernen (läuft jetzt über Ports).
- [ ] `ReactorValidator` akzeptiert Item-Port als gültigen Hüllenblock.
- [ ] Datagen wie bei A1.

### A4 — Präzise Fehlermeldungen
- [ ] `ReactorValidator` gibt Liste von `ValidationError(type, BlockPos)` zurück
      statt einzelnem Enum-Wert.
- [ ] Sprach-Einträge: `akw.reactor.error.gap`, `akw.reactor.error.no_energy_port`,
      `akw.reactor.error.no_core`, `akw.reactor.error.disconnected_pipe`,
      `akw.reactor.error.foreign_block` — jeweils mit Koordinaten-Platzhalter.
- [ ] GUI zeigt alle Fehler in der Diagnose-Liste.

---

## Phase B — Automation & Darstellung

*Ziel: eigener Redstone-Port, vollständiges Controller-GUI, einstellbarer Steuerstab.*

### B1 — Einstellbarer Steuerstabwert (0–100 %)
> **Teils erledigt:** Die Konzept-Formel ist in `reactor/ReactorSimulation.java` bereits live
> (`reactivity = min(1.0, 0.60 + 0.10·Nachbarkerne)`, `Wärme = 12·reactivity²·heatFactor`,
> `heatFactor` aus angrenzenden Steuerstäben). Offen ist nur der **stufenlose Regler 0–100 %**
> (aktuell wirken Steuerstäbe nur diskret über Nachbarschaft).
- [ ] `controlRodInsertion` (int 0–100) in Controller-BE persistieren.
- [ ] `ReactorSimulation` um den stufenlosen Steuerwert ergänzen
  (`Endreaktivität = Grundreaktivität × (1 − Steuerwirkung)`).
- [ ] GUI-Schieberegler im Screen-Handler + Screen.

### B3 — Controller-GUI-Überarbeitung
- [ ] Eigene `ModularReactorScreen`-Klasse (statt `NuclearReactorScreen`).
- [ ] Übersichts-Tab: Strukturgröße, Kernanzahl, FE, FE/t, Temperatur, HU/t,
      Kühlleistung, Brennstoff-/Abfallfüllstand, Status-Label.
- [ ] Steuerungs-Tab: Ein/Aus, Steuerstab-Regler, Redstone-Modus,
      Abschalttemperatur.
- [ ] Diagnose-Tab: Fehlerliste mit Koordinaten (aus A4).
- [ ] Screen-Handler-Properties um `coreCount`, `productionPerTick`,
      `coolingCapacity`, `controlRodInsertion` erweitern.

### B4 — Schichtansicht Innenlayout
- [ ] Einfaches Grid im Diagnose-Tab: aktuelle Y-Schicht, Pfeiltasten zum Wechsel.
- [ ] Farbcodes: Kern = gelb, Steuerstab = blau, Kühlrohr = cyan, Blei = grau, Luft = leer.
- [ ] Umsetzung mit `DrawContext.fill()`.

### B2 — `reactor_redstone_port` (neuer Block)
> **Funktional erledigt (anderer Weg):** Die 4 Redstone-Modi (`IGNORED`, `HIGH_ENABLES`,
> `HIGH_DISABLES`, `EMERGENCY_STOP`) und die 4 Komparator-Modi (Energie / Temperatur /
> Brennstoff / Abfall) sind seit v1.2.0 direkt im Controller-GUI umschaltbar
> (`reactor/RedstoneMode`, `reactor/ComparatorMode`). Der **separate Port-Block** unten ist
> daher nur noch optional (für portgebundene Multiblock-Steuerung).
- [ ] (optional) Port-Block mit eigenem Modus, der in den Controller geschrieben wird.

---

## Phase C — Reaktorsicherheit & Zustandsautomat

*Ziel: State-Machine, Nachzerfallswärme, beschädigte Kerne, keine sofortige Explosion.*

### C1 — Zustandsautomat
- [ ] Enum `ReactorStatus`: `UNASSEMBLED`, `OFFLINE`, `STARTING`, `RUNNING`,
      `SCRAM`, `COOLDOWN`, `DAMAGED`.
- [ ] Transitionen im Tick-Loop kodieren; Ad-hoc-Flags ersetzen.

### C2 — Nachzerfallswärme
- [ ] Nach SCRAM: `decayHeat` = 20 % der letzten Kernwärme, linear auf 0
      über 200 Ticks fallend.
- [ ] COOLDOWN → OFFLINE wenn `heat < 5 %`.

### C3 — Beschädigte Kerne
- [ ] Bei 100 % Hitze: 1–3 zufällige Kerne → `damaged_reactor_core`.
- [ ] `DamagedReactorCoreBlock`: keine Produktion, kleine Strahlungsquelle.
- [ ] Reparatur: Schraubenschlüssel-Rechtsklick nach Abkühlung → normaler Kern.
- [ ] Explosion nur bei `safetyOverride = true` (GUI-Checkbox) oder zerstörter Hülle.

---

## Tests und Stabilität

- [ ] Unit-Tests für `ReactorSimulation` ergänzen:
  - einzelner Kern ohne Kühlung,
  - gekühlter Vierkernreaktor,
  - Steuerstab-Wärmereduktion,
  - Teilbetrieb bei zu wenig Brennstäben,
  - Kapazitäts- und Temperaturgrenzen.
- [ ] GameTests für `ReactorValidator` mit gültiger Hülle, Gehäuselücke, fehlendem
      Kern, Fremdblock und unverbundenem Kühlrohr.
- [ ] NBT-Neuladen während eines laufenden Brennzyklus testen.
- [ ] Verhalten bei vollem Abfallslot und vollem Energiespeicher testen.
- [ ] Sicherstellen, dass Energie, Hitze und Inventar niemals ungültige oder negative
      Werte annehmen.

---

## Sounds & Assets

- [ ] **`.ogg`-Sounddateien erstellen und einbinden.** `assets/akw/sounds.json` definiert
      bereits drei Events (`reactor_ambient`, `reactor_alert`, `reactor_meltdown`), aber die
      `sounds`-Arrays sind leer und es gibt kein `assets/akw/sounds/`-Verzeichnis.
- [ ] Nach dem Hinzufügen: Events in den Arrays von `sounds.json` referenzieren und im Spiel
      prüfen (Ambient während Betrieb, Alert bei Überhitzung, Meltdown bei Explosion).

---

## Bestehende Einblockreaktoren

- [ ] Reaktor, Fortgeschrittener Reaktor, Brutreaktor, Thoriumreaktor, Elitereaktor
      und Fusionsreaktor weiterhin gemeinsam testen.
- [ ] Keine vorhandene Block-ID und kein veröffentlichtes Rezept entfernen.
- [ ] Einheitliche Status- und Sicherheitsmeldungen für alle Einblockreaktoren ergänzen.

---

## Balance

- [ ] Referenzaufbauten 3×3×3, 5×5×5 und 5×5×7 praktisch vergleichen.
- [ ] Brennstoffeffizienz des Multiblocks gegen alle sechs Einblockreaktoren prüfen.
- [ ] Verhindern, dass ein Aufbau gleichzeitig höchste Leistung, Effizienz und
      Sicherheit erreicht.
- [ ] Grenzwerte für maximale Kernanzahl, Inventarstapel und FE-Ausgabe festlegen.
- [ ] Balancewerte später über eine Konfiguration einstellbar machen.

---

## Bedienung und Darstellung (spätere Ausbaustufe)

- [ ] Sounds und Partikel nach Zustand und Leistung skalieren (bereits teilweise vorhanden).
- [ ] Tooltips für Reaktivität, Kühlkontakte und Sicherheitsstatus ergänzen.
- [ ] In-Game-Handbuch mit mindestens drei Beispielreaktoren (3×3×3, 5×5×5, 5×5×7).

---

## Projektpflege

- [ ] OneDrive-Diagnose aus
      [`docs/ONEDRIVE-GRADLE-PROBLEM.md`](docs/ONEDRIVE-GRADLE-PROBLEM.md) abarbeiten.
- [ ] Alte ignorierte `.gradle`-, `build`- und `run`-Ordner im OneDrive-Projekt nach
      erfolgreichem Test des neuen Wrappers löschen.
- [ ] Doppelte Arbeitskopien und alte Worktree-Ordner außerhalb des aktiven Projekts
      archivieren.
- [ ] Nach jeder Datagen-Änderung `runClientData` **und** `runServerData` (data/ dazwischen
      sichern, siehe `docs/NEOFORGE-MIGRATION.md` §6) und anschließend `build` ausführen.
- [ ] Roadmap und Spieler-Guide nach jeder abgeschlossenen Multiblock-Phase aktualisieren.

---

## Bereits erledigt

- [x] **Port auf NeoForge 21.10.64** abgeschlossen (Runtime + Datagen, siehe
      `docs/NEOFORGE-MIGRATION.md` und CHANGELOG 1.1.0).
- [x] **Texturen vollständig**: alle 6 Item- und 38 Block-Texturen vorhanden.
- [x] **Mod-Icon** vorhanden (`src/main/resources/assets/akw/icon.png`, 512×512).
- [x] OneDrive-Modus ergänzen; Quellen bleiben im Repository, Gradle-Cache und
      Buildausgabe liegen lokal.
- [x] `tools/prepare-onedrive.ps1` ausgeführt.
- [x] `org.gradle.java.home` plattformunabhängig gemacht.
- [x] `src/main/generated/.cache` bleibt unversioniert.
- [x] Vollständiger Build direkt aus dem OneDrive-Repository erfolgreich.
- [x] Sechs veröffentlichte Einblockreaktoren bleiben registriert und unverändert.
- [x] Modularer Innenraum erkennt Kerne, Steuerstäbe, Kühlrohre, Blei und Fremdblöcke.
- [x] Reaktorleistung und Energiespeicher skalieren mit eingebauten Kernen.
- [x] Kühlrohrnetze benötigen eine Verbindung zur Außenhülle.
- [x] Multiblock besitzt Brennstoff- und Abfallslot mit Hopper-Regeln.
- [x] Verbrauchte Brennstäbe entstehen pro aktiviertem Kern.
- [x] Automatische Abschaltung bei 90 Prozent Maximaltemperatur ist vorhanden.
- [x] Deutsche und englische Assemblierungsfehler sind vorhanden.
- [x] Compile, Datagen und vollständiger Build waren außerhalb von OneDrive erfolgreich.
- [x] **Bauroboter** (`reactor_builder_controller`, v1.2.0): baut den 3×3×3-Multiblock
      automatisch aus Inventar + Energie (500 FE/Block), sichtbarer Roboter, Komparator-Fortschritt.
- [x] **Konfigurierbare Redstone-Modi (4)** im Reaktor-GUI (v1.2.0): Ignoriert /
      Signal aktiviert / Signal deaktiviert / Not-Aus (SCRAM).
- [x] **Konfigurierbare Komparator-Modi (4)** im Reaktor-GUI (v1.2.0): Energie /
      Temperatur / Brennstoff / Abfall.
- [x] **Technische Referenz** `docs/REFERENCE.md` (klassengenau) erstellt (v1.2.0).
