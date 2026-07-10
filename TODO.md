# TODO

Aktualisiert: 2026-07-05

## Aktueller Stand (Kurzfassung)

- **Läuft:** Port auf NeoForge 21.10.64 abgeschlossen; sechs Einblockreaktoren; Energie
  (FE-Kabel, Akku); Strahlung; Multiblock-Reaktor mit **rechteckigen Hüllen 3–9 je Achse**
  (BFS-Erkennung, Controller an beliebiger Hüllenposition), **Energie-Ports** (einziger
  FE-Abgabepunkt) und **Item-Ports** (Brennstoff/Abfall, Modus per Rechtsklick) sowie
  **präziser Fehlerliste mit Koordinaten** (Multiblock-Phase A, 2026-07-02); eigenständiges
  **Tab-GUI** (Übersicht/Steuerung/Diagnose) mit stufenlosem **Steuerstab-Regler**,
  einstellbarer **Abschalttemperatur (50–95 %)** und Schichtansicht des Innenraums
  (Multiblock-Phase B, 2026-07-02); **Zustandsautomat** (`ReactorStatus`) mit
  **Nachzerfallswärme** nach Abschaltung und **beschädigten Kernen statt sofortiger
  Explosion** bei 100 % Hitze, reparierbar per Schraubenschlüssel (Multiblock-Phase C,
  2026-07-02, Review-Fixes bis 2026-07-03); **Bauroboter** (automatischer 3×3×3-Aufbau aus
  Inventar + Energie); **konfigurierbare Redstone-Modi (4) und Komparator-Modi (4)** im
  Reaktor-GUI (v1.2.0); **53 JUnit-5-Unit-Tests** für die Reaktorlogik.
- **Fertig (Doku war veraltet):** alle Item- und Block-Texturen sowie das Mod-Icon vorhanden;
  klassengenaue technische Referenz `docs/REFERENCE.md`.
- **Offen:** `.ogg`-Sounddateien; GameTests für `ReactorValidator`; Balance.
- **Bekannte Einschränkungen:**
  - Der **Bauroboter** baut weiterhin nur die reine 3×3×3-Casing-Hülle **ohne Ports**;
    sein „fertig"-Status toleriert den fehlenden Energie-Port bewusst — der Spieler rüstet
    Ports nach und assembliert mit dem Schraubenschlüssel.

## Empfohlene Reihenfolge

