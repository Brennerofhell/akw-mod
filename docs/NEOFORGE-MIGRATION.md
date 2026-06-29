# Fabric → NeoForge — Migrationsreferenz

Technische Referenz der Portierung der AKW-Mod von **Fabric (Yarn-Mappings, Team Reborn Energy)**
auf **NeoForge 21.10.64** (Minecraft **1.21.10**, Mojang-Mappings, Java 21).

Diese Datei hält das in der Migration erarbeitete API-Wissen fest — sie ist sowohl Nachschlagewerk
für künftige Updates als auch Checkliste, falls weitere Fabric-Reste auftauchen. Alle Signaturen
wurden gegen das tatsächliche NeoForge-21.10.64-Classpath-Jar (`javap`) verifiziert, nicht geraten.

> ⚠️ NeoForge 21.10 ist eine sehr aktuelle Version mit mehreren API-Umbauten gegenüber früheren
> 1.21.x-Releases (Energie-/Transfer-API, Datagen-Event-Split, Block-Removal-Lifecycle,
> RenderPipeline-basiertes GUI-Rendering). Ältere NeoForge-Tutorials passen oft nicht 1:1.

---

## 1. Build & Projekt-Setup

| Bereich | Fabric (vorher) | NeoForge 21.10.64 (jetzt) |
|---|---|---|
| Gradle-Plugin | `fabric-loom` | `net.neoforged.moddev` v2.0.141 |
| Mappings | Yarn | Mojang (offiziell) |
| Mod-Metadaten | `src/main/resources/fabric.mod.json` | `src/main/resources/META-INF/neoforge.mods.toml` |
| Einstiegspunkt | `ModInitializer` / `ClientModInitializer` | `@Mod(MOD_ID)`-Klasse + `@EventBusSubscriber` |
| Energie-Lib | Team Reborn Energy (extern) | **NeoForge-nativ** (kein externer Dependency) |
| Java | 21 | 21 |
| Datagen-Run | `./gradlew runDatagen` | `./gradlew runClientData` + `runServerData` |
| Jar-Ausgabe | `build/libs/` | `build/libs/neoforge/` |

`gradle.properties`: `neoforge_version=21.10.64`, `mod_version=1.2.0`, `maven_group=ch.danielt.akw`.

---

## 2. Yarn → Mojang Mapping (Klassen)

| Yarn (Fabric) | Mojang (NeoForge) |
|---|---|
| `net.minecraft.world.World` | `net.minecraft.world.level.Level` |
| `net.minecraft.server.world.ServerWorld` | `net.minecraft.server.level.ServerLevel` |
| `net.minecraft.block.Block` | `net.minecraft.world.level.block.Block` |
| `net.minecraft.block.BlockState` | `net.minecraft.world.level.block.state.BlockState` |
| `net.minecraft.block.entity.BlockEntity` | `net.minecraft.world.level.block.entity.BlockEntity` |
| `net.minecraft.util.math.BlockPos` | `net.minecraft.core.BlockPos` |
| `net.minecraft.util.math.Direction` | `net.minecraft.core.Direction` |
| `net.minecraft.util.math.Box` | `net.minecraft.world.phys.AABB` |
| `net.minecraft.util.math.Vec3d` | `net.minecraft.world.phys.Vec3` |
| `net.minecraft.util.collection.DefaultedList` | `net.minecraft.core.NonNullList` |
| `net.minecraft.text.Text` | `net.minecraft.network.chat.Component` |
| `net.minecraft.item.ItemStack` / `Item` / `Items` | `net.minecraft.world.item.*` |
| `net.minecraft.entity.player.PlayerEntity` | `net.minecraft.world.entity.player.Player` |
| `net.minecraft.entity.player.PlayerInventory` | `net.minecraft.world.entity.player.Inventory` |
| `net.minecraft.entity.decoration.ArmorStandEntity` | `net.minecraft.world.entity.decoration.ArmorStand` |
| `net.minecraft.entity.EquipmentSlot` | `net.minecraft.world.entity.EquipmentSlot` |
| `net.minecraft.entity.effect.StatusEffectInstance` | `net.minecraft.world.effect.MobEffectInstance` |
| `net.minecraft.particle.ParticleTypes` | `net.minecraft.core.particles.ParticleTypes` |
| `net.minecraft.inventory.SidedInventory` | `net.minecraft.world.WorldlyContainer` |
| `net.minecraft.inventory.Inventories` | `net.minecraft.world.ContainerHelper` |
| `net.minecraft.screen.ScreenHandler` | `net.minecraft.world.inventory.AbstractContainerMenu` |
| `net.minecraft.screen.PropertyDelegate` | `net.minecraft.world.inventory.ContainerData` |
| `net.minecraft.screen.ScreenHandlerType` | `net.minecraft.world.inventory.MenuType` |
| `net.minecraft.screen.NamedScreenHandlerFactory` | `net.minecraft.world.MenuProvider` |
| `net.minecraft.screen.GenericContainerScreenHandler` | `net.minecraft.world.inventory.ChestMenu` |
| `net.minecraft.storage.ReadView` / `WriteView` | `net.minecraft.world.level.storage.ValueInput` / `ValueOutput` |
| `net.minecraft.util.Identifier` | `net.minecraft.resources.ResourceLocation` |

