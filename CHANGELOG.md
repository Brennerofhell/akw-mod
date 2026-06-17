# Changelog

Alle nennenswerten Änderungen an der AKW-Mod werden hier dokumentiert.

Format orientiert sich an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
Versionierung nach [Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

## [0.3.0] — Kühlsystem

### Hinzugefügt
- **Cooling-Pipe-Bonus:** Jedes benachbarte `cooling_pipe`-Block erhöht die
  FE/Tick-Ausgabe des Reaktors um 15 % (max. 6 Rohre = +90 %).
- **GUI:** Blauer Kühlbalken links im Reaktor-Menü mit Tooltip
  (Anzahl Rohre + Bonus-Prozent).

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