1. **Smoke-Tests** des bestehenden Stands (Abschnitt „Sofort") — Basis verifizieren.
2. ✅ **Phase A** — erledigt (2026-07-02): Ports, rechteckige Hülle 3–9, präzise Fehler.
3. ✅ **Phase B** — erledigt (2026-07-02): Steuerstab-Regler, eigenständiges Tab-GUI,
   Schichtansicht. (Der separate Redstone-Port-Block aus B2 bleibt weiterhin optional.)
4. ✅ **Phase C** — erledigt (2026-07-02, Review-Fixes bis 2026-07-03): Zustandsautomat,
   Nachzerfallswärme, beschädigte Kerne.
5. **Polish/Release** — `.ogg`-Sounds, GameTests, weiterer Unit-Test-Ausbau, Balance → v1.3.0 veröffentlichen.

## Sofort (Smoke-Tests des aktuellen Stands)

- [ ] Modularen Reaktor im Spiel mit 3×3×3-, 5×5×5- und einer rechteckigen Hülle
      (z. B. 3×4×6) testen; Controller auch an Kante/Ecke platzieren.
- [ ] Prüfen, ob Assemblierung, erneute Prüfung nach 100 Ticks und Disassemblierung
      bei beschädigter Hülle korrekt funktionieren (inkl. Fehlerliste im Chat).
- [ ] Brennstoffzufuhr und Abfallentnahme über **Item-Ports** in allen drei Modi testen
      (Hopper am Controller dürfen nichts mehr bewegen).
- [ ] FE-Ausgabe über den **Energie-Port** an Kabel, Akku und mindestens einen externen
      Verbraucher testen (Controller selbst darf kein FE mehr abgeben).
- [ ] Migration testen: alte Welt ohne Energie-Port → Reaktor disassembliert binnen ~5 s
      mit Meldung „Kein Energie-Port in der Hülle."
- [ ] Tab-GUI im Spiel prüfen: Übersicht/Steuerung/Diagnose-Tab, Steuerstab-Regler
      (0–100 %), Abschalttemperatur-Regler (50–95 %), Sicherung-überbrücken-Schalter.
- [ ] Überhitzung auf 100 % erzwingen (Sicherung AUS/AN) → beschädigte Kerne statt
      Explosion prüfen; danach abkühlen lassen und mit dem Schraubenschlüssel reparieren.
- [ ] SCRAM auslösen (Redstone-Not-Aus oder GUI-Schalter AUS) während des Betriebs und die
      Nachzerfallswärme-/Abkühlungsphase im Status-Feld beobachten.
- [ ] `./gradlew test` ausführen (52 JUnit-5-Tests) und GameTests für `ReactorValidator`
      ergänzen (siehe „Tests und Stabilität").

---

## Phase A — MVP-Lücken schließen ✅ (erledigt 2026-07-02)

*Ziel: Energie nur über Ports; Item-Ports statt Hopper-Heuristik;
rechteckige Hüllen von 3×3×3 bis 9×9×9.*

> **Abgeschlossen** (Commits `fd3fe00`, `1d10eb2`, `9a3d11c`; kommende v1.3.0).
> Einzig die GUI-Fehlerliste (letzter A4-Punkt) ist bewusst nach Phase B verschoben.

### A3 — Rechteckige Hülle (3–9 Blöcke je Achse)
- [x] `ReactorValidator.find()` auf BFS-basierte Grenzerkennung umstellen
      (statt hartkodierter 3/5/7-Würfel-Versuche).
- [x] `ReactorLayout` um `relMinX/Y/Z` und `sizeX/Y/Z` erweitern;
      `outerSize`-Feld entfernen.
- [x] Maximale Suchgrenze: 729 Blöcke (9×9×9) — `MAX_EDGE=9`, `MAX_VOLUME=729`.
- [x] Revalidierung im Tick-Loop auf gespeicherte Grenzen umstellen
      (`validateBounds`, kein `facing`-Parameter mehr nötig).
- [x] NBT-Migration: altes `ReactorSize`-Feld beim Laden erkennen und in neue
      Felder umrechnen (rückwärtskompatibel, über Blockstate-`FACING`).

### A1 — `reactor_energy_port` (neuer Block)
- [x] Block und BlockEntity registrieren (`ModBlocks`, `ModBlockEntities`).
- [x] BlockEntity delegiert FE-Zugriff an Controller-BE; kein eigener Speicher.
- [x] Controller-BE: Direktabgabe über alle 6 Seiten entfernen; FE nur noch über
      Energie-Ports (Controller hat keine Energie-Capability mehr).
- [x] `ReactorValidator` akzeptiert Energie-Port als gültigen Hüllenblock
      (und verlangt mindestens einen: `NO_ENERGY_PORT`).
- [x] Datagen: Modell, Blockstate, Loot-Tabelle, Sprache (de/en), pickaxe-Tag.

### A2 — `reactor_item_port` (neuer Block)
- [x] Block mit `BlockState`-Property `ItemPortMode` (FUEL_INPUT, WASTE_OUTPUT, DISABLED).
- [x] Rechtsklick ohne Schraubenschlüssel wechselt den Modus.
- [x] BlockEntity delegiert Slot-Zugriff an Controller-BE (Slot 0 = Brennstoff,
      Slot 1 = Abfall).
- [x] Hopper-Regelung am Controller-BE entfernen (läuft jetzt über Ports).
- [x] `ReactorValidator` akzeptiert Item-Port als gültigen Hüllenblock.
- [x] Datagen wie bei A1.

### A4 — Präzise Fehlermeldungen
- [x] `ReactorValidator` gibt Liste von `ValidationError(type, BlockPos)` zurück
      statt einzelnem Enum-Wert (max. 8 Fehler; `DISCONNECTED_PIPE` nicht-blockierend;
      Anzeige beim Wrench-Klick als Chat-Zeilen mit Koordinaten).
- [x] Sprach-Einträge: `akw.reactor.error.gap`, `akw.reactor.error.no_energy_port`,
      `akw.reactor.error.no_core`, `akw.reactor.error.disconnected_pipe`,
      `akw.reactor.error.foreign_block` (+ `akw.reactor.error.too_large`) — jeweils mit
      Koordinaten-Platzhalter; alte `akw.multiblock.error.*`-Keys entfernt.
- [ ] GUI zeigt alle Fehler in der Diagnose-Liste — **bewusst nach Phase B verschoben**
      (GUI öffnet nur bei `ASSEMBLED`; siehe B3 Diagnose-Tab).

---

## Phase B — Automation & Darstellung ✅ (erledigt 2026-07-02)

*Ziel: eigener Redstone-Port, vollständiges Controller-GUI, einstellbarer Steuerstab.*

> **Abgeschlossen** (Commit `d742818`, kommende v1.3.0). B1, B3 und B4 sind vollständig
> umgesetzt; B2 (separater Redstone-Port-Block) bleibt wie unten erläutert bewusst optional.

### B1 — Einstellbarer Steuerstabwert (0–100 %)
- [x] `controlRodInsertion` (int 0–100) in Controller-BE persistiert (NBT-Key
      `ControlRodInsertion`).
- [x] `ReactorSimulation.calculate(layout, activeCores, controlRodInsertion)`: stufenloser
      Steuerwert (`Endreaktivität = Grundreaktivität × (1 − Einschub/100)`); bei 100 %
      Einschub sinken Erzeugung und Wärme auf exakt 0 (vorher lag der Bodenwert bei `max(1, …)`).
- [x] GUI-Schieberegler (`RodSlider extends AbstractSliderButton`) im Steuerungs-Tab.

### B3 — Controller-GUI-Überarbeitung
- [x] Eigene `ModularReactorScreen`-/`ModularReactorScreenHandler`-Klassen (176×222, eigene
      Textur `modular_reactor.png`, kein Erbe von `NuclearReactorScreen` mehr).
- [x] Übersichts-Tab: Strukturgröße, Kernanzahl, FE, Erzeugung, Kühlung, Hitze, Status-Label.
- [x] Steuerungs-Tab: Ein/Aus, Steuerstab-Regler, Redstone-/Komparator-Modus,
      Abschalttemperatur (50–95 %, einstellbar), „Sicherung überbrücken"-Schalter.
- [x] Diagnose-Tab: Fehlerliste mit Koordinaten (aus A4), dauerhaft im GUI statt nur im Chat.
- [x] ContainerData auf 20 Properties erweitert (`MB_PROPERTY_COUNT`, Indizes in
      `MultiblockReactorControllerBlockEntity.IDX_*`).
- [x] GUI öffnet jetzt auch unassembliert (Diagnose-Tab zeigt die Fehlerliste); der tote
      Lang-Key `akw.multiblock.need_wrench` wurde entfernt.

### B4 — Schichtansicht Innenlayout
- [x] Grid im Diagnose-Tab: aktuelle Y-Schicht, Pfeiltasten (▲/▼) zum Wechsel.
- [x] Farbcodes: Kern = gelb, Steuerstab = blau, Kühlrohr = cyan, Blei = grau, Luft = dunkel,
      Fremdblock = rot.
- [x] Umsetzung mit `GuiGraphics.fill()`; Innenraum-Daten laufen über das
      BlockEntity-Update-Tag (`InteriorGrid`-Int-Array), nicht über ContainerData.

### B2 — `reactor_redstone_port` (neuer Block)
> **Funktional erledigt (anderer Weg), Block-Variante weiterhin nicht umgesetzt:** Die 4
> Redstone-Modi (`IGNORED`, `HIGH_ENABLES`, `HIGH_DISABLES`, `EMERGENCY_STOP`) und die 4
> Komparator-Modi (Energie / Temperatur / Brennstoff / Abfall) sind seit v1.2.0 direkt im
> Controller-GUI umschaltbar (`reactor/RedstoneMode`, `reactor/ComparatorMode`). Der
> **separate Port-Block** unten bleibt daher optional.
- [ ] (optional) Port-Block mit eigenem Modus, der in den Controller geschrieben wird.

---

## Phase C — Reaktorsicherheit & Zustandsautomat ✅ (erledigt 2026-07-02, Review-Fixes bis 2026-07-03)

*Ziel: State-Machine, Nachzerfallswärme, beschädigte Kerne, keine sofortige Explosion.*

> **Abgeschlossen** (Commit `2026efb`, Review-Fixes `13dd533`/`8096d2f`/`58a3334`/`2fd543c`/
> `ef72983`, kommende v1.3.0). Siehe CHANGELOG „Behoben" für alle nachträglich gefundenen
> und behobenen Fehler.

### C1 — Zustandsautomat
- [x] Enum `reactor/ReactorStatus`: `UNASSEMBLED`, `OFFLINE`, `STARTING`, `RUNNING`,
      `SCRAM`, `COOLDOWN`, `DAMAGED`.
- [x] Transitionen im Tick-Loop kodiert; Ad-hoc-Flags ersetzt.
- [x] NBT-Migration für Stände ohne `Status`-Key (vor Phase C): impliziter `RUNNING`-Status
      bei `burnTime>0 && activeCores>0`, sonst `UNASSEMBLED`.

### C2 — Nachzerfallswärme
- [x] Nach SCRAM: `decayHeatBase` = 20 % der letzten Kernwärme, linear auf 0
      über `DECAY_TICKS=200` fallend.
- [x] COOLDOWN → OFFLINE wenn `heat < effectiveMaxHeat()·5/100`.
- [x] Kühlung wirkt während SCRAM/COOLDOWN/DAMAGED unverändert weiter (Bugfix: skaliert mit
      der installierten Kernzahl statt mit `activeCores`, das in diesen Zuständen 0 ist).

### C3 — Beschädigte Kerne
- [x] Bei 100 % Hitze: 1–3 zufällige Kerne → `damaged_reactor_core` (`damageCores()`),
      außer die Sicherung ist überbrückt (`safetyOverride`) → dann Explosion wie zuvor.
- [x] `DamagedReactorCoreBlock`: keine Produktion, im Validator inert; Strahlung läuft über
      den Controller-Status weiter, solange er `isTooHotForRepair()` ist.
- [x] Reparatur: Schraubenschlüssel-Rechtsklick nach Abkühlung (`isTooHotForRepair()==false`)
      → normaler Kern; Rückkehr von `DAMAGED` nach `OFFLINE`, sobald kein beschädigter Kern
      mehr im Innenraum liegt.
- [x] Explosion sonst nur noch bei zerstörter Hülle ab 75 % Maximalhitze (Revalidierung)
      oder überbrückter Sicherung.

---

## Tests und Stabilität

- [x] **ContainerData-Sync auf 16 Bit begrenzt (Behoben 2026-07-10):**
      `ClientboundContainerSetDataPacket` überträgt jeden Property-Wert per
      `FriendlyByteBuf.writeShort`/`readShort` (bytecode-verifiziert gegen die
      1.21.10/NeoForge-21.10-Klasse) — Werte über 32 767 wurden als vorzeichenbehafteter
      16-Bit-Wert truncated (z. B. ergibt `100 000 mod 65536` als signed short einen
      negativen Wert). Betrifft **alle** Reaktor-GUIs, nicht nur die Phase-B/C-Änderungen:
      Energie (`IDX_ENERGY`, bis 20 Mio. FE), Kapazität (`IDX_CAPACITY`, oft weit über
      32 767), bei großen Multiblocks auch Hitze/Maximalhitze — sowohl
      `NuclearReactorScreenHandler`/`NuclearReactorScreen` (alle 6 Einblockreaktoren) als
      auch `ModularReactorScreenHandler`/`ModularReactorScreen`. Behebung durch:
      Low/High-Word-Splitting über je zwei `ContainerData`-Slots.
- [x] Unit-Tests für die Reaktorlogik: **53 JUnit-5-Tests** unter
      `src/test/java/ch/danielt/akw/reactor/` für `ReactorSimulation`, `ReactorLayout`,
      `ValidationError`, `ItemPortMode`, `RedstoneMode`, `ComparatorMode`, `ReactorStatus`
      (`build.gradle`: JUnit-5-BOM + `useJUnitPlatform()` + ModDevGradle-`unitTest{}`-Block,
      Commits `112dea4`/`9590607`).
- [ ] GameTests für `ReactorValidator` mit gültiger Hülle, Gehäuselücke, fehlendem
      Kern, Fremdblock und unverbundenem Kühlrohr (weltabhängig, weiterhin offen).
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
- [x] **Multiblock-Phase A** (2026-07-02, kommende v1.3.0): rechteckige Hüllen 3–9 je Achse
      per BFS (`ReactorValidator.find`/`validateBounds`), Controller an beliebiger
      Hüllenposition; `reactor_energy_port` als einziger FE-Abgabepunkt (Controller ohne
      Energie-Capability); `reactor_item_port` mit Modus-Property (Brennstoff-Eingang /
      Abfall-Ausgang / Deaktiviert, Rechtsklick schaltet um); Fehlerliste `ValidationError`
      (6 Typen, max. 8 Fehler, Koordinaten, Chat-Ausgabe beim Wrench-Klick); NBT-Migration
      vom alten `ReactorSize`-Format.
- [x] **Multiblock-Phase B** (2026-07-02, kommende v1.3.0): eigenständiges Tab-GUI
      (`ModularReactorScreenHandler`/`ModularReactorScreen`, 176×222, drei Tabs), stufenloser
      Steuerstab-Regler 0–100 % (`ReactorSimulation.calculate` um `controlRodInsertion`
      erweitert), einstellbare Abschalttemperatur 50–95 %, Schichtansicht des Innenraums mit
      Farbcodes; ContainerData auf 20 Properties erweitert; GUI öffnet jetzt auch
      unassembliert (toter Lang-Key `akw.multiblock.need_wrench` entfernt).
- [x] **Multiblock-Phase C** (2026-07-02, Review-Fixes bis 2026-07-03, kommende v1.3.0):
      Zustandsautomat `ReactorStatus` (UNASSEMBLED/OFFLINE/STARTING/RUNNING/SCRAM/COOLDOWN/
      DAMAGED), Nachzerfallswärme nach Abschaltung (20 % der letzten Kernwärme, 200 Ticks
      linear abklingend), beschädigte Kerne (`damaged_reactor_core`) statt sofortiger
      Explosion bei 100 % Hitze — Reparatur per Schraubenschlüssel nach Abkühlung; Explosion
      nur noch bei überbrückter Sicherung oder bei ≥75 % Maximalhitze zerstörter Hülle.
      Adversarialer Review fand und behob danach 9 weitere Fehler (DAMAGED-Sackgasse,
      Kühlungsausfall bei SCRAM, Client-GUI-Desync u. a. — siehe CHANGELOG).
- [x] **Unit-Tests für die Reaktorlogik** (2026-07-02, kommende v1.3.0): 52 JUnit-5-Tests
      (`ReactorSimulation`, `ReactorLayout`, `ValidationError`, `ItemPortMode`,
      `RedstoneMode`, `ComparatorMode`, `ReactorStatus`); `build.gradle` um JUnit-5-BOM und
      ModDevGradle-`unitTest{}`-Block erweitert.