## 2b. Yarn → Mojang Mapping (Methoden)

| Yarn | Mojang |
|---|---|
| `markDirty()` | `setChanged()` |
| `world.isClient()` | `level.isClientSide()` |
| `getCachedState()` | `getBlockState()` |
| `getTranslationKey()` | `getDescriptionId()` |
| `state.get(prop)` / `state.with(prop, v)` | `state.getValue(prop)` / `state.setValue(prop, v)` |
| `world.setBlockState(pos, state, Block.NOTIFY_ALL)` | `level.setBlock(pos, state, Block.UPDATE_ALL)` |
| `world.updateComparators(pos, block)` | `level.updateNeighbourForOutputSignal(pos, block)` |
| `world.createExplosion(…, ExplosionSourceType.BLOCK)` | `level.explode(…, Level.ExplosionInteraction.BLOCK)` |
| `stack.isOf(item)` / `state.isOf(block)` | `stack.is(item)` / `state.is(block)` |
| `stack.getMaxCount()` | `stack.getMaxStackSize()` |
| `stack.decrement(n)` / `increment(n)` | `stack.shrink(n)` / `grow(n)` |
| `item.getDefaultStack()` | `new ItemStack(item)` |
| `player.squaredDistanceTo(…)` | `player.distanceToSqr(…)` |
| `player.getBlockPos()` | `player.blockPosition()` |
| `player.addStatusEffect(new StatusEffectInstance(…))` | `player.addEffect(new MobEffectInstance(…))` |
| `player.damage(world, source, amount)` | `player.hurt(source, amount)` |
| `world.getEntitiesByClass(…)` | `level.getEntitiesOfClass(…)` |
| `world.getDamageSources()` | `level.damageSources()` |
| `world.getTime()` | `level.getGameTime()` |
| `serverWorld.spawnParticles(…)` | `serverLevel.sendParticles(…)` |
| `serverWorld.spawnEntity(e)` | `serverLevel.addFreshEntity(e)` |
| `Vec3d.ofCenter(pos)` | `Vec3.atCenterOf(pos)` |
| `new Box(pos).expand(r)` | `new AABB(pos).inflate(r)` |
| `dir.rotateYClockwise()` | `dir.getClockWise()` |
| `dir.getOffsetX/Y/Z()` | `dir.getStepX/Y/Z()` |
| `pos.offset(dir[, n])` | `pos.relative(dir[, n])` |
| `pos.up()` / `pos.toImmutable()` | `pos.above()` / `pos.immutable()` |
| `entity.getUuid()` | `entity.getUUID()` |
| `armorStand.equipStack(slot, stack)` | `armorStand.setItemSlot(slot, stack)` |
| `armorStand.requestTeleport(x,y,z)` | `armorStand.teleportTo(x,y,z)` |
| `armorStand.setHideBasePlate(b)` | `armorStand.setNoBasePlate(b)` |

