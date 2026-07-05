# Architektur & Entwickler-Guide

Technische Dokumentation der AKW-Mod (Atomkraftwerk, Minecraft 1.21.10 / NeoForge 21.10.64).
Für die Spieler-Perspektive siehe [GUIDE.md](GUIDE.md), für den Überblick die
[README](../README.md). Die vollständige Fabric→NeoForge-API-Referenz steht in
[NEOFORGE-MIGRATION.md](NEOFORGE-MIGRATION.md).

> Dieses Dokument gibt den **konzeptionellen** Aufbau. Die **klassengenaue** Referenz
> (jede Klasse, Konstante, Formel, Blockstate, NBT-Key, Registry-ID, GUI-Koordinate,
> Datagen/Worldgen) steht in [REFERENCE.md](REFERENCE.md).

---

## 1. Paket-Überblick

Alle Java-Klassen liegen unter `src/main/java/ch/danielt/akw/`:

| Paket | Inhalt |
|---|---|
| *(root)* | `AkwMod` (`@Mod`-Klasse, Registrierung + Energie-Capabilities), `AkwClient` (`@EventBusSubscriber`, Screens) |
| `energy` | `EnergyNet` — gemeinsame FE-Push-Logik (Reaktor, Kabel, Akku, Multiblock) |
| `block` | Block-Klassen inkl. `MultiblockReactorControllerBlock`, `ReactorBuilderControllerBlock` |
| `block.entity` | BlockEntities; `MutableEnergyStorage` (Energie-Facade über `SimpleEnergyHandler`), `ImplementedInventory` |
| `reactor` | `ReactorValidator`, `ReactorLayout`, `ReactorSimulation`, `RedstoneMode`, `ComparatorMode` (weltunabhängige Multiblock-Logik) |
| `datagen` | `AkwDataGenerator` + 6 Provider (Modelle, Rezepte, Loot, Tags, Lang, Advancements) |
| `screen` | `NuclearReactorScreenHandler`/`NuclearReactorScreen` (Einblock), `ModularReactorScreenHandler`/`ModularReactorScreen` (Multiblock, eigenständiges Tab-GUI seit Phase B) |
| `registry` | `ModItems`, `ModBlocks`, `ModBlockEntities`, `ModScreenHandlers`, `ModItemGroups`, `ModEffects`, `ModSounds` |
| `worldgen` | `ModWorldGen` — Uranerz-Weltgenerierung |

**Registrierungs-Muster:** Jede `Mod*`-Klasse hält einen `DeferredRegister` und meldet ihn im
`AkwMod`-Konstruktor über `register(modEventBus)` an. Die Inhalte werden so erst beim `RegisterEvent`
gebaut. Reihenfolge der `register(...)`-Aufrufe ist relevant (siehe unten).

### Registrierungs-Reihenfolge (`AkwMod`-Konstruktor)
```
ModItems → ModBlocks → ModEffects → ModSounds → ModBlockEntities
        → ModScreenHandlers → ModItemGroups
        + modEventBus.addListener(this::registerCapabilities)
```
- `ModBlockEntities` braucht die fertige Liste `ModBlocks.REACTORS`.
- `registerCapabilities(RegisterCapabilitiesEvent)` registriert `Capabilities.Energy.BLOCK` für
  `NUCLEAR_REACTOR`, `REACTOR_BUILDER_CONTROLLER`, `ENERGY_CABLE` und `ENERGY_BATTERY`
  (Provider: `(be, side) -> be.energyStorage`) sowie für `REACTOR_ENERGY_PORT`
  (Provider: `(be, side) -> be.resolveControllerEnergy()`). Der
  `MULTIBLOCK_REACTOR_CONTROLLER` ist **bewusst nicht** registriert — FE fließt beim
  Multiblock ausschließlich über Energie-Ports (siehe REFERENCE.md §1).
- BlockItems werden über `ITEMS.registerSimpleBlockItem(name, block)` angelegt (setzt die nötige Item-ID).

---

## 2. NeoForge Datagen — Asset-Pipeline

