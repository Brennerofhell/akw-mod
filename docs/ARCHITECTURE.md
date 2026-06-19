# Architektur & Entwickler-Guide

Technische Dokumentation der AKW-Mod (Atomkraftwerk, Minecraft 1.21.10 / Fabric).
Für die Spieler-Perspektive siehe [GUIDE.md](GUIDE.md), für den Überblick die
[README](../README.md).

---

## 1. Paket-Überblick

Alle Java-Klassen liegen unter `src/main/java/ch/danielt/akw/`:

| Paket | Inhalt |
|---|---|
| *(root)* | `AkwMod` (gemeinsamer Einstiegspunkt), `AkwClient` (Client-Einstiegspunkt) |
| `energy` | `EnergyNet` — gemeinsame FE-Push-Logik (Reaktor, Kabel, Akku) |
| `block` | `NuclearReactorBlock`, `EnergyCableBlock`, `EnergyBatteryBlock` |
| `block.entity` | `NuclearReactorBlockEntity`, `EnergyCableBlockEntity`, `EnergyBatteryBlockEntity`, `ImplementedInventory` |
| `datagen` | `AkwDataGenerator` + 5 Provider (Modelle, Rezepte, Loot, Tags, Lang) |
| `screen` | `NuclearReactorScreenHandler`, `NuclearReactorScreen` |
| `registry` | `ModItems`, `ModBlocks`, `ModBlockEntities`, `ModScreenHandlers`, `ModItemGroups` |
| `worldgen` | `ModWorldGen` — Uranerz-Weltgenerierung |

**Registrierungs-Muster:** Statische Felder in den `Mod*`-Klassen melden Inhalte über
`register(...)`-Helfer an; `registerAll()` erzwingt das Klassen-Laden aus
`AkwMod.onInitialize()`. Reihenfolge ist relevant (siehe unten).

### Initialisierungs-Reihenfolge (`AkwMod.onInitialize`)
```
ModItems → ModBlocks → ModBlockEntities → ModScreenHandlers
        → ModItemGroups → ModWorldGen → EnergyStorage.SIDED-Lookups
```
- `ModBlockEntities` braucht die fertige Liste `ModBlocks.REACTORS`.
- Die Energie-Lookups registrieren `EnergyStorage.SIDED` für `NUCLEAR_REACTOR`,
  `ENERGY_CABLE` und `ENERGY_BATTERY` — alle drei nach `ModBlockEntities`.

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

Ausgabe geht nach **`src/main/generated/`** (als eigene Ressourcen-Wurzel eingebunden,
committed). `src/main/resources/` bleibt für handgepflegte Dateien (Texturen, Icon,
`fabric.mod.json`, Weltgenerierung) — der Fabric-Cleanup löscht dort nichts.