### Container / ScreenHandler (`SidedInventory` → `WorldlyContainer`)

| Yarn | Mojang |
|---|---|
| `canPlayerUse(PlayerEntity)` | `stillValid(Player)` |
| `getAvailableSlots(Direction)` | `getSlotsForFace(Direction)` |
| `canInsert(slot, stack, dir)` | `canPlaceItemThroughFace(slot, stack, dir)` |
| `canExtract(slot, stack, dir)` | `canTakeItemThroughFace(slot, stack, dir)` |

### NBT-Persistenz (`ReadView`/`WriteView` → `ValueInput`/`ValueOutput`)

| Yarn | Mojang |
|---|---|
| `writeData(WriteView)` / `readData(ReadView)` | `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)` |
| `view.putInt(k, v)` / `putLong`/`putBoolean`/`putString` | `output.putInt(k, v)` / … (identische Namen) |
| `view.getInt(k, default)` | `input.getIntOr(k, default)` (analog `getLongOr`, `getBooleanOr`, `getStringOr`) |
| `Inventories.writeData(view, list)` | `ContainerHelper.saveAllItems(output, list)` |
| `Inventories.readData(view, list)` | `ContainerHelper.loadAllItems(input, list)` |

### Stolperstein: `new ItemStack(DeferredItem)` ist mehrdeutig

`DeferredItem` implementiert **sowohl** `ItemLike` als auch `Holder<Item>`, daher ist
`new ItemStack(ModItems.X)` zwischen `ItemStack(ItemLike)` und `ItemStack(Holder<Item>)` mehrdeutig.
Lösung: `.get()` benutzen → `new ItemStack(ModItems.X.get())` (eindeutig `ItemLike`).
(Bei `DeferredBlock` tritt das nicht auf, da es nur `Holder<Block>` ist.)

---

## 3. Energiesystem — kompletter Umbau (NeoForge Transfer-API)

NeoForge 21.10 hat das alte Energie-Capability ersetzt:

| vorher (ältere NeoForge / Team Reborn) | jetzt (21.10) |
|---|---|
| `Capabilities.EnergyStorage.BLOCK` | `Capabilities.Energy.BLOCK` |
| `IEnergyStorage` (deprecated for removal) | `net.neoforged.neoforge.transfer.energy.EnergyHandler` |
| `team.reborn.energy.api.base.SimpleEnergyStorage` | `net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler` |

`Capabilities.Energy.BLOCK` ist ein `BlockCapability<EnergyHandler, Direction>`.

**`EnergyHandler`** (transaktionsbasiert):
- `getAmountAsInt()` / `getCapacityAsInt()` (auch `…AsLong`)
- `insert(int, TransactionContext)` / `extract(int, TransactionContext)`

