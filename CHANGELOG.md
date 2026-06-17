# Changelog

Alle nennenswerten Änderungen an der AKW-Mod werden hier dokumentiert.

Format orientiert sich an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
Versionierung nach [Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

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

### Bekannt / offen
- Reaktor/Energiesystem (Phase 2) noch nicht umgesetzt.

## [0.1.0] — Grundgerüst

### Hinzugefügt
- Mod-Initialisierung mit Registry-System (`AkwMod`, `AkwClient`).
- Items: `raw_uranium`, `uranium_ingot`, `fuel_rod`.
- Blöcke: `uranium_ore`, `deepslate_uranium_ore` (inkl. BlockItems).
- Kreativ-Tab „Atomkraftwerk".
- Lokalisierung: Deutsch (`de_de`) und Englisch (`en_us`).
- Einbindung der Team Reborn Energy API (FE-kompatibel, per JiJ gebündelt).
