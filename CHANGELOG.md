# Changelog

Alle nennenswerten Änderungen an der AKW-Mod werden hier dokumentiert.

Format orientiert sich an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
Versionierung nach [Semantic Versioning](https://semver.org/lang/de/).

## [1.0.0] — 2026-06-20

### Release
- Erster stabiler Release des Atomkraftwerk-Mods.
- Vollständiger Gameplay-Loop: Uranabbau → Anreicherung → Reaktor → Energie → Abfallmanagement.
- README vollständig überarbeitet: korrekte Feature-Liste, Reaktor-Werte, Installation, Projektstruktur.
- `fabric.mod.json`: GitHub-Links in `contact`-Feld eingetragen.
- ROADMAP Phase 5 abgeschlossen.

---

## [0.9.5] — 2026-06-20

### Hinzugefügt
- **Sound-Registrierung** (`ModSounds`): drei Sound-Events registriert —
  `reactor_ambient`, `reactor_alert`, `reactor_meltdown`. Infrastruktur ist bereit;
  Sounds werden stumm gespielt bis `.ogg`-Dateien in `assets/akw/sounds/` abgelegt werden.
  `sounds.json` mit Subtitle-Verweisen angelegt.
- **Strahlungspartikel** (vanilla `ELECTRIC_SPARK`): Reaktoren (Standard + Multiblock)
  spawnen alle 10 Ticks grüne Funken-Partikel über dem Block, solange ein Brennstab brennt.
- **Abfallbehälter-Partikel** (vanilla `GLOW_SQUID_INK`): bei Füllstand > 50 %
  alle 20 Ticks Warnsignalpartikel.
- **Advancement-Kette** (9 Stufen via `ModAdvancementProvider`, Datagen):
  Uranabbau → Schmelzen → Anreicherung → Brennstab → Erster Reaktor → Energie online
  → (Elite-Reaktor / Fusionsreaktor / Multiblock-Meister als Challenges).
- Sound-Untertitel + Advancement-Übersetzungen in DE und EN.

---

## [0.8.0] — 2026-06-20

### Hinzugefügt
- **Komparator-Output** für alle Reaktor-Typen (Standard + Multiblock): Komparator
  liest den Energie-Füllstand als Signal 0–15 aus — 0 = leer, 15 = voll.
- **Redstone pausiert Reaktor** (neues Property `POWERED`): Redstone-Signal am
  Reaktor-Block stoppt das Zünden neuer Brennstäbe. Der laufende Stab brennt noch
  ab, danach geht der Reaktor sanft in Standby.
  Praxisnutzen: Komparator-Signal „voll" → Leitung → benachbarter Reaktor pausiert
  → automatische Lastverteilung.
- **Abfallbehälter** (`waste_container`) ist jetzt ein vollständiger Speicher-Block:
  - 9 Slots für Verbrauchte Brennstäbe (GUI öffnet sich per Rechtsklick)
  - Komparator-Output: Füllstand 0–15
  - Hopper von oben/seitig → Einlagern (nur Verbrauchte Brennstäbe)
  - Hopper von unten → Entnehmen
  - Passive Strahlung (Level 0) bei Füllstand > 50 % in 5-Block-Radius
    (Blei-Block auf dem Pfad schützt vollständig)
- Pfadbasierter Strahlungsschutz auch im **Multiblock-Reaktor** nachgezogen
  (war bisher nur im Standard-Reaktor implementiert).

### Technisch
- `NuclearReactorBlock` + `MultiblockReactorControllerBlock`: `hasComparatorOutput`,
  `getComparatorOutput`, `neighborUpdate` (POWERED-Property).
- `WasteContainerBlock` + `WasteContainerBlockEntity` neu (SidedInventory,
  NamedScreenHandlerFactory → GenericContainerScreenHandler).
- Blockstate-JSONs aller 6 Reaktortypen um `powered=false/true` erweitert.
- `NuclearReactorBlockEntity.hasLeadShielding` ist jetzt package-private
  (von WasteContainerBlockEntity wiederverwendet).

---

## [0.7.0] — 2026-06-20

### Hinzugefügt
- **Verbrauchter Brennstab** (`spent_fuel_rod`): Wenn ein Brennstab im Reaktor
  vollständig verbrannt ist, erscheint automatisch ein Verbrauchter Brennstab im
  neuen Abfall-Slot (rechts neben dem Brennstoff-Slot im GUI).
- **Abfall-Slot im Reaktor-GUI**: Neuer Output-Slot (Slot 1). Solange der
  Abfall-Slot keinen Platz mehr hat (max. 64 Stäbe), lädt der Reaktor keinen
  neuen Brennstab — Wartungsloop erzwungen.
- **Hopper-Kompatibilität** (via `SidedInventory`):
  - Hopper von **oben** → befüllt den Brennstoff-Slot (nur Brennstäbe akzeptiert).
  - Hopper von **unten** → entnimmt verbrauchte Brennstäbe aus dem Abfall-Slot.
  - Seitliche Hopper haben keinen Zugriff.
- **Verbesserte Strahlungsabschirmung**: Blei-Block schützt jetzt pfadbasiert —
  jeder Blei-Block auf der direkten Linie zwischen Reaktor und Spieler blockt
  die Strahlung vollständig (früher: nur direkt angrenzender Blei-Block).

### Technisch
- `NuclearReactorBlockEntity` implementiert nun `SidedInventory` (Fabric/MC-Standard).
- Inventargröße: 1 → 2 Slots (abwärtskompatibel: bestehende Saves laden korrekt).

---

## [0.6.0] — 2026-06-19

### Hinzugefügt
- **Multiblock-Reaktor-System:** Zwei neue Blöcke ermöglichen Reaktoren, die mehrere
  Blöcke groß sind.
  - `Reaktor-Gehäuse` — Wandblock (Eisen + Blei-Block, 4 Stück pro Rezept); hart und
    explosionsresistent.
  - `Multiblock-Reaktor-Controller` — Steuerblock mit GUI, FACING-Ausrichtung und
    ASSEMBLED-Status. Rezept: 8× Reaktor-Gehäuse + 1× Reaktionsblöcke + Redstone.
- **Reaktor-Schraubenschlüssel** — Werkzeug zum Assemblieren/Disassemblieren.
  Rezept: 2× Eisen-Barren + 1× Stab.
- **Automatische Größenerkennung:** 3×3×3, 5×5×5 oder 7×7×7 (Außenmaß). Leistung
  skaliert mit dem Innenvolumen (outerSize−2)³:
  - 3×3×3: 50 FE/Tick, 200 000 FE Kapazität
  - 5×5×5: 1 350 FE/Tick, 5,4 Mio. FE Kapazität
  - 7×7×7: 6 250 FE/Tick, 25 Mio. FE Kapazität
- **Struktur-Revalidierung:** Alle 100 Ticks wird die Struktur geprüft — werden
  Gehäuse-Blöcke entfernt, deaktiviert sich der Reaktor automatisch.
- Strahlung + Überhitzungsexplosion (bricht alle Gehäuse-Blöcke) auch für Multiblock.
- I18n-Schlüssel für alle Feedback-Nachrichten (de_de + en_us).

---

## [0.5.1] — 2026-06-19

### Hinzugefügt
- **Angereichertes Uran** (`enriched_uranium`): Neues Zwischenprodukt in der
  Uran-Verarbeitungskette. Rezept: 2 Uran-Barren → 1 Angereichertes Uran.
  Brennstab benötigt jetzt 3× Angereichertes Uran statt Uran-Barren (doppelt
  so teuer, realistischerer Anreicherungsprozess).
- **Textur** für Angereichertes Uran: leuchtend gelblich-grüner Stil (abgeleitet
  vom Uran-Barren, mit Energie-Highlights).

---

## [0.5.0] — 2026-06-19

### Hinzugefügt
- **Steuerstab-Logik:** Jeder direkt angrenzende `Steuerstab-Block` reduziert den
  Hitzeaufbau des Reaktors um 4 Wärme/Tick (bis zu 6 Stäbe = 24 Reduktion).
  Beispiel: Basis-Reaktor (6 Hitze/Tick) + 2 Steuerstäbe = nur noch 0 Aufbau netto
  (vor Kühlung).
- **Strahlung (Radiation):** Laufende Reaktoren bestrahlen Spieler im Radius von
  8 Blöcken. Strahlung I bei niedrigem Hitze-Level, Strahlung II ab 50% der
  Maximaltemperatur. Schaden: 0,5 HP/s bzw. 1,5 HP/s. **Blei-Block** direkt neben
  dem Spieler blockiert die Strahlung vollständig.
- **Status-Effekt „Strahlung"** (`effect.akw.radiation`) — grünes Effekt-Icon im
  Spieler-HUD, sichtbare Partikel.

---

## [0.4.1] — 2026-06-19

### Hinzugefügt
- **CI-Pipeline** (`.github/workflows/build.yml`) — GitHub Actions führt bei jedem
  Push automatisch `./gradlew build` aus; fehlgeschlagene Builds blockieren Merges.
  Gebaute JARs werden als Artefakt hochgeladen.
- **Diagnose-Logging** in `ModItemGroups.registerAll()` — im Log erscheint jetzt
  sowohl der Start als auch etwaige Fehler der Kreativ-Tab-Registration (try/catch
  mit explizitem `LOGGER.error` vor dem Weiterwerfen der Exception).

---

## [0.4.0] — 2026-06-19

### Geändert
- **Kreativ-Tab: idiomatische RegistryKey-Verwendung** — `RegistryKeys.ITEM_GROUP`
  statt `Registries.ITEM_GROUP.getKey()`, entspricht dem offiziellen Fabric-API-Beispiel
  (beide liefern denselben Wert, aber `RegistryKeys.*` ist die empfohlene Konstante).
- **Hinweis zur Tab-Navigation:** Der AKW-Tab erscheint bei mehreren installierten
  Mods auf Seite 2 des Kreativ-Menüs. Mit dem **„>>"**-Button (oben rechts neben den
  Tab-Icons, eingeblendet von Fabric API) gelangt man zu Mod-Tabs — dies ist
  Fabric-Standard-Verhalten (Pagination), kein Bug.