**`SimpleEnergyHandler(capacity, maxInsert, maxExtract)`** — mutierbarer Speicher mit `set(int)` und dem
geschützten Hook `onEnergyChanged(int)` (für „dirty"-Markierung).

### Projektlösung: `MutableEnergyStorage` als Facade
`MutableEnergyStorage extends SimpleEnergyHandler` ist damit selbst ein `EnergyHandler` (direkt unter
`Capabilities.Energy.BLOCK` registrierbar) und bietet eine schlanke, IEnergyStorage-artige Fassade,
damit die BlockEntities unverändert bleiben:
- `getEnergyStored()` → `getAmountAsInt()`
- `getMaxEnergyStored()` → `getCapacityAsInt()`
- `setEnergy(int)` → `set(clamp(…))`
- `getMaxExtract()` → geschütztes Feld `maxExtract`
- `setOnChange(Runnable)` + Override `onEnergyChanged` → ruft den Callback (z. B. `this::setChanged`).
  Wichtig: externes Laden läuft jetzt über `insert(…)`, **nicht** mehr über ein überschriebenes
  `receiveEnergy` — daher den Dirty-Callback an `onEnergyChanged` hängen.

### `EnergyNet` (FE-Verteilung)
```java
EnergyHandler target = Capabilities.Energy.BLOCK.getCapability(level, neighborPos, null, null, side.getOpposite());
try (Transaction tx = Transaction.openRoot()) {
    int moved = EnergyHandlerUtil.move(source, target, limit, tx);
    if (moved > 0) tx.commit();
}
```
(`Level` selbst hat **kein** `getCapability` — `BlockCapability.getCapability(level, pos, state, be, ctx)` nutzen.)

### Capability-Registrierung (`AkwMod#registerCapabilities`)
```java
event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.X.get(), (be, side) -> be.energyStorage);
```

---

## 4. Block & BlockEntity — 1.21.10-Lifecycle

- **`BlockEntityType.Builder` entfernt** → direkter Konstruktor:
  ```java
  BLOCK_ENTITIES.register("name", () -> new BlockEntityType<>(MyBlockEntity::new, ModBlocks.A.get(), ModBlocks.B.get()));
  ```
- **`getAnalogOutputSignal` hat einen vierten Parameter** `Direction`:
  `getAnalogOutputSignal(BlockState, Level, BlockPos, Direction)`.
- **`onRemove` ist weg.** Ersatz im Block: `affectNeighborsAfterRemoval(BlockState, ServerLevel, BlockPos, boolean)`
  (nur Nachbar-Updates). **Inventar-Dropping ist automatisch**: `BlockEntity.preRemoveSideEffects(BlockPos, BlockState)`
  wirft den Inhalt jedes BE ab, das `Container` implementiert. → Block-`onRemove`-Overrides ersatzlos entfernen.
  Eigene Aufräumlogik (z. B. Bauroboter-Freigabe) gehört in einen `preRemoveSideEffects`-Override der BlockEntity
  (`super.preRemoveSideEffects(...)` zuerst, dann eigene Logik) — dort lebt das BE noch.
- **Menü öffnen**: `serverPlayer.openMenu(menuProvider, buf -> buf.writeBlockPos(pos))`; Client-Konstruktion via
  `IMenuTypeExtension.create((id, inv, buf) -> new …(id, inv, buf.readBlockPos()))` im `ModScreenHandlers`-Register.

### Registrierungs-Fix: „Item id not set"
In 1.21.x muss die `Item.Properties` eine Registry-ID tragen. `new BlockItem(block, new Item.Properties())`
bringt **keine** ID mit → Crash beim Mod-Laden (`NullPointerException: Item id not set`). Lösung:
```java
ITEMS.registerSimpleBlockItem(name, block);   // setzt die ID automatisch
```

---

## 5. Client / GUI (1.21.10 RenderPipeline)

- **`GuiGraphics.blit`** verlangt jetzt eine `RenderPipeline`:
  ```java
  graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, w, h, 256, 256);
  ```
- **`renderTooltip(Font, Component, x, y)` entfernt** → `graphics.setTooltipForNextFrame(font, component, x, y)`.
- **`@EventBusSubscriber` ohne `bus`-Parameter** (`EventBusSubscriber.Bus` ist nach der Bus-Unifizierung weg):
  `@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)`.
- Screen-Registrierung: `RegisterMenuScreensEvent` (Client-Mod-Bus).

---

## 6. Datagen — Fabric → NeoForge `GatherDataEvent`

Entrypoint ist jetzt ein `@EventBusSubscriber` mit zwei Methoden (21.10 trennt Client/Server):

```java
@EventBusSubscriber(modid = AkwMod.MOD_ID)
public final class AkwDataGenerator {
    @SubscribeEvent static void onGatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModLanguageProvider.German::new);
        event.createProvider(ModLanguageProvider.English::new);
    }
    @SubscribeEvent static void onGatherServerData(GatherDataEvent.Server event) {
        event.createProvider(ModRecipeProvider::new);
        event.createProvider(ModTagsProvider::new);
        event.createProvider((output, lookup) -> new LootTableProvider(output, Set.of(),
            List.of(new LootTableProvider.SubProviderEntry(ModLootTableProvider::new, LootContextParamSets.BLOCK)), lookup));
        event.createProvider((output, lookup) -> new AdvancementProvider(output, lookup,
            List.of(new ModAdvancementProvider())));
    }
}
```
`createProvider` ist nach Arität überladen: 1-arg `(PackOutput)` → `DataProviderFromOutput`;
2-arg `(PackOutput, CompletableFuture<HolderLookup.Provider>)` → `…Lookup`.

| Provider | Fabric-Basis | NeoForge/Vanilla-Basis | Kern-API |
|---|---|---|---|
| Sprache | `FabricLanguageProvider` | `net.neoforged.neoforge.common.data.LanguageProvider` | `addTranslations()`, `add/addBlock/addItem` (DeferredBlock/Item sind `Supplier`) |
| Tags | `FabricTagProvider.BlockTagProvider` | `net.neoforged.neoforge.common.data.BlockTagsProvider` | `addTags(provider)`, `tag(key).add(block.get())` |
| Loot | `FabricBlockLootTableProvider` | `BlockLootSubProvider` (in `LootTableProvider` gewrappt) | `generate()`, `getKnownBlocks()`, `dropSelf`, `createOreDrop` |
| Rezepte | `FabricRecipeProvider`/`RecipeGenerator` | `RecipeProvider.Runner` → innerer `RecipeProvider.buildRecipes()` | `shaped`/`define`/`unlockedBy(getHasName, has)`/`save(output, key)`; `SimpleCookingRecipeBuilder.smelting/blasting`; `Ingredient.of` |
| Modelle | `FabricModelProvider` | `net.minecraft.client.data.models.ModelProvider` | `registerModels(BlockModelGenerators, ItemModelGenerators)`; `createTrivialCube`, `createFurnace(b, TexturedModel.ORIENTABLE)`, `generateFlatItem(i, ModelTemplates.FLAT_ITEM)` |
| Advancements | `FabricAdvancementProvider` | `AdvancementSubProvider` (in `AdvancementProvider` gewrappt) | `Advancement.Builder.advancement()/display(ItemLike,…,AdvancementType,…)/parent/addCriterion(InventoryChangeTrigger.TriggerInstance.hasItems)/save(consumer, name)` |

Yarn-Begriffe im Datagen: `RecipeExporter`→`RecipeOutput`, `AdvancementEntry`→`AdvancementHolder`,
`AdvancementFrame`→`AdvancementType`, `Models.GENERATED`→`ModelTemplates.FLAT_ITEM`,
`registerSimpleCubeAll`→`createTrivialCube`, `registerCooker`→`createFurnace`.

### ⚠️ Datagen ausführen — Purge-Stolperstein
`runClientData` erzeugt `assets/`, `runServerData` erzeugt `data/`. **Beide schreiben nach
`src/main/generated` und löschen jeweils die Dateien des anderen Laufs** (HashCache-Purge). Für einen
**vollständigen** Asset-Satz die `data/`-Ebene zwischen den Läufen sichern und wiederherstellen:
```bash
./gradlew runServerData
cp -r src/main/generated/data /tmp/akw_data
./gradlew runClientData
rm -rf src/main/generated/data && cp -r /tmp/akw_data src/main/generated/data
```
(`src/main/generated/.cache/` ist gitignored; der Asset-Inhalt wird committet.)

---

## 7. Build, Jars & Verifikation

```bash
./gradlew build          # kompiliert + baut das Jar nach build/libs/neoforge/
./gradlew runClient      # Client zum Testen
./gradlew runClientData  # Client-Datagen (Modelle, Sprache)
./gradlew runServerData  # Server-Datagen (Rezepte, Loot, Tags, Advancements)
```
- Aktuelles Jar: `build/libs/neoforge/akw-<version>.jar` (in den `mods/`-Ordner eines NeoForge-21.10.64-Clients).
- Alte Fabric-Jars: archiviert in `releases/fabric/` (lokal, gitignored).

Der vollständige Port wurde verifiziert: `BUILD SUCCESSFUL`, Mod lädt, beide Datagen-Läufe erfolgreich,
Assets in 1.21.10-Struktur (Reaktor-Blockstates `facing×lit`, `_on`-Modelle, Rezepte etc.).