Blockstates, Modelle, Loot-Tables, Rezepte, Tags, Lang-Dateien und Advancements werden per
**NeoForge Datagen** (`GatherDataEvent`, Java) erzeugt. Die Wahrheitsquelle ist ausschließlich Java.

```
src/main/java/ch/danielt/akw/datagen/
  AkwDataGenerator.java     ← @EventBusSubscriber: GatherDataEvent.Client + .Server
  ModRecipeProvider.java    → data/akw/recipe/*.json            (RecipeProvider.Runner)
  ModLootTableProvider.java → data/akw/loot_table/blocks/*.json (BlockLootSubProvider)
  ModModelProvider.java     → assets/akw/blockstates/*.json     (ModelProvider)
                              assets/akw/models/block/*.json
                              assets/akw/items/*.json
  ModTagsProvider.java      → data/minecraft/tags/block/mineable/pickaxe.json   (BlockTagsProvider)
                              data/minecraft/tags/block/needs_iron_tool.json
  ModLanguageProvider.java  → assets/akw/lang/de_de.json + en_us.json (LanguageProvider)
  ModAdvancementProvider.java → data/akw/advancement/story/*.json     (AdvancementSubProvider)
```

Aufteilung der Events: `GatherDataEvent.Client` erzeugt Client-Assets (Modelle, Sprache),
`GatherDataEvent.Server` erzeugt Server-Daten (Rezepte, Loot, Tags, Advancements).

Ausgabe geht nach **`src/main/generated/`** (als eigene Ressourcen-Wurzel eingebunden,
committed). `src/main/resources/` bleibt für handgepflegte Dateien (Texturen, Icon,
`META-INF/neoforge.mods.toml`, Weltgenerierung).

Texturen (PNG) werden **manuell** in `src/main/resources/assets/akw/textures/` gepflegt.

```bash
./gradlew runClientData   # Modelle + Sprache → src/main/generated/assets
./gradlew runServerData   # Rezepte, Loot, Tags, Advancements → src/main/generated/data
```

> ⚠️ Beide Läufe schreiben nach `src/main/generated` und purgen sich gegenseitig die jeweils
> anderen Dateien (HashCache). Für einen vollständigen Asset-Satz die `data/`-Ebene zwischen den
> Läufen sichern/wiederherstellen — Schritt-für-Schritt in [NEOFORGE-MIGRATION.md](NEOFORGE-MIGRATION.md) §6.

> **Tier-Werte** (`capacity`, `genPerTick`, `maxExtract`, `burnTicksPerRod`,
> `maxHeat`, `heatPerTick`) leben ausschließlich in `registry/ModBlocks.java`.

---

## 3. Energiefluss & Hitze-Mechanik

### 3.1 Gemeinsame Push-Logik (`EnergyNet`)

```java
// energy/EnergyNet.java — NeoForge Transfer-API
public static void pushToNeighbors(EnergyHandler source, Level level, BlockPos pos, int limit) {
    if (source.getAmountAsInt() <= 0) return;
    for (Direction side : Direction.values()) {
        if (source.getAmountAsInt() <= 0) break;
        EnergyHandler target = Capabilities.Energy.BLOCK.getCapability(
                level, pos.relative(side), null, null, side.getOpposite());
        if (target == null) continue;
        try (Transaction tx = Transaction.openRoot()) {
            int moved = EnergyHandlerUtil.move(source, target, limit, tx);
            if (moved > 0) tx.commit();
        }
    }
}
```

`EnergyNet.pushToNeighbors()` wird von `NuclearReactorBlockEntity`, `EnergyCableBlockEntity`,
`EnergyBatteryBlockEntity` und `ReactorEnergyPortBlockEntity` genutzt. Der
Multiblock-Controller selbst gibt **kein** FE mehr direkt ab — die Abgabe läuft ausschließlich
über die `reactor_energy_port`-Blöcke der Hülle, die den Speicher des Controllers per
`resolveControllerEnergy()` referenzieren. Der Speicher jedes BE ist eine
`MutableEnergyStorage` (Subklasse von `SimpleEnergyHandler`, also selbst ein `EnergyHandler`).

### 3.2 Reaktor-Tick-Logik (`NuclearReactorBlockEntity`)