---

## [0.3.7] — 2026-06-18

### Dokumentation
- **ROADMAP aktualisiert:** Status auf v0.3.7, Energie-Kabel & Akku-Block als
  abgeschlossen markiert, Phase 3 als ausgeliefert gekennzeichnet.

---

## [0.3.6] — 2026-06-18

### Behoben
- **Kreativ-Tab fehlte im Spiel:** Items wurden über `ItemGroupEvents` in den Tab
  eingetragen, was für frisch registrierte Custom-Tabs in Fabric API 0.138 nicht
  zuverlässig feuert. Fix: Items direkt im Builder via `.entries(...)` eingetragen.

---

## [0.3.5] — 2026-06-18

### Hinzugefügt
- **Kreativ-Tab „Atomkraftwerk" stabil & dokumentiert:** Alle Items und Blöcke sind
  per `ItemGroupEvents.modifyEntriesEvent` im eigenen Tab eingetragen. Neue Reaktoren
  und Dekor-Blöcke erscheinen automatisch, sobald sie über `registerReactor()` /
  `registerDecor()` in `ModBlocks` registriert sind (kein manueller Nachpflege-Schritt).
- **Vanilla-Tab-Einträge:** Items und Blöcke zusätzlich in die passenden Vanilla-Tabs
  einsortiert (Zutaten, Naturblöcke, Funktionsblöcke, Baublöcke, Redstone).

