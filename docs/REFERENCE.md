# Technische Referenz — AKW-Mod

Vollständige, klassengenaue Referenz der **Atomkraftwerk-Mod** (`akw`). Sie dokumentiert
jedes Paket, jede Klasse, alle Konstanten/Formeln, Blockstates, NBT-Keys, Registry-IDs,
das GUI-Layout, Datagen und Worldgen.

Diese Datei ist die **Detailreferenz**. Für den konzeptionellen Überblick siehe
[ARCHITECTURE.md](ARCHITECTURE.md), für Spieler [GUIDE.md](GUIDE.md), für die
Fabric→NeoForge-API-Tabellen [NEOFORGE-MIGRATION.md](NEOFORGE-MIGRATION.md).

| | |
|---|---|
| **Mod-ID** | `akw` (`AkwMod.MOD_ID`) |
| **Maven-Group / Paket-Root** | `ch.danielt.akw` |
| **Minecraft** | 1.21.10 |
| **NeoForge** | 21.10.64 (`loaderVersion = "[4,)"`, Dependency `neoforge [21.10,)`, `minecraft [1.21.10,1.22)`) |
| **Java** | 21 (Mojang-Mappings) |
| **Mod-Version** | 1.2.0 (`gradle.properties` → `mod_version`) |
| **Lizenz** | MIT |

> **Wahrheitsquelle der Balance-Werte:** Reaktor-Tier-Werte stehen ausschließlich in
> `registry/ModBlocks.java`; Multiblock-Balance-Konstanten in `reactor/ReactorSimulation.java`.

---

## Inhalt