**`tick(world, pos, state, be)` pro Server-Tick:**
1. **Brennen:** wenn `burnTime > 0` → `burnTime--`, `amount += genPerTick` (gedeckelt),
   `heat += heatPerTick` (gedeckelt auf `maxHeat`).
2. **Kühlen:** `heat -= (countCoolingPipes() * COOL_PER_PIPE + PASSIVE_COOL)`.
3. **Drosselung:** ab 75 % der `maxHeat` wird nur `genPerTick / 4` erzeugt.
4. **Explosion:** bei `heat >= maxHeat` → Block entfernen + `level.explode(...)`.
5. **Zünden:** wenn `burnTime <= 0` und Energie nicht voll und `fuel_rod` im Slot →
   Stab verringern, `burnTime = burnTicksPerRod`.
6. **Abgeben:** `EnergyNet.pushToNeighbors(energyStorage, level, pos, maxExtract)`.
7. **Zustand:** `LIT`-Blockstate setzen/löschen, `setChanged()`.

**Cooling-Pipes zählen:** `countCoolingPipes()` prüft alle 6 Nachbarblöcke per
`level.getBlockState(pos.relative(dir)).is(ModBlocks.COOLING_PIPE.get())`.

**Tier-Parameter** kommen aus dem `NuclearReactorBlock` im Konstruktor
(`(NuclearReactorBlock) state.getBlock()`), sodass ein einziger BE-Typ alle Tiers bedient.

### 3.3 Kabel & Akku

- **`EnergyCableBlockEntity`:** CAPACITY=8 192, TRANSFER=2 048. Nimmt FE von beliebiger
  Seite auf und gibt es pro Tick weiter (EnergyNet).
- **`EnergyBatteryBlockEntity`:** CAPACITY=1 000 000, TRANSFER=4 096. Gibt Füllstand
  als Komparator-Signal (0–15) aus; `lastComparator`-Feld verhindert redundante
  `level.updateNeighbourForOutputSignal()`-Aufrufe.

**Bereitstellung nach außen:** `Capabilities.Energy.BLOCK` wird in `AkwMod.registerCapabilities`
für alle energieführenden BE-Typen registriert. Dadurch sind die Blöcke automatisch mit allen
FE-kompatiblen NeoForge-Mods kompatibel (kein hard dependency). Externes Laden läuft über
`EnergyHandler.insert(...)`; die Dirty-Markierung hängt am `onEnergyChanged`-Hook von `MutableEnergyStorage`.

---

## 4. GUI-Kette

```
Rechtsklick → NuclearReactorBlock.useWithoutItem → serverPlayer.openMenu(be, buf -> buf.writeBlockPos(pos))
   │  (BlockEntity implementiert MenuProvider)
   ▼
ModScreenHandlers.NUCLEAR_REACTOR (MenuType via IMenuTypeExtension.create(... buf.readBlockPos()))
   │  synchronisiert die BlockPos zum Client
   ▼
NuclearReactorScreenHandler
   │  Server: bekommt Container + ContainerData direkt von der BlockEntity
   │  Client: löst beides über die BlockPos aus der Welt auf (resolveInventory/-Delegate)
   ▼
NuclearReactorScreen (Client)  ← liest Werte über die Handler-Getter
```

**Sync der Live-Werte** läuft über die `ContainerData` (vanilla-Mechanik, automatisch
synchronisiert, via `addDataSlots`). Indizes sind als Konstanten in `NuclearReactorBlockEntity` zentralisiert:

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

## 5. Fabric → NeoForge 21.10.64 — wichtige API-Änderungen

Auszug der wichtigsten Umstellungen; die vollständige Mapping- und API-Referenz steht in
[NEOFORGE-MIGRATION.md](NEOFORGE-MIGRATION.md).