### Geändert
- Version in `gradle.properties` und `README.md` auf 0.4.0 angehoben.
- Bekannte Lücken im README aktualisiert (Steuerstab-Wirkung → v0.5 verschoben).

---

## [0.3.0] — 2026-06-18

### Hinzugefügt
- **Energie-Infrastruktur (v0.3):** **Energie-Kabel** transportiert FE zwischen
  Blöcken (kleiner Puffer, gibt pro Tick an alle Nachbarn weiter → FE fliesst entlang
  der Strecke von Erzeugern zu Verbrauchern/Speichern). **Akku-Block** als grosser
  FE-Puffer (1 Mio FE), der den Füllstand als **Komparator-Signal** (0–15) ausgibt.
  Beide exponieren `EnergyStorage.SIDED` → automatisch **FE-kompatibel mit Create**
  (via FE-Brücken). Modularer Kern: gemeinsame Push-Logik in `energy/EnergyNet`
  herausgezogen, Reaktor darauf umgestellt.
- **Kühlsystem & Hitze-Mechanik:** Reaktoren bauen im Betrieb Hitze auf
  (`heatPerTick` je Typ). Jedes direkt angrenzende **Kühlrohr** (`cooling_pipe`)
  senkt die Hitze um 8/Tick, dazu 2/Tick Eigenkühlung. Ab 75 % der maxHitze wird
  die Energie-Erzeugung auf ¼ gedrosselt; bei Erreichen der maxHitze **explodiert**
  der Reaktor (Stärke skaliert mit dem Typ). Neuer **Hitzebalken** im GUI
  (orange → rot ab Drosselung) mit `Hitze / maxHitze`-Tooltip; Hitze wird per NBT
  gespeichert. Tier-Werte (`maxHeat`, `heatPerTick`) in `ModBlocks` gepflegt.