1. [Initialisierung & Registry](#1-initialisierung--registry)
2. [Items](#2-items)
3. [Blöcke](#3-blöcke)
4. [Einblock-Reaktoren](#4-einblock-reaktoren)
5. [Multiblock-Reaktor](#5-multiblock-reaktor)
6. [Bauroboter (Reactor Builder)](#6-bauroboter-reactor-builder)
7. [Energie-Infrastruktur](#7-energie-infrastruktur)
8. [Abfallbehälter](#8-abfallbehälter)
9. [Energiesystem (EnergyNet / MutableEnergyStorage)](#9-energiesystem)
10. [Strahlung & Effekt](#10-strahlung--effekt)
11. [Steuerung: Redstone- & Komparator-Modi](#11-steuerung-redstone--komparator-modi)
12. [GUI / Screen-Kette](#12-gui--screen-kette)
13. [Worldgen](#13-worldgen)
14. [Datengenerierung & Rezepte](#14-datengenerierung--rezepte)
15. [Sounds](#15-sounds)
16. [Creative-Tab & Lokalisierung](#16-creative-tab--lokalisierung)
17. [NBT-Schlüssel — Übersicht](#17-nbt-schlüssel--übersicht)

---

## 1. Initialisierung & Registry

### `AkwMod` (`@Mod("akw")`)
Einstiegspunkt. Konstruktor `AkwMod(IEventBus modEventBus)` registriert in **dieser Reihenfolge**:

```
ModItems → ModBlocks → ModEffects → ModSounds → ModBlockEntities
        → ModScreenHandlers → ModItemGroups
        + modEventBus.addListener(this::registerCapabilities)
```

- `ModBlockEntities` benötigt die fertige Liste `ModBlocks.REACTORS`.
- `MOD_ID = "akw"`, `LOGGER = LoggerFactory.getLogger("akw")`.

`registerCapabilities(RegisterCapabilitiesEvent)` meldet `Capabilities.Energy.BLOCK` für fünf
BE-Typen an, Provider jeweils `(be, side) -> be.energyStorage`:
`NUCLEAR_REACTOR`, `MULTIBLOCK_REACTOR_CONTROLLER`, `REACTOR_BUILDER_CONTROLLER`,
`ENERGY_CABLE`, `ENERGY_BATTERY`.

### `AkwClient` (`@EventBusSubscriber(value = Dist.CLIENT)`)
`onRegisterScreens(RegisterMenuScreensEvent)` bindet beide MenuTypes an `NuclearReactorScreen`:
`NUCLEAR_REACTOR` und `MULTIBLOCK_REACTOR` (der Multiblock nutzt denselben Screen).

### Registrierungsmuster
Jede `Mod*`-Klasse (`registry/`) hält einen `DeferredRegister` und exponiert `register(IEventBus)`.
BlockItems werden über `ITEMS.registerSimpleBlockItem(name, block)` erzeugt (sonst „Item id not set").
`BlockEntityType` via `new BlockEntityType<>(supplier, Block...)` (kein Builder mehr).

---

## 2. Items

`registry/ModItems.java` — `DeferredRegister.Items`, alle via `registerSimpleItem`.

| Konstante | ID | Verwendung |
|---|---|---|
| `RAW_URANIUM` | `akw:raw_uranium` | Drop von Uranerz (Fortune wirkt) |
| `URANIUM_INGOT` | `akw:uranium_ingot` | Aus Roh-Uran geschmolzen |
| `ENRICHED_URANIUM` | `akw:enriched_uranium` | 2× Uran-Barren |
| `FUEL_ROD` | `akw:fuel_rod` | Reaktor-Treibstoff (3× angereichertes Uran) |
| `SPENT_FUEL_ROD` | `akw:spent_fuel_rod` | Verbrannter Brennstab (Abfall) |
| `REACTOR_WRENCH` | `akw:reactor_wrench` | Multiblock assemblieren/deaktivieren |

---

## 3. Blöcke

`registry/ModBlocks.java` — `DeferredRegister.Blocks` + `.Items`. Hilfslisten:
`REACTORS` (alle Reaktor-Tiers) und `DECOR` (einfache Bausteine). Material-Basis `metal()` =
`Properties.ofFullCopy(Blocks.IRON_BLOCK)`.

| Konstante | ID | Klasse | Besonderheit |
|---|---|---|---|
| `URANIUM_ORE` | `akw:uranium_ore` | `Block` | Kopie `IRON_ORE` |
| `DEEPSLATE_URANIUM_ORE` | `akw:deepslate_uranium_ore` | `Block` | Kopie `DEEPSLATE_IRON_ORE` |
| `REACTOR_CORE` | `akw:reactor_core` | `Block` | `lightLevel 7` |
| `CONTROL_ROD_BLOCK` | `akw:control_rod_block` | `Block` | — |
| `COOLING_PIPE` | `akw:cooling_pipe` | `Block` | Kopie `COPPER_BLOCK` |
| `LEAD_BLOCK` | `akw:lead_block` | `Block` | Strahlenschutz (pfadbasiert) |
| `WASTE_CONTAINER` | `akw:waste_container` | `WasteContainerBlock` | 9-Slot-Speicher |
| `ENRICHED_URANIUM_BLOCK` | `akw:enriched_uranium_block` | `Block` | `lightLevel 5` |
| `REACTOR_CASING` | `akw:reactor_casing` | `ReactorCasingBlock` | `strength(5, 1200)` |
| `MULTIBLOCK_REACTOR_CONTROLLER` | `akw:multiblock_reactor_controller` | `MultiblockReactorControllerBlock` | `lightLevel 13` bei `LIT` |
| `REACTOR_BUILDER_CONTROLLER` | `akw:reactor_builder_controller` | `ReactorBuilderControllerBlock` | `lightLevel 7` bei `ACTIVE` |
| `ENERGY_CABLE` | `akw:energy_cable` | `EnergyCableBlock` | FE-Transport |
| `ENERGY_BATTERY` | `akw:energy_battery` | `EnergyBatteryBlock` | FE-Speicher |
| 6× Reaktoren | siehe §4 | `NuclearReactorBlock` | `lightLevel 13` bei `LIT` |

`ReactorCasingBlock` ist ein reiner `Block` ohne eigene Logik (nur als Multiblock-Wand verwendet).

---

## 4. Einblock-Reaktoren

### `block/NuclearReactorBlock` (`Block implements EntityBlock`)
Eine Instanz pro Tier; alle Tiers teilen sich eine `NuclearReactorBlockEntity`. Tier-Werte sind
`public final int`-Felder, im Konstruktor gesetzt:
`capacity, genPerTick, maxExtract, burnTicksPerRod, maxHeat, heatPerTick`.

**Blockstate-Properties:** `FACING` (`HORIZONTAL_FACING`), `LIT`, `POWERED`.
Default `FACING=NORTH, LIT=false, POWERED=false`. Platzierung: `FACING = ctx.getHorizontalDirection().getOpposite()`.

- `hasAnalogOutputSignal = true`, `getAnalogOutputSignal` → `BE.getComparatorLevel()`.
- `neighborChanged` → setzt `POWERED = level.hasNeighborSignal(pos)`.
- `useWithoutItem` (Server) → `ServerPlayer.openMenu(reactor, buf -> buf.writeBlockPos(pos))`.
- `getTicker` nur serverseitig → `NuclearReactorBlockEntity.tick`.

### Tier-Tabelle (`registry/ModBlocks.registerReactor(...)`)

| Reaktor | ID | capacity | genPerTick | maxExtract | burnTicks | maxHeat | heatPerTick |
|---|---|--:|--:|--:|--:|--:|--:|
| Reaktor | `nuclear_reactor` | 100 000 | 40 | 512 | 1600 | 1 200 | 6 |
| Fortgeschritten | `advanced_nuclear_reactor` | 400 000 | 120 | 2 048 | 2000 | 2 000 | 14 |
| Brutreaktor | `breeder_reactor` | 800 000 | 240 | 4 096 | 2200 | 2 400 | 18 |
| Thorium | `thorium_reactor` | 600 000 | 180 | 3 072 | 2600 | 2 200 | 16 |
| Elite | `elite_nuclear_reactor` | 1 600 000 | 360 | 8 192 | 2400 | 3 200 | 28 |
| Fusion | `fusion_reactor` | 4 000 000 | 1 000 | 32 768 | 1200 | 4 000 | 44 |

### `block/entity/NuclearReactorBlockEntity`
`implements ImplementedInventory, WorldlyContainer, MenuProvider`. Inventar 2 Slots:
`FUEL_SLOT=0`, `WASTE_SLOT=1`. Energiespeicher `MutableEnergyStorage(capacity, 0, maxExtract)` —
**reiner Generator** (kein Input, nur Abgabe).

**Konstanten:**

| Konstante | Wert | Bedeutung |
|---|--:|---|
| `COOL_PER_PIPE` | 8 | Kühlung/Tick je angrenzendem Kühlrohr |
| `PASSIVE_COOL` | 2 | Eigenkühlung/Tick |
| `THROTTLE_NUMERATOR` | 3 | Drosselung ab `maxHeat·3/4` (= 75 %) |
| `HEAT_REDUCTION_PER_ROD` | 4 | Hitzeaufbau-Reduktion je angrenzendem Steuerstab |
| `RADIATION_RADIUS` | 8 | Strahlungsradius (Blöcke) |

`explosionPower = clamp(maxHeat / 400, 4, 12)`.

**`tick(level, pos, state, be)`** (server-only) pro Server-Tick:
1. **EMERGENCY_STOP:** wenn `POWERED` && Modus `EMERGENCY_STOP` && `burnTime>0` → `burnTime=0`, `burnTimeTotal=0`.
2. **Brennen:** wenn `burnTime>0` → `burnTime--`; `gen = genPerTick`, bei `heat ≥ maxHeat·3/4` → `gen = max(1, gen/4)` (Drosselung); Energie += `gen` (gedeckelt auf capacity).
3. **Zünden:** wenn `burnTime≤0` && Energie < capacity && `canIgnite` (siehe §11) && `FUEL_ROD` im Slot && Platz im Abfallslot → Brennstab −1, Spent-Rod in Abfallslot, `burnTime = burnTimeTotal = burnTicksPerRod`.
4. **Hitze:** `heatEff = max(0, heatPerTick − Steuerstäbe·4)`; bei `wasBurning` → `heat += heatEff`; danach `heat = max(0, heat − (PASSIVE_COOL + Kühlrohre·8))`.
5. **Explosion:** bei `heat ≥ maxHeat` → Block entfernen + `level.explode(... explosionPower ...)`.
6. **Abgabe:** `EnergyNet.pushToNeighbors(energyStorage, level, pos, maxExtract)`.
7. **Strahlung:** wenn `burnTime>0` → siehe §10. `radLevel = heat ≥ maxHeat/2 ? 1 : 0`.
8. **Partikel:** alle 10 Ticks `ELECTRIC_SPARK` über dem Block (wenn aktiv).
9. **Blockstate:** `LIT` = `burnTime>0` bei Änderung.
10. **Komparator:** `updateNeighbourForOutputSignal` bei Änderung von `getComparatorLevel()`.

`countCoolingPipes` / `countControlRods` prüfen alle 6 direkten Nachbarn (max. 6).

**WorldlyContainer:** `UP` → Brennstoff (nur `FUEL_ROD` einsetzbar), `DOWN` → Abfall (entnehmbar), Seiten gesperrt.

**`hasLeadShielding(level, reactorPos, playerPos)`** (package-private, auch vom Abfallbehälter genutzt):
rastert den direkten Pfad in Schrittweite 1 Block; ein `LEAD_BLOCK` darauf blockt vollständig.

---

## 5. Multiblock-Reaktor

Modular aufgebauter Reaktor aus Hülle + Innenmodulen. Die Leistung skaliert mit den **tatsächlich
eingebauten Kernen**, nicht mit dem leeren Innenvolumen.

### Weltunabhängige Logik (`reactor/`)

#### `ReactorLayout` (record, immutable)
Vorberechneter Innenraum-Snapshot. Komponenten (alle `int`):
`outerSize, coreCount, controlRodCount, coolingPipeCount, connectedCoolingPipeCount,
coreNeighborContacts, coreControlRodContacts, coreCoolingContacts`.
- `EMPTY = new ReactorLayout(0,0,0,0,0,0,0,0)`.
- `isAssembled()` → `outerSize ≥ 3 && coreCount > 0`.

> `coreNeighborContacts` zählt jedes Kern-Kern-Paar doppelt (pro Kern in alle 6 Richtungen geprüft).
> `connectedCoolingPipeCount`/`coreCoolingContacts` zählen **nur** per Flood-Fill mit der Hülle
> verbundene Rohre — isolierte Rohre kühlen nicht.

#### `ReactorValidator` (statisch)
Prüft eine zentrierte Hülle der Größe 3/5/7. Koordinaten relativ zum Controller:
`forward = facing.getOpposite()`, `right = facing.getClockWise()`, `half = outerSize/2`.
Der Controller sitzt in der **Mitte der zugewandten Fläche** (dz=0), nicht im geometrischen Zentrum.

- `find(level, pos, facing)` — probiert Größen **7 → 5 → 3**, gibt die erste gültige zurück;
  merkt den spezifischsten Fehler (≠ `MISSING_CASING`) als „nützlichsten".
- `validate(level, pos, facing, outerSize)` — iteriert den Würfel, klassifiziert jede Position:
  - **Hülle** (`|dx|=half || |dy|=half || dz∈{0, outerSize−1}`): Controller-Position muss der
    Controller sein, sonst `REACTOR_CASING` — andernfalls `MISSING_CASING`.
  - **Innenraum:** Luft/`LEAD_BLOCK` erlaubt (zählt nichts); `REACTOR_CORE`/`CONTROL_ROD_BLOCK`/
    `COOLING_PIPE` werden gezählt; alles andere → `INVALID_INTERIOR`.
  - Keine Kerne → `MISSING_CORE`.
- `findPipesConnectedToShell(...)` — Zwei-Phasen-BFS: Rohre mit Hüllenkontakt markieren, dann über
  benachbarte Rohre ausbreiten.

**Enum `Error`** → Translation-Key:

| Wert | Key |
|---|---|
| `NONE` | `akw.multiblock.assembled` |
| `MISSING_CASING` | `akw.multiblock.error.casing` |
| `MISSING_CORE` | `akw.multiblock.error.core` |
| `INVALID_INTERIOR` | `akw.multiblock.error.interior` |

**Record `Result(layout, error)`** — `success(layout)`, `error(error)`, `valid()`.

#### `ReactorSimulation` (statisch, reine Balance-Mathematik)

**Konstanten:** `FE_PER_CORE=80`, `HEAT_PER_CORE=12`, `BURN_TICKS=2400`,
`CAPACITY_PER_CORE=100 000`, `PASSIVE_COOLING=2`, `COOLING_PER_CONTACT=8`.

**`calculate(layout, activeCores)` → `ReactorStats(generationPerTick, heatPerTick, coolingPerTick, capacity, maxHeat)`**

Zwischengrößen (`cores = layout.coreCount()`, `n = min(activeCores, cores)`):
```
neighborsPerCore       = coreNeighborContacts / cores
reactivity             = min(1.0, 0.60 + 0.10 * neighborsPerCore)        // 0.60 … 1.00
controlRodsPerCore     = min(2.0, coreControlRodContacts / cores)
heatFactor             = max(0.50, 1.0 - 0.25 * controlRodsPerCore)      // 0.50 … 1.00
coolingContactsPerCore = coreCoolingContacts / cores
```
Output-Formeln:
```
generation = max(1, round(n * 80 * reactivity))
heat       = max(1, round(n * 12 * reactivity² * heatFactor))   // reactivity geht QUADRATISCH ein
cooling    = 2 + round(n * coolingContactsPerCore * 8)
```
Sonderfall (nicht assembliert oder `activeCores ≤ 0`): `(0, 0, 2, capacity, maxHeat)`.

**`capacity(layout)`** = `max(1, coreCount) · 100 000` (Overflow-sicher via `multiplyExact`).
**`maxHeat(layout)`** = `1 600 + max(1, coreCount) · 200`.

### `block/MultiblockReactorControllerBlock`
**Blockstate:** `FACING`, `ASSEMBLED`, `LIT`, `POWERED`.
- `getAnalogOutputSignal` → `BE.getComparatorLevel()`; `neighborChanged` → `POWERED`.
- `useWithoutItem`:
  - **mit `REACTOR_WRENCH`:** `ASSEMBLED` → `disassemble` (Meldung `akw.multiblock.disassembled`);
    sonst `tryAssemble` (Erfolg: `akw.multiblock.assembled` mit coreCount + connectedCoolingPipeCount;
    Fehler: `getLastAssemblyError()`).
  - **ohne Wrench:** `ASSEMBLED` → GUI öffnen; sonst Meldung `akw.multiblock.need_wrench`.

### `block/entity/MultiblockReactorControllerBlockEntity`
`implements ImplementedInventory, WorldlyContainer, MenuProvider`. Inventar 2 Slots (Brennstoff/Abfall).
Energiespeicher `MutableEnergyStorage(20 000 000, 0, 32 768)` — effektive Kapazität wird per Layout
über `effectiveCapacity()` = `ReactorSimulation.capacity(layout)` begrenzt.

Zustand: `layout`, `lastAssemblyError`, `activeCores`, `burnTime`, `burnTimeTotal`, `heat`,
`revalidateTimer`, `redstoneMode`, `comparatorMode`.

- `tryAssemble` → `ReactorValidator.find`; bei Erfolg `layout` setzen, `heat=0`, `ASSEMBLED=true`.
- `disassemble` → `layout=EMPTY`, alle Laufzeitwerte 0, `ASSEMBLED=LIT=false`.
- **`tick`** (nur wenn `ASSEMBLED`):
  - Alle **100 Ticks** Revalidierung (`ReactorValidator.validate` mit aktueller `outerSize`);
    schlägt sie fehl → `disassemble` (Selbstabschaltung).
  - `EMERGENCY_STOP` analog Einblock-Reaktor (`burnTime=0`, `activeCores=0`).
  - **Brennen:** Energie += `stats.generationPerTick()` (gedeckelt auf `stats.capacity()`).
  - **Zünden** (`canIgnite`): `consumeFuelBatch()` startet so viele Kerne, wie Brennstäbe **und**
    freier Abfallplatz vorhanden sind (`min(coreCount, fuelCount, wasteRoom)`); verbraucht
    so viele Brennstäbe und erzeugt so viele Spent-Rods. `burnTime = BURN_TICKS (2400)`.
  - **Hitze:** `heat += stats.heatPerTick()` (wenn vorher aktiv); `heat -= stats.coolingPerTick()`.
  - **Explosion:** bei `heat ≥ effectiveMaxHeat()` → `explode` (Stärke `4 + outerSize`, disassembliert).
  - **Auto-Abschaltung** bei `heat ≥ effectiveMaxHeat·9/10` (90 %): `burnTime=0`, `activeCores=0`.
  - **Abgabe:** `EnergyNet.pushToNeighbors(..., max(160, generationPerTick·2))`.
  - **Strahlung:** Radius 8, `radLevel = heat ≥ maxHeat/2 ? 1 : 0`, Blei schützt (gemeinsame
    `hasLeadShielding`). **Partikel:** `ELECTRIC_SPARK`, Anzahl `min(12, 2+activeCores)`.
- **WorldlyContainer:** `UP`→Brennstoff (nur `FUEL_ROD`), `DOWN`→Abfall; `stillValid` ≤ 64 Blöcke².
- **Menü:** `MultiblockReactorScreenHandler(syncId, inv, this, propertyDelegate)`.

NBT speichert zusätzlich zum Laufzeitzustand das gesamte Layout (siehe §17), sodass nach Reload kein
erneutes Assemblieren nötig ist.

---

## 6. Bauroboter (Reactor Builder)

Automatischer Aufbau eines **3×3×3**-Multiblock-Reaktors aus dem Inventar; ein sichtbarer
Roboter (ArmorStand) setzt Block für Block.

### `block/ReactorBuilderControllerBlock`
**Blockstate:** `FACING` (`HORIZONTAL_FACING`), `ACTIVE` (`create("active")`), `POWERED`.
- `getAnalogOutputSignal` → `BE.getComparatorLevel()` (Baufortschritt 0–15).
- `neighborChanged` → `POWERED` (Redstone **pausiert** den Bau).
- `useWithoutItem`:
  - **Shift-Rechtsklick** → `toggleBuilding(...)`, Status in der Actionbar.
  - **Rechtsklick** → öffnet das 27-Slot-Inventar-GUI.

### `block/entity/ReactorBuilderControllerBlockEntity`
`implements ImplementedInventory, WorldlyContainer, MenuProvider`.

**Konstanten:** `SIZE=27`, `CAPACITY=1 000 000`, `MAX_INSERT=4 096`, `ENERGY_PER_BLOCK=500`,
`BUILD_INTERVAL=5` (Ticks zwischen Bauschritten). Energiespeicher
`MutableEnergyStorage(CAPACITY, MAX_INSERT, 0)` — **nur Input** (Extract 0).

**Bauplan (`createPlan`):** 27 Schritte über `dx,dy ∈ {-1,0,1}`, `dz ∈ {0,1,2}`,
`controllerPos = builderPos.relative(builderFacing, 2)`. Reihenfolge: zuerst alle Hüllenblöcke
(`REACTOR_CASING`), dann der Kern (`REACTOR_CORE`, Mitte), zuletzt der `MULTIBLOCK_REACTOR_CONTROLLER`
(mit `FACING = reactorFacing = builderFacing.getOpposite()`).

**Ablauf:**
- `toggleBuilding` startet/pausiert; bei Start prüft `validateStart` Blockaden, Material und Energie.
- `tick` (wenn `building` && nicht `POWERED`): alle 5 Ticks `buildNext` — platziert **genau einen**
  Block, kostet ein passendes Item + 500 FE, bewegt den Roboter, aktualisiert Komparator.
  Stop-Gründe: Blockade, fehlendes Material, zu wenig Energie.
- `finish`: validiert die fertige Struktur (`ReactorValidator.validate(..., 3)`) → Status `complete`
  oder `invalid`.
- **Komparator:** `0` vor Start, sonst `min(15, max(1, buildIndex·15/27))`.

**Status-Keys** (`statusKey`, init `akw.builder.status.idle`):
`idle, paused, building, blocked, materials, energy, complete, invalid`.
Feedback-Meldungen: `akw.builder.{started, paused, blocked, missing_materials, missing_energy, robot}`.

**Roboter (ArmorStand):** `ensureRobot` spawnt bei `controllerPos + (0.5,1,0.5)` mit
`NoGravity`, `Invulnerable`, `ShowArms`, `NoBasePlate`, CustomName `akw.builder.robot`;
HEAD = Builder-Block-Item, MAINHAND = `REACTOR_WRENCH`. `releaseRobot`/`preRemoveSideEffects`
entfernen ihn beim Abbau.

**WorldlyContainer:** alle 27 Slots; einsetzbar nur Block-Items `REACTOR_CASING`, `REACTOR_CORE`,
`MULTIBLOCK_REACTOR_CONTROLLER`; Entnahme nur wenn **nicht** gerade gebaut wird.
**Menü:** `ChestMenu(GENERIC_9x3, …, 3)`.

---

## 7. Energie-Infrastruktur

### `EnergyCableBlock` / `EnergyCableBlockEntity`
`CAPACITY=8 192`, `TRANSFER=2 048`. Speicher `MutableEnergyStorage(CAPACITY, TRANSFER)`.
`tick` (server-only) → `EnergyNet.pushToNeighbors(..., TRANSFER)`. Nimmt/gibt FE über alle Seiten,
keine Blockstates, keine Interaktion. NBT-Key `Energy`.

### `EnergyBatteryBlock` / `EnergyBatteryBlockEntity`
`CAPACITY=1 000 000`, `TRANSFER=4 096`. `tick` → push + Komparator-Update.
`getAnalogOutputSignal` → `getComparatorLevel()`:
`energy ≤ 0 ? 0 : max(1, energy·15/capacity)`. NBT-Key `Energy`; nach Laden
`lastComparator = getComparatorLevel()`.

---

## 8. Abfallbehälter

### `WasteContainerBlock` / `WasteContainerBlockEntity`
`implements ImplementedInventory, WorldlyContainer, MenuProvider`. `SIZE=9`, `RADIATION_RADIUS=5`.
Inventar 9 Slots (1×9, `ChestMenu(GENERIC_9x1, …, 1)`). `useWithoutItem` öffnet das GUI.

- **Komparator:** Summe aller Stacks `total`; `total ≤ 0 ? 0 : max(1, total·15/(9·64))` (576 Items = 15).
- **Tick** (server-only): Komparator-Update; bei Signal **≥ 8** (≙ > 50 %):
  - alle 20 Ticks `GLOW_SQUID_INK`-Partikel über dem Block;
  - **passive Strahlung** Level 0 im Radius 5 (60 Ticks), Blei schützt pfadbasiert.
- **WorldlyContainer:** Einsetzen nur `SPENT_FUEL_ROD` (von jeder Seite), Entnahme nur nach unten.
- NBT: Standard-Items-Format (`ContainerHelper`, Key `Items`).

---

## 9. Energiesystem

System: **NeoForge-native Transfer-API** (`Capabilities.Energy` / FE) — kein externer Dependency.
Push-Modell: Erzeuger/Speicher schieben aktiv pro Tick an alle 6 Nachbarn.

### `energy/EnergyNet` (Utility)
`pushToNeighbors(EnergyHandler source, Level level, BlockPos pos, int limit)`:
Frühausstieg bei leer; iteriert alle 6 Seiten; holt
`Capabilities.Energy.BLOCK.getCapability(level, pos.relative(side), null, null, side.getOpposite())`;
Transfer in `Transaction.openRoot()` via `EnergyHandlerUtil.move(source, target, limit, tx)`,
commit bei `moved > 0`. `limit` = max. FE pro Seite und Tick.

### `block/entity/MutableEnergyStorage extends SimpleEnergyHandler`
Energie-Facade, direkt unter `Capabilities.Energy.BLOCK` registrierbar.
- Konstruktoren: `(capacity, maxTransfer)` (Receive=Extract) und `(capacity, maxReceive, maxExtract)`.
- `setOnChange(Runnable)` — Dirty-Callback (typisch `BlockEntity::setChanged`), via überschriebenes
  `onEnergyChanged(int diff)`.
- `getEnergyStored()`, `getMaxEnergyStored()`, `setEnergy(int)` (clamp `[0, capacity]`), `getMaxExtract()`.

### `block/entity/ImplementedInventory extends Container`
Minimal-Container über `NonNullList<ItemStack> getItems()`; Default-Implementierungen für
`getContainerSize/getItem/setItem/removeItem/…`. Factory `of(items)`.

---

## 10. Strahlung & Effekt

### `effect/RadiationEffect extends MobEffect`
Kategorie `HARMFUL`, Farbe `0x39D353` (Grün). Keine eigene Tick-Logik — die Wirkung wird von den
Reaktoren/Abfallbehältern beim Anwenden gesetzt. Registriert als `akw:radiation` (`ModEffects`),
Übersetzungsschlüssel `effect.akw.radiation`.

### Strahlungsmechanik
- **Reaktor (Einblock & Multiblock):** laufender Reaktor bestrahlt Spieler im Radius **8**.
  `radLevel = heat ≥ maxHeat/2 ? 1 : 0`. Effekt `RADIATION` (60 Ticks); zusätzlich alle 20 Ticks
  `magic`-Schaden **0,5 HP** (Stufe 0) bzw. **1,5 HP** (Stufe 1). Ein `LEAD_BLOCK` auf dem direkten
  Pfad schützt vollständig.
- **Abfallbehälter:** Radius **5**, Stufe 0, ab Füllstand > 50 % (siehe §8).

---

## 11. Steuerung: Redstone- & Komparator-Modi

Beide Reaktor-Typen (Einblock & Multiblock) sind im GUI über zwei Buttons konfigurierbar.
Werte werden per NBT gespeichert (`RedstoneMode`, `ComparatorMode`, Ordinal).

### `reactor/RedstoneMode`
Reaktion auf ein Redstone-Signal (`POWERED`). `next()` schaltet zyklisch weiter.

| Wert | Key | `canIgnite` (neuen Stab zünden) |
|---|---|---|
| `IGNORED` | `akw.redstone_mode.ignored` | immer |
| `HIGH_ENABLES` | `akw.redstone_mode.high_enables` | nur bei Signal |
| `HIGH_DISABLES` | `akw.redstone_mode.high_disables` *(Default)* | nur ohne Signal |
| `EMERGENCY_STOP` | `akw.redstone_mode.emergency_stop` (EN „SCRAM") | nur ohne Signal; Signal stoppt den **laufenden** Stab sofort |

### `reactor/ComparatorMode`
Quelle des Komparator-Ausgangs (0–15). `next()` zyklisch. Default `ENERGY`.

| Wert | Key | Signalquelle |
|---|---|---|
| `ENERGY` | `akw.comparator_mode.energy` | Energiefüllstand |
| `TEMPERATURE` | `akw.comparator_mode.temperature` | Hitze / maxHitze |
| `FUEL` | `akw.comparator_mode.fuel` | Brennstoff-Slot |
| `WASTE` | `akw.comparator_mode.waste` | Abfall-Slot |

Formel je Modus: `quelle ≤ 0 ? 0 : max(1, wert·15/bezug)`.

---

## 12. GUI / Screen-Kette

```
Rechtsklick → Block.useWithoutItem → ServerPlayer.openMenu(be, buf -> buf.writeBlockPos(pos))
   ▼  MenuType via IMenuTypeExtension.create(... buf.readBlockPos())
ScreenHandler  (Server: Container+ContainerData direkt von der BE;
   ▼            Client: über BlockPos aus der Welt aufgelöst, sonst SimpleContainer-Fallback)
NuclearReactorScreen (Client, liest Live-Werte über Handler-Getter)
```

### ContainerData-Indizes (`NuclearReactorBlockEntity`, `PROPERTY_COUNT = 8`)

| Index | Konstante | Bedeutung |
|--:|---|---|
| 0 | `IDX_ENERGY` | aktuelle Energie (auf `Integer.MAX_VALUE` geklammert) |
| 1 | `IDX_CAPACITY` | Kapazität |
| 2 | `IDX_BURN_TIME` | verbleibende Brenndauer |
| 3 | `IDX_BURN_TOTAL` | Brenndauer des aktuellen Stabs |
| 4 | `IDX_HEAT` | aktuelle Hitze |
| 5 | `IDX_MAX_HEAT` | maximale Hitze |
| 6 | `IDX_REDSTONE_MODE` | Redstone-Modus (Ordinal) |
| 7 | `IDX_COMPARATOR_MODE` | Komparator-Modus (Ordinal) |

### `screen/NuclearReactorScreenHandler`
2-Slot-Block-Inventar (`checkContainerSize(inv, 2)`).
- Brennstoff-Slot 0 bei **(80, 35)**, akzeptiert nur `FUEL_ROD`.
- Abfall-Slot 1 bei **(116, 35)**, Output-only (`mayPlace=false`).
- Spieler-Inventar `x=8+col·18, y=84+row·18`; Hotbar `y=142`.
- `clickMenuButton`: **id 0** → Redstone-Modus +1; **id 1** → Komparator-Modus +1.
- Client-Fallback: `SimpleContainer(2)` / `SimpleContainerData(8)`.

`MultiblockReactorScreenHandler` erbt davon; Delegate via `be.getPropertyDelegate()`,
Fallback auf `MultiblockReactorControllerBlockEntity`.

### `screen/NuclearReactorScreen`
Textur `akw:textures/gui/nuclear_reactor.png` (Atlas 256×256), `blit` mit `RenderPipelines.GUI_TEXTURED`.
Zwei Buttons (relativ zu `leftPos/topPos`): Redstone-Button `(7, 17, 71×20)` → Klick id 0;
Komparator-Button `(7, 40, 71×20)` → Klick id 1; Labels jeden Frame aus dem aktuellen Modus.

Balken (relativ, von unten gefüllt):

| Element | x | y | B×H | Farbe |
|---|--:|--:|---|---|
| Energie | 153 | 17 | 12×52 | `0xFF3CC850` grün |
| Hitze (Hintergrund) | 137 | 17 | 12×52 | `0xFF202020` |
| Hitze (Füllung) | 137 | 17 | 12×52 | `0xFFE0902C` orange / `0xFFE03030` rot ab `heatFrac ≥ 0.75` |
| Brenn-Anzeige | 72 | 34 | 4×18 | `0xFFE0902C` orange |

Tooltips: Energie-Hitbox `x∈[152,166), y∈[16,70)` → „<E> / <Cap> FE";
Hitze-Hitbox `x∈[136,150)` → „<Heat> / <maxHeat> Hitze".

---

## 13. Worldgen

`worldgen/ModWorldGen` ist ein No-Op (loggt nur). Worldgen läuft vollständig über JSON.

- **Configured Feature** `akw:uranium_ore` (`type minecraft:ore`, `size 9`,
  `discard_chance_on_air_exposure 0.0`): Stein → `uranium_ore`, Tiefenschiefer → `deepslate_uranium_ore`.
- **Placed Feature** `akw:uranium_ore`: `count 4` pro Chunk, `in_square`,
  `height_range trapezoid` von **y −64 bis 32**, `biome`.
- **BiomeModifier** `akw:add_uranium_ore` (`neoforge:add_features`): Biome `#minecraft:is_overworld`,
  Feature `akw:uranium_ore`, Step `underground_ores`.

Loot/Tags: Erze in `mineable/pickaxe` und `needs_iron_tool` (mind. Eisen-Spitzhacke); Drop = Roh-Uran
(Silk Touch → Block, Fortune erhöht).

---

## 14. Datengenerierung & Rezepte

### `datagen/AkwDataGenerator` (`@EventBusSubscriber`)
- **`GatherDataEvent.Client`:** `ModModelProvider`, `ModLanguageProvider.German`, `…English`.
- **`GatherDataEvent.Server`:** `ModRecipeProvider`, `ModTagsProvider`, `LootTableProvider`
  (SubProvider `ModLootTableProvider`, `LootContextParamSets.BLOCK`),
  `AdvancementProvider` (`ModAdvancementProvider`).

Ausgabe → `src/main/generated/` (committet). Texturen werden in `src/main/resources/` von Hand gepflegt.

> ⚠️ `runClientData` und `runServerData` schreiben beide nach `src/main/generated` und purgen sich
> gegenseitig — `data/`-Ebene dazwischen sichern (siehe [NEOFORGE-MIGRATION.md](NEOFORGE-MIGRATION.md) §6).

### Rezepte (`ModRecipeProvider`) — IDs `akw:<path>`

**Verarbeitung**
| Rezept | Typ / Muster | Schlüssel |
|---|---|---|
| `uranium_ingot_from_smelting` | Schmelzen 200 t, exp 0.7 | Roh-Uran → Uran-Barren |
| `uranium_ingot_from_blasting` | Schmelzofen 100 t, exp 0.7 | Roh-Uran → Uran-Barren |
| `enriched_uranium` | `UU` | U=Uran-Barren |
| `fuel_rod` | `E`/`E`/`E` | E=Angereichertes Uran |

**Reaktoren**
| Rezept | Muster | Schlüssel |
|---|---|---|
| `nuclear_reactor` | `III`/`UFU`/`IRI` | I=Eisen, U=Uran-Barren, F=Ofen, R=Redstone |
| `advanced_nuclear_reactor` | `GCG`/`CNC`/`GCG` | G=Gold, C=Steuerstab-Block, N=Reaktor |
| `elite_nuclear_reactor` | `DCD`/`CAC`/`DCD` | D=Diamant, C=Kühlrohr, A=Fortgeschr. Reaktor |
| `breeder_reactor` | `CCC`/`UNU`/`CCC` | C=Kupfer, U=Angereich.-Uran-Block, N=Reaktor |
| `thorium_reactor` | `MEM`/`ENE`/`MEM` | M=Smaragd, E=Angereich.-Uran-Block, N=Reaktor |
| `fusion_reactor` | `NDN`/`DED`/`NDN` | N=Netherit-Barren, D=Diamantblock, E=Elite-Reaktor |

**Bausteine**
| Rezept | Muster | Schlüssel | Ausbeute |
|---|---|---|--:|
| `reactor_core` | `UUU`/`UFU`/`UUU` | U=Uran-Barren, F=Brennstab | 1 |
| `control_rod_block` | `IUI`/`IUI`/`IUI` | I=Eisen, U=Uran-Barren | 1 |
| `cooling_pipe` | `C C`/`C C`/`C C` | C=Kupfer | 2 |
| `lead_block` | `IUI`/`UIU`/`IUI` | I=Eisen, U=Uran-Barren | 1 |
| `waste_container` | `IFI`/`IFI`/`III` | I=Eisen, F=Brennstab | 1 |
| `enriched_uranium_block` | `UUU`/`UUU`/`UUU` | U=Uran-Barren | 1 |

**Multiblock & Energie**
| Rezept | Muster | Schlüssel | Ausbeute |
|---|---|---|--:|
| `reactor_wrench` | ` I`/`IS` | I=Eisen, S=Stock | 1 |
| `reactor_casing` | `ILI`/`LIL`/`ILI` | I=Eisen, L=Blei-Block | 4 |
| `multiblock_reactor_controller` | `CRC`/`RNR`/`CRC` | C=Reaktor-Gehäuse, R=Redstone-Block, N=Reaktor | 1 |
| `reactor_builder_controller` | `CRC`/`EBE`/`CRC` | C=Gehäuse, R=Redstone-Block, E=Kabel, B=Akku | 1 |
| `energy_cable` | `CRC` | C=Kupfer, R=Redstone | 3 |
| `energy_battery` | `ICI`/`RRR`/`ICI` | I=Eisen, C=Kupfer, R=Redstone | 1 |

### Advancements (`ModAdvancementProvider`, `data/akw/advancement/story/`)
9-stufige Kette: `mine_uranium → smelt_uranium → enrich → fuel_rod → first_reactor → energy_online`,
plus Challenges `elite_reactor`, `fusion_reactor`, `multiblock`.

---

## 15. Sounds

`registry/ModSounds` registriert drei Events via `SoundEvent.createVariableRangeEvent`:
`reactor_ambient`, `reactor_alert`, `reactor_meltdown`. In `assets/akw/sounds.json` sind alle
`sounds`-Arrays **leer** — nur Untertitel (`subtitles.akw.*`) sind hinterlegt; es fehlen die
`.ogg`-Dateien, die Sounds bleiben stumm.

---

## 16. Creative-Tab & Lokalisierung

### `registry/ModItemGroups`
Tab `akw:akw`, Icon `RAW_URANIUM`, Titel `itemgroup.akw`. Inhalt: Items, dann Multiblock-/Builder-
Controller, Erze, `REACTORS`, `DECOR`, Kabel, Akku. Zusätzlich Injektion in Vanilla-Tabs:
`INGREDIENTS` (Items), `FUNCTIONAL_BLOCKS` (Controller + Reaktoren), `NATURAL_BLOCKS` (Erze),
`BUILDING_BLOCKS` (`DECOR`), `REDSTONE_BLOCKS` (Kabel, Akku).

### `datagen/ModLanguageProvider`
Inner-Klassen `German` (`de_de`) / `English` (`en_us`). Liefert Namen aller Items/Blöcke,
Redstone-/Komparator-Modi, Builder-Status, Multiblock-Meldungen, Sound-Untertitel und die 9
Advancement-Paare `advancements.akw.<id>.{title,desc}`.

---

## 17. NBT-Schlüssel — Übersicht

| BlockEntity | Keys |
|---|---|
| `NuclearReactorBlockEntity` | `Items` (Inventar), `Energy`, `BurnTime`, `BurnTimeTotal`, `Heat`, `RedstoneMode`, `ComparatorMode` |
| `MultiblockReactorControllerBlockEntity` | wie oben + `ActiveCores`, `ReactorSize`, `CoreCount`, `ControlRodCount`, `CoolingPipeCount`, `ConnectedCoolingPipeCount`, `CoreNeighborContacts`, `CoreControlRodContacts`, `CoreCoolingContacts` |
| `ReactorBuilderControllerBlockEntity` | `Items`, `Energy`, `BuildIndex`, `BuildCooldown`, `Building`, `StatusKey`, `RobotUuidMost`, `RobotUuidLeast` |
| `EnergyCableBlockEntity` / `EnergyBatteryBlockEntity` | `Energy` |
| `WasteContainerBlockEntity` | `Items` |

---

*Diese Referenz spiegelt den Codestand von Mod-Version 1.2.0. Bei Code-Änderungen die betroffenen
Abschnitte mitführen — insbesondere Tier-Werte (§4), Simulationsformeln (§5) und NBT-Keys (§17).*
