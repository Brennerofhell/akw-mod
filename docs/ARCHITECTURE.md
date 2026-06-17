# Architektur & Entwickler-Guide

Technische Dokumentation der AKW-Mod (Atomkraftwerk, Minecraft 1.21.1 / Fabric).
Für die Spieler-Perspektive siehe [GUIDE.md](GUIDE.md), für den Überblick die
[README](../README.md).

---

## 1. Paket-Überblick

Alle Java-Klassen liegen unter `src/main/java/ch/danielt/akw/`:

| Paket | Inhalt |
|---|---|
| *(root)* | `AkwMod` (gemeinsamer Einstiegspunkt), `AkwClient` (Client-Einstiegspunkt) |
| `block` | `NuclearReactorBlock` — der Reaktor-Block (FACING/LIT, GUI-Öffnung, Ticker) |
| `block.entity` | `NuclearReactorBlockEntity` (Logik & Energie), `ImplementedInventory` (Inventory-Helfer) |
| `screen` | `NuclearReactorScreenHandler` (Slots/Sync), `NuclearReactorScreen` (Client-GUI) |
| `registry` | `ModItems`, `ModBlocks`, `ModBlockEntities`, `ModScreenHandlers`, `ModItemGroups` |
| `worldgen` | `ModWorldGen` — Uranerz-Weltgenerierung |

**Registrierungs-Muster:** Statische Felder in den `Mod*`-Klassen melden Inhalte über
`register(...)`-Helfer an; `registerAll()` erzwingt das Klassen-Laden aus
`AkwMod.onInitialize()`. Reihenfolge ist relevant (siehe unten).

### Initialisierungs-Reihenfolge (`AkwMod.onInitialize`)
```
ModItems → ModBlocks → ModBlockEntities → ModScreenHandlers
        → ModItemGroups → ModWorldGen → EnergyStorage.SIDED-Lookup
```
- `ModBlockEntities` braucht die fertige Liste `ModBlocks.REACTORS`.
- Der Energie-Lookup braucht den registrierten `ModBlockEntities.NUCLEAR_REACTOR`-Typ.
- `ModBlocks` initialisiert die Listen `REACTORS`/`DECOR` **vor** den Block-Feldern
  (Feld-Reihenfolge in der Klasse), damit `registerReactor`/`registerDecor` einfügen können.

---

## 2. Fabric Datagen — Asset-Pipeline

Blockstates, Modelle, Loot-Tables, Rezepte, Tags und Lang-Dateien werden per
**Fabric Datagen** (Java) erzeugt. Die Wahrheitsquelle ist ausschließlich Java.

```
src/main/java/ch/danielt/akw/datagen/
  AkwDataGenerator.java     ← Datagen-Einstiegspunkt (fabric-datagen-Entrypoint)
  ModRecipeProvider.java    → data/akw/recipe/*.json
  ModLootTableProvider.java → data/akw/loot_table/blocks/*.json
  ModModelProvider.java     → assets/akw/blockstates/*.json
                              assets/akw/models/block/*.json
                              assets/akw/models/item/*.json
  ModTagsProvider.java      → data/minecraft/tags/block/mineable/pickaxe.json
                              data/minecraft/tags/block/needs_iron_tool.json
  ModLanguageProvider.java  → assets/akw/lang/de_de.json + en_us.json
```

Texturen (PNG) werden **manuell** in `assets/akw/textures/` gepflegt — Datagen
erzeugt keine Bilder.

Datagen ausführen (schreibt direkt nach `src/main/resources/`):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew runDatagen
```

> **Tier-Werte** (`capacity`, `genPerTick`, `maxExtract`, `burnTicksPerRod`) leben
> ausschließlich in `registry/ModBlocks.java` — eine einzige Wahrheitsquelle für
> Laufzeit und Datagen.

---

## 3. Energiefluss (Reaktor)

Kern ist `NuclearReactorBlockEntity` mit einem `SimpleEnergyStorage` aus der
**Team Reborn Energy**-API (`SimpleEnergyStorage(capacity, maxInsert=0, maxExtract)` —
reiner Generator, keine Aufnahme).

**`tick(world, pos, state, be)` pro Server-Tick:**
1. **Brennen:** wenn `burnTime > 0` → `burnTime--` und `amount += genPerTick` (auf
   `capacity` gedeckelt).
2. **Zünden:** wenn `burnTime <= 0` und Energie nicht voll und ein `fuel_rod` im Slot →
   Stab um 1 verringern, `burnTime = burnTicksPerRod`.
3. **Abgeben:** `pushEnergy(...)` schiebt FE an alle 6 Nachbarn, die einen
   `EnergyStorage` anbieten — über eine `Transaction` und `EnergyStorageUtil.move(...)`.
4. **Zustand:** wechselt `burning`, wird der `LIT`-Blockstate gesetzt (Front leuchtet).
5. `markDirty()` bei Änderungen → NBT wird persistiert (`Energy`, `BurnTime`, `BurnTimeTotal`).

**Bereitstellung nach außen:** `AkwMod` registriert
`EnergyStorage.SIDED.registerForBlockEntity((be, dir) -> be.energyStorage, NUCLEAR_REACTOR)`.
Dadurch finden FE-Kabel/-Maschinen (Tech Reborn & Co.) den Speicher über jede Seite.

**Tier-Parameter** kommen aus dem `NuclearReactorBlock` und werden im BlockEntity-Konstruktor
gelesen (`(NuclearReactorBlock) state.getBlock()`), sodass ein einziger BlockEntity-Typ alle
Reaktor-Tiers bedient.

---

## 4. GUI-Kette

```
Rechtsklick → NuclearReactorBlock.onUse → player.openHandledScreen(blockEntity)
   │  (BlockEntity ist ExtendedScreenHandlerFactory<BlockPos>)
   ▼
