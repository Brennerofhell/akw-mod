# AGENTS.md — Anweisungen für KI-Agenten

> Diese Datei ist der Einstiegspunkt für Coding-Agenten (Google Antigravity, Codex,
> Claude u. a.). Sie fasst zusammen, **wie gebaut wird, welche Regeln gelten und wo
> weitergearbeitet werden soll**. Ausführliche Codebase-Notizen stehen in
> [`CLAUDE.md`](CLAUDE.md); Planung in [`ROADMAP.md`](ROADMAP.md) und [`TODO.md`](TODO.md).

## Sprache
- **Doku, Commits und Code-Kommentare auf Deutsch.** Conventional-Commit-Präfixe
  (`feat:`, `fix:`, `docs:`, `build:`, `ci:`).

## Projekt
- **Atomkraftwerk (AKW)** — Minecraft-Mod rund um Kernreaktoren, Energie (FE) und
  Multiblock-Strukturen.
- **Plattform:** NeoForge **21.10.64** · Minecraft **1.21.10** · Java **21** · Mojang-Mappings.
- Mod-ID `akw`, `maven_group=ch.danielt.akw`, Version in `gradle.properties` (`mod_version`).
- Aktiver Git-Branch: **`neoforge`**. Der Port von Fabric ist abgeschlossen.

## Build & Run
```bash
./gradlew build          # kompiliert + baut das Jar → build/libs/neoforge/akw-<version>.jar
./gradlew runClient      # Client starten
./gradlew runClientData  # Datagen: Modelle + Sprache  (schreibt nach src/main/generated)
./gradlew runServerData  # Datagen: Rezepte, Loot, Tags, Advancements
```
- ⚠️ `runClientData` und `runServerData` purgen sich gegenseitig die
  `src/main/generated`-Dateien — die `data/`-Ebene dazwischen sichern
  (siehe `docs/NEOFORGE-MIGRATION.md` §6).
- API-Signaturen **nicht raten**: gegen das Classpath-Jar prüfen (`javap`), z. B.
  `~/.gradle/caches/neoformruntime/intermediate_results/compiledWithNeoForge_*_output.jar`.

## Arbeitsablauf für Agenten
1. **Vor dem Start:** `TODO.md` lesen → nächste Aufgabe aus der „Empfohlenen Reihenfolge".
2. **Während der Arbeit:** Änderungen klein halten, an die bestehende Paketstruktur halten
   (siehe unten / `docs/ARCHITECTURE.md`).
3. **Nach Code-Änderungen:** `./gradlew build` muss grün sein. Bei Datagen-Änderungen
   zusätzlich `runClientData` **und** `runServerData` (data/ dazwischen sichern).
4. **Doku nachziehen:** `CHANGELOG.md`, `TODO.md` und ggf. `ROADMAP.md` bei jeder
   abgeschlossenen Aufgabe aktualisieren.
5. **Commit** erst nach grünem Build, auf Deutsch, mit Conventional-Commit-Präfix.

## Paketstruktur (`src/main/java/ch/danielt/akw/`)
- *(root)* `AkwMod` (`@Mod`, Registrierung + Energie-Capabilities), `AkwClient` (Screens)
- `block/` — Block-Klassen · `block/entity/` — BlockEntities (`MutableEnergyStorage`, `ImplementedInventory`)
- `energy/` — `EnergyNet` (FE-Verteilung über `Capabilities.Energy.BLOCK`)
- `reactor/` — weltunabhängige Multiblock-Logik (`ReactorValidator`, `ReactorLayout`,
  `ReactorSimulation`, `RedstoneMode`, `ComparatorMode`)
- `registry/` — `ModBlocks`, `ModItems`, `ModBlockEntities`, `ModScreenHandlers`,
  `ModItemGroups`, `ModEffects`, `ModSounds` (alle via `DeferredRegister`)
- `screen/` — Menüs/Screens · `effect/` — `RadiationEffect` · `worldgen/` — `ModWorldGen` ·
  `datagen/` — Datagen-Provider

## Wichtige Konventionen (NeoForge 21.10) — Stolpersteine
- **Energie**: NeoForge-nativ. Speicher = `MutableEnergyStorage extends SimpleEnergyHandler`,
  Capability = `Capabilities.Energy.BLOCK`. Dirty-Markierung via `onEnergyChanged`-Callback.
- **BlockItems**: immer `ITEMS.registerSimpleBlockItem(name, block)` — sonst „Item id not set".
- **BlockEntityType**: `new BlockEntityType<>(supplier, Block...)` (kein `Builder` mehr).
- **Inventar-Drop** beim Abbau ist automatisch (`BlockEntity.preRemoveSideEffects`).
- **GUI**: `blit(RenderPipelines.GUI_TEXTURED, …)`, `setTooltipForNextFrame(…)`.
- **Datagen**: `GatherDataEvent.Client` (Modelle/Sprache) + `.Server` (Rezepte/Loot/Tags/Advancements).
- `new ItemStack(DeferredItem)` ist mehrdeutig → `.get()` benutzen.

## Wo weitermachen (Stand 2026-06-24)
Empfohlene Reihenfolge laut [`TODO.md`](TODO.md):
1. **Smoke-Tests** des bestehenden MVP-Multiblock-Reaktors (3×3×3 / 5×5×5 / 7×7×7).
2. **Phase A** — MVP-Lücken: rechteckige Hülle (3–9), Energie-/Item-Ports, präzise Fehler.
3. **Phase B** — Steuerstab-Regler, Controller-GUI, Redstone-Port.
4. **Phase C** — Zustandsautomat, Nachzerfallswärme, beschädigte Kerne.
5. **Polish/Release** — `.ogg`-Sounds, Unit-/GameTests, Guide/ROADMAP nachziehen → v1.0.0.

Konkrete Checklisten zu jeder Phase stehen in [`TODO.md`](TODO.md).

## Tabus
- Keine veröffentlichte Block-ID und kein veröffentlichtes Rezept entfernen
  (Rückwärtskompatibilität).
- Die sechs bestehenden Einblockreaktoren bleiben registriert und unverändert.
- Lokale Scratch-Verzeichnisse (`briefkasten/`, `codex/`, `codes-branch/`, `tools/`,
  `releases/`) **nicht** committen — sie gehören nicht ins Repo.
