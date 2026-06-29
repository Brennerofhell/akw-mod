# CLAUDE.md — Codebase-Überblick

Kurzreferenz für Claude/Entwickler. Sprache der Doku & Commits: **Deutsch**.

## Projekt
- **Atomkraftwerk (AKW)** — Minecraft-Mod rund um Kernreaktoren, Energie (FE) und Multiblock-Strukturen.
- **Plattform:** NeoForge **21.10.64** · Minecraft **1.21.10** · Java **21** · Mojang-Mappings.
- Mod-ID `akw`, `maven_group=ch.danielt.akw`, Version in `gradle.properties` (`mod_version`).
- Früher ein Fabric-Mod; der Port ist abgeschlossen (siehe `docs/NEOFORGE-MIGRATION.md`).

## Build & Run
```bash
./gradlew build          # kompiliert + baut das Jar → build/libs/neoforge/akw-<version>.jar
./gradlew runClient      # Client starten
./gradlew runClientData  # Datagen: Modelle + Sprache  (schreibt nach src/main/generated)
./gradlew runServerData  # Datagen: Rezepte, Loot, Tags, Advancements
```
- Aktuelles Jar: `build/libs/neoforge/`. Alte Fabric-Jars: `releases/fabric/` (lokal, gitignored).
- ⚠️ `runClientData` und `runServerData` purgen sich gegenseitig die `src/main/generated`-Dateien —
  für einen vollständigen Asset-Satz die `data/`-Ebene dazwischen sichern (Details in `docs/NEOFORGE-MIGRATION.md` §6).
- API-Signaturen NICHT raten: gegen das Classpath-Jar prüfen (`javap`), z. B. unter
  `~/.gradle/caches/neoformruntime/intermediate_results/compiledWithNeoForge_*_output.jar`.

## Paketstruktur (`src/main/java/ch/danielt/akw/`)
- *(root)* `AkwMod` (`@Mod`, Registrierung + Energie-Capabilities), `AkwClient` (`@EventBusSubscriber`, Screens)
- `block/` — Block-Klassen (Reaktoren, Kabel, Akku, Abfallbehälter, Bausteine, Multiblock-/Bauroboter-Controller)
- `block/entity/` — BlockEntities; `MutableEnergyStorage` (Energie-Facade über `SimpleEnergyHandler`),
  `ImplementedInventory`
- `energy/` — `EnergyNet` (FE-Verteilung über `Capabilities.Energy.BLOCK` + Transaktionen)
- `reactor/` — weltunabhängige Multiblock-Logik: `ReactorValidator`, `ReactorLayout`, `ReactorSimulation`,
  `RedstoneMode`, `ComparatorMode`
- `registry/` — `ModBlocks`, `ModItems`, `ModBlockEntities`, `ModScreenHandlers`, `ModItemGroups`,
  `ModEffects`, `ModSounds` (alle via `DeferredRegister`)
- `screen/` — Menüs/Screens (Reaktor, Multiblock-Reaktor)
- `effect/` — `RadiationEffect` · `worldgen/` — `ModWorldGen` (Uranerz) · `datagen/` — siehe unten

## Konventionen / Stolpersteine (NeoForge 21.10)
- **Energie**: NeoForge-nativ (kein Team Reborn). Speicher = `MutableEnergyStorage extends SimpleEnergyHandler`,
  Capability = `Capabilities.Energy.BLOCK` (`EnergyHandler`). Dirty-Markierung via `onEnergyChanged`-Callback.
- **BlockItems**: immer `ITEMS.registerSimpleBlockItem(name, block)` — sonst „Item id not set".
- **BlockEntityType**: `new BlockEntityType<>(supplier, Block...)` (kein `Builder` mehr).
- **Inventar-Drop** beim Abbau ist automatisch (`BlockEntity.preRemoveSideEffects`); keine `onRemove`-Drops.
- **GUI**: `blit(RenderPipelines.GUI_TEXTURED, …)`, `setTooltipForNextFrame(…)`.
- **Datagen**: `GatherDataEvent.Client` (Modelle/Sprache) + `.Server` (Rezepte/Loot/Tags/Advancements).
- `new ItemStack(DeferredItem)` ist mehrdeutig → `.get()` benutzen.

## Dokumentation
- `docs/REFERENCE.md` — **vollständige technische Referenz** (klassengenau: Konstanten, Formeln, Blockstates, NBT-Keys, Registry-IDs, GUI-Layout, Datagen, Worldgen).
- `docs/NEOFORGE-MIGRATION.md` — vollständige Fabric→NeoForge-API-Referenz (Mapping-Tabellen, Energie, Datagen).
- `docs/ARCHITECTURE.md` — technischer Aufbau (Pakete, Datagen-Pipeline, Energiefluss, GUI-Kette).
- `docs/GUIDE.md` — Spieler-Anleitung. · `CONTRIBUTING.md` — Beitrags-/Dev-Guide.
- `ROADMAP.md` / `TODO.md` — Planung & offene Aufgaben (u. a. modularer Reaktor Phase A–C).

## Git
- Aktiver Branch: `neoforge` (Port). Commits auf Deutsch, Conventional-Commit-Präfixe (`feat:`, `docs:`, `build:`).
- Lokale Scratch-Verzeichnisse (`briefkasten/`, `codex/`, `tools/`, `releases/`) gehören nicht ins Repo.