Texturen (PNG) werden **manuell** in `src/main/resources/assets/akw/textures/` gepflegt.

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew runDatagen   # schreibt nach src/main/generated/
```

> **Tier-Werte** (`capacity`, `genPerTick`, `maxExtract`, `burnTicksPerRod`,
> `maxHeat`, `heatPerTick`) leben ausschließlich in `registry/ModBlocks.java`.

---

## 3. Energiefluss & Hitze-Mechanik

### 3.1 Gemeinsame Push-Logik (`EnergyNet`)

```java
// energy/EnergyNet.java
public static void pushToNeighbors(SimpleEnergyStorage source,
                                   World world, BlockPos pos, long maxPerSide) {
    if (source.amount <= 0) return;
    for (Direction dir : Direction.values()) {
        EnergyStorage target = EnergyStorage.SIDED.find(world, pos.offset(dir), dir.getOpposite());
        if (target == null) continue;
        try (Transaction tx = Transaction.openOuter()) {
            EnergyStorageUtil.move(source, target, maxPerSide, tx);
            tx.commit();
        }
    }
}
```

`EnergyNet.pushToNeighbors()` wird von `NuclearReactorBlockEntity`,
`EnergyCableBlockEntity` und `EnergyBatteryBlockEntity` genutzt.

### 3.2 Reaktor-Tick-Logik (`NuclearReactorBlockEntity`)

**`tick(world, pos, state, be)` pro Server-Tick:**
1. **Brennen:** wenn `burnTime > 0` → `burnTime--`, `amount += genPerTick` (gedeckelt),
   `heat += heatPerTick` (gedeckelt auf `maxHeat`).
2. **Kühlen:** `heat -= (countCoolingPipes() * COOL_PER_PIPE + PASSIVE_COOL)`.
3. **Drosselung:** ab 75 % der `maxHeat` wird nur `genPerTick / 4` erzeugt.
4. **Explosion:** bei `heat >= maxHeat` → Block entfernen + `world.createExplosion(...)`.
5. **Zünden:** wenn `burnTime <= 0` und Energie nicht voll und `fuel_rod` im Slot →
   Stab verringern, `burnTime = burnTicksPerRod`.
6. **Abgeben:** `EnergyNet.pushToNeighbors(energyStorage, world, pos, maxExtract)`.
7. **Zustand:** `LIT`-Blockstate setzen/löschen, `markDirty()`.

**Cooling-Pipes zählen:** `countCoolingPipes()` prüft alle 6 Nachbarblöcke per
`world.getBlockState(pos.offset(dir)).isOf(ModBlocks.COOLING_PIPE)`.

**Tier-Parameter** kommen aus dem `NuclearReactorBlock` im Konstruktor
(`(NuclearReactorBlock) state.getBlock()`), sodass ein einziger BE-Typ alle Tiers bedient.

### 3.3 Kabel & Akku

- **`EnergyCableBlockEntity`:** CAPACITY=8 192, TRANSFER=2 048. Nimmt FE von beliebiger
  Seite auf und gibt es pro Tick weiter (EnergyNet).
- **`EnergyBatteryBlockEntity`:** CAPACITY=1 000 000, TRANSFER=4 096. Gibt Füllstand
  als Komparator-Signal (0–15) aus; `lastComparator`-Feld verhindert redundante
  `world.updateComparators()`-Aufrufe.

**Bereitstellung nach außen:** `EnergyStorage.SIDED` wird in `AkwMod` für alle drei
BE-Typen registriert. Dadurch sind die Blöcke automatisch mit Create-FE-Brücken und
anderen FE-kompatiblen Mods kompatibel (kein hard dependency auf Create).

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
synchronisiert). Indizes sind als Konstanten in `NuclearReactorBlockEntity` zentralisiert:

| Index | Konstante | Bedeutung |
|--:|---|---|
| 0 | `IDX_ENERGY` | aktuelle Energie (long → auf `Integer.MAX_VALUE` geklammert) |
| 1 | `IDX_CAPACITY` | Kapazität |
| 2 | `IDX_BURN_TIME` | verbleibende Brenndauer |
| 3 | `IDX_BURN_TOTAL` | Brenndauer des aktuellen Stabs |
| 4 | `IDX_HEAT` | aktuelle Hitze |
| 5 | `IDX_MAX_HEAT` | maximale Hitze (tier-abhängig) |

**Hitzebalken** wird in `NuclearReactorScreen` bei `heatX = x + 137` gezeichnet;
Farbe orange (`0xFFE0902C`), bei ≥ 75 % rot (`0xFFE03030`). Tooltip zeigt `Hitze / maxHitze`.

---

## 5. MC 1.21.10 — wichtige API-Änderungen

| Bereich | Alte API (1.21.1) | Neue API (1.21.10) |
|---|---|---|
| NBT schreiben | `NbtCompound` + `RegistryWrapper` | `WriteView`/`ReadView` |
| NBT lesen | `nbt.getLong("k")` | `view.getLong("k", 0L)` |
| Block-Settings | `new Block.Settings()` | `Block.Settings.create().registryKey(key)` |
| Datagen-Modelle | `net.minecraft.data.client.*` | `net.minecraft.client.data.*` |
| Datagen-Rezepte | `FabricRecipeProvider.generate()` | `getRecipeGenerator()` mit `RecipeGenerator`-Unterklasse |
| Datagen-Tags | `getOrCreateTagBuilder()` | `valueLookupBuilder(TagKey)` |
| FabricModelProvider | `net.fabricmc.fabric.api.datagen.v1.provider.*` | `net.fabricmc.fabric.api.client.datagen.v1.provider.*` |
| Datagen Run | `inherit server` | `inherit client` |
| onStateReplaced | 5 Parameter (inkl. `newState`, `moved`) | 4 Parameter: `(BlockState, ServerWorld, BlockPos, boolean)` |
| Komparator-Output | 3 Parameter | `getComparatorOutput(BlockState, World, BlockPos, Direction)` |
| GUI zeichnen | `drawTexture(id, ...)` | `drawTexture(RenderPipeline, id, ...)` mit `RenderPipelines.GUI_TEXTURED` |
| Welt-Methode | `world.isClient` | `world.isClient()` |
| Entity-Welt | `player.getWorld()` | `player.getEntityWorld()` |

---

## 6. Howto: Neuen Reaktor-Typ hinzufügen

1. **`registry/ModBlocks.java`** — Feld + `registerReactor("<id>", capacity, gen, extract, burn, maxHeat, heatPerTick)`.
   Der neue Block landet automatisch in `REACTORS`, im BlockEntity-Typ, im Energie-Lookup
   und im Kreativ-Tab.
2. **`datagen/ModRecipeProvider.java`** — `createShaped(...)`-Eintrag ergänzen.
3. **`datagen/ModLanguageProvider.java`** — DE- und EN-Namen ergänzen.
4. **Texturen** unter `src/main/resources/assets/akw/textures/block/` anlegen:
   `<id>_top.png`, `<id>_front.png`, `<id>_front_on.png`, `<id>_side.png`.
5. **`./gradlew runDatagen`** — erzeugt alle JSONs. Danach `./gradlew build`.

`ModModelProvider` und `ModLootTableProvider` iterieren über `ModBlocks.REACTORS`
und decken neue Einträge automatisch ab.

### Neuen Infrastruktur-Block hinzufügen (Kabel/Akku-Typ)

1. Neue Block-Klasse in `block/`, neues BlockEntity in `block/entity/`.
2. Feld in `ModBlocks` (analog `ENERGY_CABLE`), BE-Typ in `ModBlockEntities`.
3. `EnergyStorage.SIDED`-Registrierung in `AkwMod.onInitialize()`.
4. `addDrop` in `ModLootTableProvider`, `gen.registerSimpleCubeAll` in `ModModelProvider`,
   Tags in `ModTagsProvider`, Namen in `ModLanguageProvider`, Rezept in `ModRecipeProvider`.
5. `runDatagen` → `build`.

---

## 7. Build & lokale Validierung

`gradlew` benötigt zwingend ein gesetztes `JAVA_HOME`:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew runDatagen     # → JSONs in src/main/generated/
./gradlew build          # → build/libs/akw-0.3.0.jar
./gradlew runClient      # visuelle Prüfung im Spiel
```

**Headless-Daten-Validierung** (lädt alle Registries/Rezepte/Loot):

```bash
( printf 'stop\n' | perl -e 'alarm shift; exec @ARGV' 300 \
    ./gradlew runServer --console=plain > /tmp/akw_server.log 2>&1 )
grep -iE "error|exception|fail|Done \(" /tmp/akw_server.log
```

> Die Log-Zeile `No key layers in MapLike[{}]` ist ein **harmloser Vanilla-Fehler** bei der
> Weltenerstellung und stammt nicht aus dieser Mod.