### Geändert
- **Upgrade auf Minecraft 1.21.10** (von 1.21.1): Loom 1.17, Gradle 9.5,
  yarn 1.21.10+build.3, loader 0.19.3, fabric-API 0.138.4. Angepasst an die neue
  Registry- (`Settings.registryKey`), NBT- (`Read-/WriteView`), Modell-
  (`client.data`, `registerCooker`), Recipe- (`RecipeGenerator`) und
  Render-API (`drawTexture` mit `RenderPipelines`).
- **Datagen von Python auf Java (Fabric Datagen)** umgestellt; `tools/akw_data.py`
  & Co. entfallen. Ausgabe nach `src/main/generated` (eigene, committete
  Ressourcen-Wurzel), damit der Fabric-Cleanup keine handgepflegten Dateien in
  `src/main/resources` löscht. Ausführen: `./gradlew runDatagen`.

### Dokumentation
- **JavaDoc vervollständigt:** Klassen-Doc für alle bislang undokumentierten Klassen
  (`AkwMod`, `AkwClient`, `ModItemGroups`, `ModScreenHandlers`, `NuclearReactorScreen`).
- **Entwickler-Guide** `docs/ARCHITECTURE.md`: Paketstruktur, datengetriebene
  Asset-Pipeline (`akw_data.py` → Generatoren → Java-Spiegelung), Energiefluss,
  GUI-Kette, Anleitung „neuen Reaktor-Typ hinzufügen", Build-/Validierungs-Hinweise.
- **Spieler-Guide** `docs/GUIDE.md`: Progression, Reaktor-Bedienung, Vergleichstabelle
  aller Reaktor-Typen und sämtliche Rezepte.
- README um einen Dokumentations-Abschnitt mit Querverweisen erweitert.

## [0.2.0] — Energiesystem & Reaktoren

### Hinzugefügt
- **6 funktionale Reaktor-Typen** als FE-Generatoren (Team Reborn Energy):
  `nuclear_reactor`, `advanced_nuclear_reactor`, `elite_nuclear_reactor`,
  `breeder_reactor`, `thorium_reactor`, `fusion_reactor`. Jeder Typ hat eigene
  Kapazität/Leistung/Brenndauer (siehe `README.md`).
  - **BlockEntity** (`NuclearReactorBlockEntity`): verbrennt `fuel_rod`, erzeugt
    FE/Tick, gibt Strom über alle 6 Seiten an angrenzende FE-Speicher ab,
    speichert Energie/Brennzustand per NBT, droppt Inhalt beim Abbau.
  - **GUI** (`NuclearReactorScreenHandler` + `NuclearReactorScreen`):
    Brennstoff-Slot, Energiebalken mit `FE / Kapazität`-Tooltip, Brenn-Anzeige;
    Synchronisation per `PropertyDelegate`, Öffnen via `ExtendedScreenHandlerType`
    (BlockPos-Sync).
  - **Blockstates** `facing` (horizontale Ausrichtung) + `lit` (leuchtende Front
    im Betrieb), parametrisierte Modelle (orientable, `_on`-Variante).