| Bereich | Fabric / Yarn | NeoForge 21.10.64 |
|---|---|---|
| Mappings | Yarn (`World`, `markDirty`, …) | Mojang (`Level`, `setChanged`, …) |
| Energie-Speicher | Team Reborn `SimpleEnergyStorage` | `SimpleEnergyHandler` / `MutableEnergyStorage` |
| Energie-Capability | `EnergyStorage.SIDED` | `Capabilities.Energy.BLOCK` (`EnergyHandler`, Transaktionen) |
| BlockEntityType | `BlockEntityType.Builder.of(...).build()` | `new BlockEntityType<>(supplier, Block...)` |
| Komparator-Output | `getComparatorOutput(state, world, pos)` | `getAnalogOutputSignal(state, level, pos, Direction)` |
| Block-Entfernung / Drop | `onStateReplaced` / `Inventories` | `affectNeighborsAfterRemoval` + autom. Drop via `BlockEntity.preRemoveSideEffects` |
| BlockItem-Registrierung | `new BlockItem(block, settings)` | `ITEMS.registerSimpleBlockItem(name, block)` (sonst „Item id not set") |
| NBT | `WriteView`/`ReadView` | `ValueOutput`/`ValueInput` (`saveAdditional`/`loadAdditional`) |
| Menü | `ExtendedScreenHandlerFactory` / `…Type` | `MenuProvider` + `IMenuTypeExtension.create(... readBlockPos)` |
| GUI zeichnen | `drawTexture(...)` | `blit(RenderPipelines.GUI_TEXTURED, ...)` |
| GUI Tooltip | `renderTooltip(font, text, x, y)` | `setTooltipForNextFrame(font, text, x, y)` |
| Datagen-Entry | `DataGeneratorEntrypoint` | `@EventBusSubscriber` + `GatherDataEvent.Client`/`.Server` |
| Datagen-Provider | `Fabric*Provider` | vanilla/NeoForge (`RecipeProvider.Runner`, `BlockLootSubProvider`, `ModelProvider`, `LanguageProvider`, `BlockTagsProvider`, `AdvancementSubProvider`) |
| EventBusSubscriber | `bus = Bus.MOD` | (Parameter entfernt — Bus-Unifizierung) |

---

## 6. Howto: Neuen Reaktor-Typ hinzufügen

1. **`registry/ModBlocks.java`** — Feld + `registerReactor("<id>", capacity, gen, extract, burn, maxHeat, heatPerTick)`.
   Der neue Block landet automatisch in `REACTORS`, im BlockEntity-Typ, im Energie-Lookup
   und im Kreativ-Tab.
2. **`datagen/ModRecipeProvider.java`** — `shaped(...)`-Eintrag ergänzen.
3. **`datagen/ModLanguageProvider.java`** — DE- und EN-Namen ergänzen (`addBlock(...)`).
4. **Texturen** unter `src/main/resources/assets/akw/textures/block/` anlegen:
   `<id>_top.png`, `<id>_front.png`, `<id>_front_on.png`, `<id>_side.png`.
5. **`./gradlew runClientData && runServerData`** — erzeugt alle JSONs. Danach `./gradlew build`.

`ModModelProvider` iteriert über `ModBlocks.REACTORS` (via `createFurnace`); in
`ModLootTableProvider` muss der Block zusätzlich in `getKnownBlocks()` aufgenommen werden.

### Neuen Infrastruktur-Block hinzufügen (Kabel/Akku-Typ)

1. Neue Block-Klasse in `block/`, neues BlockEntity in `block/entity/`.
2. Feld in `ModBlocks` (analog `ENERGY_CABLE`), BE-Typ in `ModBlockEntities`.
3. `Capabilities.Energy.BLOCK`-Registrierung in `AkwMod.registerCapabilities`.
4. `dropSelf` + `getKnownBlocks()` in `ModLootTableProvider`, `blockModels.createTrivialCube` in
   `ModModelProvider`, Tags in `ModTagsProvider`, Namen in `ModLanguageProvider`, Rezept in `ModRecipeProvider`.
5. `runClientData && runServerData` → `build`.

---

## 7. Build & lokale Validierung

`gradlew` benötigt zwingend ein gesetztes `JAVA_HOME`:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew runClientData  # → assets in src/main/generated/
./gradlew runServerData  # → data in src/main/generated/  (data/ ggf. zwischensichern, §2)
./gradlew build          # → build/libs/neoforge/akw-<version>.jar
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