ModScreenHandlers.NUCLEAR_REACTOR (ExtendedScreenHandlerType, BlockPos.PACKET_CODEC)
   │  synchronisiert die BlockPos zum Client
   ▼
NuclearReactorScreenHandler
   │  Server: bekommt Inventory + PropertyDelegate direkt von der BlockEntity
   │  Client: löst beides über die BlockPos aus der Welt auf (resolveInventory/-Delegate)
   ▼
NuclearReactorScreen (Client)  ← liest Werte über die Handler-Getter
```

**Sync der Live-Werte** läuft über das `PropertyDelegate` (vanilla-Mechanik, automatisch
synchronisiert). Indizes sind als Konstanten in `NuclearReactorBlockEntity` zentralisiert,
um Desync zwischen den drei Klassen zu vermeiden:

| Index | Konstante | Bedeutung |
|--:|---|---|
| 0 | `IDX_ENERGY` | aktuelle Energie (long → auf `Integer.MAX_VALUE` geklammert) |
| 1 | `IDX_CAPACITY` | Kapazität |
| 2 | `IDX_BURN_TIME` | verbleibende Brenndauer |
| 3 | `IDX_BURN_TOTAL` | Brenndauer des aktuellen Stabs |

---

## 5. Rezept-Format (1.21.1)

Wichtige Eigenheit: das **Result** nutzt `"id"` (nicht `"item"` oder einen String):

```json
{ "type": "minecraft:crafting_shaped", "category": "misc",
  "pattern": ["III","UFU","IRI"],
  "key": { "I": {"item":"minecraft:iron_ingot"}, "U": {"item":"akw:uranium_ingot"},
           "F": {"item":"minecraft:furnace"}, "R": {"item":"minecraft:redstone"} },
  "result": { "id": "akw:nuclear_reactor", "count": 1 } }
```

Ordnernamen sind **singular**: `data/akw/recipe/`, `data/akw/loot_table/blocks/`.

---

## 6. Howto: Neuen Reaktor-Typ hinzufügen

1. **`registry/ModBlocks.java`** — Feld + `registerReactor("<id>", capacity, gen, extract, burn)`.
   Der neue Block landet automatisch in `REACTORS`, im BlockEntity-Typ, im Energie-Lookup
   und im Kreativ-Tab.
2. **`datagen/ModRecipeProvider.java`** — `ShapedRecipeJsonBuilder`-Eintrag ergänzen.
3. **`datagen/ModLanguageProvider.java`** — DE- und EN-Namen ergänzen.
4. **Texturen** unter `assets/akw/textures/block/` anlegen:
   `<id>_top.png`, `<id>_front.png`, `<id>_front_on.png`, `<id>_side.png`.
5. **`./gradlew runDatagen`** — erzeugt alle JSONs. Danach `./gradlew build`.

`ModModelProvider` und `ModLootTableProvider` iterieren über `ModBlocks.REACTORS`
und decken neue Einträge automatisch ab — dort ist keine Änderung nötig.

Ein neuer **Baustein** (Vollwürfel): `registerDecor("<id>", settings)` in `ModBlocks`,
`addDrop` in `ModLootTableProvider`, Namen in `ModLanguageProvider`, Textur anlegen,
`runDatagen`.

---

## 7. Build & lokale Validierung

`gradlew` benötigt zwingend ein gesetztes `JAVA_HOME` (trotz `org.gradle.java.home` in
`gradle.properties`):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew runDatagen     # → JSONs in src/main/resources/ (nach Datagen-Änderungen)
./gradlew build          # → build/libs/akw-<version>.jar
./gradlew runClient      # visuelle Prüfung im Spiel
```

**Headless-Daten-Validierung** (lädt alle Registries/Rezepte/Loot, meldet JSON-Fehler ohne
GUI). `timeout` fehlt auf macOS → `perl alarm` nutzen:

```bash
( printf 'stop\n' | perl -e 'alarm shift; exec @ARGV' 300 \
    ./gradlew runServer --console=plain > /tmp/akw_server.log 2>&1 )
grep -iE "error|exception|fail|registriert|Done \(" /tmp/akw_server.log
```

> Die Log-Zeile `No key layers in MapLike[{}]` ist ein **harmloser Vanilla-Fehler** bei der
> Weltenerstellung und stammt nicht aus dieser Mod.