- **6 Reaktor-Bausteine:** `reactor_core`, `control_rod_block`, `cooling_pipe`,
  `lead_block`, `waste_container`, `enriched_uranium_block` (mit Texturen,
  Modellen, Loot, gestaffelten Crafting-Rezepten, Lokalisierung).
- **Generatoren** (`tools/`): zentrale Datenquelle `akw_data.py`,
  `gen_textures.py` (Block-/GUI-Texturen) und `gen_resources.py`
  (Blockstates/Modelle/Loot/Rezepte/Tags/Lang) — reine Python-stdlib.
- **Energie-Lookup:** `EnergyStorage.SIDED` für den Reaktor-BlockEntity-Typ.

### Geändert
- Mod-Version auf `0.2.0` angehoben.
- Rezept-Result-Format auf MC 1.21.1 korrigiert (`"result": {"id": …}` statt
  `"item"`/String) — betrifft `fuel_rod` sowie Schmelz-/Schmelzofen-Rezepte.
- `mineable/pickaxe`-Tag um alle Reaktoren und Bausteine erweitert.

### Verifiziert
- `./gradlew build` läuft fehlerfrei; headless `runServer` lädt alle Registries,
  1305 Rezepte und Loot-Tables ohne AKW-bezogene Fehler.

## [0.1.5] — Texturen & Weltgenerierung

### Hinzugefügt
- **Texturen:** Pixel-Art-Texturen für alle Items (`raw_uranium`,
  `uranium_ingot`, `fuel_rod`) und Blöcke (`uranium_ore`,
  `deepslate_uranium_ore`) sowie ein Mod-Icon (`icon.png`, 512×512,
  in `fabric.mod.json` eingebunden). Generiert via `tools/gen_textures.py`
  (stdlib-PNG-Encoder, keine externen Abhängigkeiten).
- **Erz-Weltgenerierung:** Uranerz spawnt natürlich in allen Overworld-Biomen
  (Stein + Tiefenschiefer, y -64…32) via Configured/Placed Feature + Fabric
  `BiomeModifications` (`ModWorldGen`).
- **Loot-Tables** für `uranium_ore` und `deepslate_uranium_ore`
  (droppen Roh-Uran; Silk Touch → Block, Fortune erhöht Drop).
- **Mining-Tags:** Erze in `mineable/pickaxe` und `needs_iron_tool`
  (Abbau erfordert mindestens eine Eisen-Spitzhacke).
- **Rezepte:**
  - Schmelzen `raw_uranium` → `uranium_ingot` (Ofen, 200 Ticks).
  - Schmelzen `raw_uranium` → `uranium_ingot` (Schmelzofen, 100 Ticks).
  - Crafting 3× `uranium_ingot` → `fuel_rod`.
- **Dokumentation:** `README.md`, `CHANGELOG.md`, `ROADMAP.md`.
- **`LICENSE`** (MIT) — schließt die Referenz in `build.gradle`.
- **`briefkasten/`** Austausch-Ordner (`eingang/`, `ausgang/`).

## [0.1.0] — Grundgerüst

### Hinzugefügt
- Mod-Initialisierung mit Registry-System (`AkwMod`, `AkwClient`).
- Items: `raw_uranium`, `uranium_ingot`, `fuel_rod`.
- Blöcke: `uranium_ore`, `deepslate_uranium_ore` (inkl. BlockItems).
- Kreativ-Tab „Atomkraftwerk".
- Lokalisierung: Deutsch (`de_de`) und Englisch (`en_us`).
- Einbindung der Team Reborn Energy API (FE-kompatibel, per JiJ gebündelt).
