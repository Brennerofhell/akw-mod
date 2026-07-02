package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.MultiblockReactorControllerBlock;
import ch.danielt.akw.reactor.ComparatorMode;
import ch.danielt.akw.reactor.ReactorLayout;
import ch.danielt.akw.reactor.ReactorSimulation;
import ch.danielt.akw.reactor.ReactorValidator;
import ch.danielt.akw.reactor.RedstoneMode;
import ch.danielt.akw.reactor.ValidationError;
import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.screen.ModularReactorScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Controller und Laufzeit-Zustand des modular aufgebauten Reaktors. */
public class MultiblockReactorControllerBlockEntity extends BlockEntity
        implements ImplementedInventory, WorldlyContainer, MenuProvider {

    public static final int FUEL_SLOT = 0;
    public static final int WASTE_SLOT = 1;
    private static final int[] NO_SLOTS = {};

    private static final int IDX_ENERGY = NuclearReactorBlockEntity.IDX_ENERGY;
    private static final int IDX_CAPACITY = NuclearReactorBlockEntity.IDX_CAPACITY;
    private static final int IDX_BURN_TIME = NuclearReactorBlockEntity.IDX_BURN_TIME;
    private static final int IDX_BURN_TOTAL = NuclearReactorBlockEntity.IDX_BURN_TOTAL;
    private static final int IDX_HEAT = NuclearReactorBlockEntity.IDX_HEAT;
    private static final int IDX_MAX_HEAT = NuclearReactorBlockEntity.IDX_MAX_HEAT;
    private static final int IDX_REDSTONE_MODE = NuclearReactorBlockEntity.IDX_REDSTONE_MODE;
    private static final int IDX_COMPARATOR_MODE = NuclearReactorBlockEntity.IDX_COMPARATOR_MODE;

    // Multiblock-spezifische Zusatz-Properties (schließen an die geerbten 0–7 an).
    public static final int IDX_CORE_COUNT = 8;
    public static final int IDX_PRODUCTION = 9;
    public static final int IDX_COOLING = 10;
    public static final int IDX_CONTROL_ROD = 11;
    public static final int IDX_ENABLED = 12;
    public static final int IDX_SHUTDOWN_TEMP = 13;
    public static final int IDX_ERROR_COUNT = 14;
    public static final int IDX_SIZE_X = 15;
    public static final int IDX_SIZE_Y = 16;
    public static final int IDX_SIZE_Z = 17;
    public static final int MB_PROPERTY_COUNT = 18;

    /** Untere/obere Grenze der einstellbaren Abschalttemperatur (Prozent der Maximalhitze). */
    public static final int MIN_SHUTDOWN_TEMP = 50;
    public static final int MAX_SHUTDOWN_TEMP = 95;

    private static final int RADIATION_RADIUS = 8;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    /** Generator-Speicher; die effektive Kapazität wird vom Layout begrenzt. */
    public final MutableEnergyStorage energyStorage =
            new MutableEnergyStorage(20_000_000, 0, 32_768);

    private ReactorLayout layout = ReactorLayout.EMPTY;
    /** Letzte Validierungsfehler (transient, nur für Anzeige beim Wrench-Klick / GUI). */
    private List<ValidationError> lastErrors = List.of();
    private int activeCores;
    private int burnTime;
    private int burnTimeTotal;
    private int heat;
    private int revalidateTimer;
    private int lastComparator = -1;
    private RedstoneMode redstoneMode = RedstoneMode.HIGH_DISABLES;
    private ComparatorMode comparatorMode = ComparatorMode.ENERGY;
    /** Stufenloser Steuerstab-Einschub 0–100 % (senkt die Endreaktivität linear). */
    private int controlRodInsertion;
    /** Manueller Ein-/Aus-Schalter im GUI (aus = laufender Zyklus stoppt). */
    private boolean enabled = true;
    /** Auto-Drosselschwelle in Prozent der Maximalhitze. */
    private int shutdownTempPercent = 90;
    /** Innenraum-Schnitt für die Schichtansicht (nur Client, per Update-Tag befüllt). */
    private int @Nullable [] clientInteriorGrid;

    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case IDX_ENERGY -> energyStorage.getEnergyStored();
                case IDX_CAPACITY -> effectiveCapacity();
                case IDX_BURN_TIME -> burnTime;
                case IDX_BURN_TOTAL -> burnTimeTotal;
                case IDX_HEAT -> heat;
                case IDX_MAX_HEAT -> effectiveMaxHeat();
                case IDX_REDSTONE_MODE -> redstoneMode.ordinal();
                case IDX_COMPARATOR_MODE -> comparatorMode.ordinal();
                case IDX_CORE_COUNT -> layout.coreCount();
                case IDX_PRODUCTION -> currentStats().generationPerTick();
                case IDX_COOLING -> currentStats().coolingPerTick();
                case IDX_CONTROL_ROD -> controlRodInsertion;
                case IDX_ENABLED -> enabled ? 1 : 0;
                case IDX_SHUTDOWN_TEMP -> shutdownTempPercent;
                case IDX_ERROR_COUNT -> lastErrors.size();
                case IDX_SIZE_X -> layout.sizeX();
                case IDX_SIZE_Y -> layout.sizeY();
                case IDX_SIZE_Z -> layout.sizeZ();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case IDX_ENERGY -> energyStorage.setEnergy(value);
                case IDX_BURN_TIME -> burnTime = value;
                case IDX_BURN_TOTAL -> burnTimeTotal = value;
                case IDX_HEAT -> heat = value;
                case IDX_REDSTONE_MODE -> {
                    if (value >= 0 && value < RedstoneMode.values().length) {
                        redstoneMode = RedstoneMode.values()[value];
                        setChanged();
                    }
                }
                case IDX_COMPARATOR_MODE -> {
                    if (value >= 0 && value < ComparatorMode.values().length) {
                        comparatorMode = ComparatorMode.values()[value];
                        setChanged();
                    }
                }
                case IDX_CONTROL_ROD -> {
                    controlRodInsertion = Math.clamp(value, 0, 100);
                    setChanged();
                }
                case IDX_ENABLED -> {
                    enabled = value != 0;
                    setChanged();
                }
                case IDX_SHUTDOWN_TEMP -> {
                    shutdownTempPercent = Math.clamp(value, MIN_SHUTDOWN_TEMP, MAX_SHUTDOWN_TEMP);
                    setChanged();
                }
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return MB_PROPERTY_COUNT;
        }
    };

    public MultiblockReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIBLOCK_REACTOR_CONTROLLER.get(), pos, state);
        // FE-Entnahme läuft über die Port-Capability am Controller-Tick vorbei —
        // ohne Dirty-Markierung droht beim Chunk-Entladen ein Energie-Rollback.
        energyStorage.setOnChange(this::setChanged);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public ContainerData getPropertyDelegate() {
        return propertyDelegate;
    }

    public ReactorLayout getLayout() {
        return layout;
    }

    /** Letzte Validierungsfehler (leer, wenn noch nie validiert oder zuletzt gültig). */
    public List<ValidationError> getLastErrors() {
        return lastErrors;
    }

    /** Übersetzte Fehlermeldungen mit Koordinaten für Chat/GUI. */
    public List<Component> getLastErrorComponents() {
        return lastErrors.stream().map(ValidationError::toComponent).toList();
    }

    public boolean tryAssemble(Level level, BlockPos pos, BlockState state) {
        ReactorValidator.Result result = ReactorValidator.find(level, pos);
        lastErrors = result.errors();
        if (!result.valid()) {
            syncToClient(level);
            return false;
        }

        layout = result.layout();
        heat = 0;
        activeCores = 0;
        energyStorage.setEnergy(Math.min(energyStorage.getEnergyStored(), effectiveCapacity()));
        level.setBlock(pos, state.setValue(MultiblockReactorControllerBlock.ASSEMBLED, true),
                Block.UPDATE_ALL);
        updatePortLinks(level, true);
        setChanged();
        syncToClient(level);
        return true;
    }

    public void disassemble(Level level, BlockPos pos, BlockState state) {
        updatePortLinks(level, false);
        layout = ReactorLayout.EMPTY;
        activeCores = 0;
        burnTime = 0;
        heat = 0;
        level.setBlock(pos,
                state.setValue(MultiblockReactorControllerBlock.ASSEMBLED, false)
                        .setValue(MultiblockReactorControllerBlock.LIT, false),
                Block.UPDATE_ALL);
        setChanged();
        syncToClient(level);
    }

    /**
     * Verlinkt ({@code link=true}) oder entlinkt alle Port-BlockEntities auf der
     * Hüllenoberfläche mit diesem Controller. Idempotent; wird bei Assemble,
     * erfolgreicher Revalidierung und Disassemble aufgerufen.
     */
    private void updatePortLinks(Level level, boolean link) {
        if (!layout.isAssembled()) {
            return;
        }
        BlockPos min = layout.boundsMin(worldPosition);
        BlockPos max = layout.boundsMax(worldPosition);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            boolean surface = pos.getX() == min.getX() || pos.getX() == max.getX()
                    || pos.getY() == min.getY() || pos.getY() == max.getY()
                    || pos.getZ() == min.getZ() || pos.getZ() == max.getZ();
            if (!surface) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ReactorEnergyPortBlockEntity port) {
                port.setController(link ? worldPosition : null);
            } else if (blockEntity instanceof ReactorItemPortBlockEntity itemPort) {
                itemPort.setController(link ? worldPosition : null);
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // Ports entlinken, bevor der Controller verschwindet; super droppt das Inventar.
        if (this.level != null) {
            updatePortLinks(this.level, false);
        }
        super.preRemoveSideEffects(pos, state);
    }

    private int effectiveCapacity() {
        return ReactorSimulation.capacity(layout);
    }

    private int effectiveMaxHeat() {
        return ReactorSimulation.maxHeat(layout);
    }

    private ReactorSimulation.ReactorStats currentStats() {
        return ReactorSimulation.calculate(layout, activeCores, controlRodInsertion);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            MultiblockReactorControllerBlockEntity be) {
        if (level.isClientSide() || !state.getValue(MultiblockReactorControllerBlock.ASSEMBLED)) {
            return;
        }

        if (++be.revalidateTimer >= 100) {
            be.revalidateTimer = 0;
            ReactorValidator.Result result = ReactorValidator.validateBounds(
                    level, pos, be.layout.boundsMin(pos), be.layout.boundsMax(pos));
            boolean errorsChanged = !result.errors().equals(be.lastErrors);
            be.lastErrors = result.errors();
            if (!result.valid()) {
                be.disassemble(level, pos, state);
                return;
            }
            if (errorsChanged) {
                be.syncToClient(level);
            }
            be.layout = result.layout();
            be.activeCores = Math.min(be.activeCores, be.layout.coreCount());
            be.energyStorage.setEnergy(Math.min(be.energyStorage.getEnergyStored(), be.effectiveCapacity()));
            be.updatePortLinks(level, true);
        }

        boolean wasBurning = be.burnTime > 0;
        boolean dirty = false;
        boolean powered = state.getValue(MultiblockReactorControllerBlock.POWERED);

        // EMERGENCY_STOP oder manuell ausgeschaltet: laufenden Brennzyklus sofort beenden
        boolean forceStop = !be.enabled
                || (powered && be.redstoneMode == RedstoneMode.EMERGENCY_STOP);
        if (forceStop && be.burnTime > 0) {
            be.burnTime = 0;
            be.activeCores = 0;
            dirty = true;
        }

        ReactorSimulation.ReactorStats stats = be.currentStats();

        if (be.burnTime > 0) {
            be.burnTime--;
            if (be.energyStorage.getEnergyStored() < stats.capacity()) {
                be.energyStorage.setEnergy(Math.min(stats.capacity(),
                        be.energyStorage.getEnergyStored() + stats.generationPerTick()));
            }
            dirty = true;
        }

        boolean canIgnite = be.enabled && switch (be.redstoneMode) {
            case IGNORED -> true;
            case HIGH_ENABLES -> powered;
            case HIGH_DISABLES, EMERGENCY_STOP -> !powered;
        };
        if (be.burnTime <= 0 && be.energyStorage.getEnergyStored() < be.effectiveCapacity() && canIgnite) {
            int startedCores = be.consumeFuelBatch();
            if (startedCores > 0) {
                be.activeCores = startedCores;
                be.burnTime = ReactorSimulation.BURN_TICKS;
                be.burnTimeTotal = ReactorSimulation.BURN_TICKS;
                stats = be.currentStats();
                dirty = true;
            } else {
                be.activeCores = 0;
            }
        }

        int oldHeat = be.heat;
        if (wasBurning) {
            be.heat += stats.heatPerTick();
        }
        be.heat = Math.max(0, be.heat - stats.coolingPerTick());
        if (be.heat != oldHeat) {
            dirty = true;
        }

        if (be.heat >= be.effectiveMaxHeat()) {
            be.explode(level, pos, state);
            return;
        }
        if (be.heat >= (long) be.effectiveMaxHeat() * be.shutdownTempPercent / 100 && be.burnTime > 0) {
            be.burnTime = 0;
            be.activeCores = 0;
            dirty = true;
        }

        // FE-Abgabe läuft ausschließlich über die Energie-Ports der Hülle.

        if (be.burnTime > 0 && level instanceof ServerLevel serverLevel) {
            be.applyRadiation(serverLevel, pos);
        }
        if (wasBurning && level instanceof ServerLevel serverLevel
                && serverLevel.getGameTime() % 10 == 0) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    Math.min(12, 2 + be.activeCores), 0.3, 0.3, 0.3, 0.01);
        }

        boolean nowBurning = be.burnTime > 0;
        if (nowBurning != wasBurning) {
            level.setBlock(pos,
                    state.setValue(MultiblockReactorControllerBlock.LIT, nowBurning),
                    Block.UPDATE_ALL);
            dirty = true;
        }

        int comparatorLevel = be.getComparatorLevel();
        if (comparatorLevel != be.lastComparator) {
            be.lastComparator = comparatorLevel;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        if (dirty) {
            be.setChanged();
        }
    }

    private int consumeFuelBatch() {
        ItemStack fuel = inventory.get(FUEL_SLOT);
        ItemStack waste = inventory.get(WASTE_SLOT);
        if (!fuel.is(ModItems.FUEL_ROD)) {
            return 0;
        }

        int wasteRoom = waste.isEmpty() ? new ItemStack(ModItems.SPENT_FUEL_ROD.get()).getMaxStackSize()
                : waste.is(ModItems.SPENT_FUEL_ROD) ? waste.getMaxStackSize() - waste.getCount() : 0;
        int startedCores = Math.min(layout.coreCount(), Math.min(fuel.getCount(), wasteRoom));
        if (startedCores <= 0) {
            return 0;
        }

        fuel.shrink(startedCores);
        if (waste.isEmpty()) {
            inventory.set(WASTE_SLOT, new ItemStack(ModItems.SPENT_FUEL_ROD.get(), startedCores));
        } else {
            waste.grow(startedCores);
        }
        return startedCores;
    }

    private void applyRadiation(ServerLevel level, BlockPos pos) {
        int radLevel = heat >= effectiveMaxHeat() / 2 ? 1 : 0;
        Vec3 center = Vec3.atCenterOf(pos);
        AABB searchBox = new AABB(pos).inflate(RADIATION_RADIUS);
        level.getEntitiesOfClass(Player.class, searchBox,
                        player -> player.distanceToSqr(center)
                                <= (double) RADIATION_RADIUS * RADIATION_RADIUS)
                .forEach(player -> {
                    if (!NuclearReactorBlockEntity.hasLeadShielding(level, pos, player.blockPosition())) {
                        player.addEffect(new MobEffectInstance(
                                ModEffects.RADIATION, 60, radLevel, false, true));
                        if (level.getGameTime() % 20 == 0) {
                            player.hurt(level.damageSources().magic(),
                                    radLevel == 0 ? 0.5f : 1.5f);
                        }
                    }
                });
    }

    public int getComparatorLevel() {
        return switch (comparatorMode) {
            case ENERGY -> {
                int capacity = effectiveCapacity();
                int stored = energyStorage.getEnergyStored();
                if (stored <= 0 || capacity <= 0) yield 0;
                yield (int) Math.max(1, (long) stored * 15 / capacity);
            }
            case TEMPERATURE -> {
                int maxH = effectiveMaxHeat();
                if (heat <= 0 || maxH <= 0) yield 0;
                yield Math.max(1, heat * 15 / maxH);
            }
            case FUEL -> {
                ItemStack fuel = inventory.get(FUEL_SLOT);
                if (fuel.isEmpty()) yield 0;
                yield Math.max(1, fuel.getCount() * 15 / fuel.getMaxStackSize());
            }
            case WASTE -> {
                ItemStack waste = inventory.get(WASTE_SLOT);
                if (waste.isEmpty()) yield 0;
                yield Math.max(1, waste.getCount() * 15 / waste.getMaxStackSize());
            }
        };
    }

    private void explode(Level level, BlockPos pos, BlockState state) {
        int size = layout.maxDimension();
        disassemble(level, pos, state);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                4f + size, true, Level.ExplosionInteraction.BLOCK);
    }

    // --- WorldlyContainer: Hopper laufen ausschließlich über Item-Ports ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) <= 64.0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("Energy", energyStorage.getEnergyStored());
        output.putInt("BurnTime", burnTime);
        output.putInt("BurnTimeTotal", burnTimeTotal);
        output.putInt("Heat", heat);
        output.putInt("ActiveCores", activeCores);
        output.putInt("RelMinX", layout.relMinX());
        output.putInt("RelMinY", layout.relMinY());
        output.putInt("RelMinZ", layout.relMinZ());
        output.putInt("SizeX", layout.sizeX());
        output.putInt("SizeY", layout.sizeY());
        output.putInt("SizeZ", layout.sizeZ());
        output.putInt("CoreCount", layout.coreCount());
        output.putInt("ControlRodCount", layout.controlRodCount());
        output.putInt("CoolingPipeCount", layout.coolingPipeCount());
        output.putInt("ConnectedCoolingPipeCount", layout.connectedCoolingPipeCount());
        output.putInt("CoreNeighborContacts", layout.coreNeighborContacts());
        output.putInt("CoreControlRodContacts", layout.coreControlRodContacts());
        output.putInt("CoreCoolingContacts", layout.coreCoolingContacts());
        output.putInt("EnergyPortCount", layout.energyPortCount());
        output.putInt("ItemPortCount", layout.itemPortCount());
        output.putInt("RedstoneMode", redstoneMode.ordinal());
        output.putInt("ComparatorMode", comparatorMode.ordinal());
        output.putInt("ControlRodInsertion", controlRodInsertion);
        output.putBoolean("Enabled", enabled);
        output.putInt("ShutdownTemp", shutdownTempPercent);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        burnTime = Math.max(0, input.getIntOr("BurnTime", 0));
        burnTimeTotal = Math.max(0, input.getIntOr("BurnTimeTotal", 0));
        heat = Math.max(0, input.getIntOr("Heat", 0));
        activeCores = Math.max(0, input.getIntOr("ActiveCores", 0));
        int rsOrd = input.getIntOr("RedstoneMode", RedstoneMode.HIGH_DISABLES.ordinal());
        redstoneMode = rsOrd >= 0 && rsOrd < RedstoneMode.values().length
                ? RedstoneMode.values()[rsOrd] : RedstoneMode.HIGH_DISABLES;
        int cmpOrd = input.getIntOr("ComparatorMode", ComparatorMode.ENERGY.ordinal());
        comparatorMode = cmpOrd >= 0 && cmpOrd < ComparatorMode.values().length
                ? ComparatorMode.values()[cmpOrd] : ComparatorMode.ENERGY;
        controlRodInsertion = Math.clamp(input.getIntOr("ControlRodInsertion", 0), 0, 100);
        enabled = input.getBooleanOr("Enabled", true);
        shutdownTempPercent = Math.clamp(input.getIntOr("ShutdownTemp", 90),
                MIN_SHUTDOWN_TEMP, MAX_SHUTDOWN_TEMP);
        layout = loadLayout(input);
        activeCores = Math.min(activeCores, layout.coreCount());
        energyStorage.setEnergy(Math.min(Math.max(0, input.getIntOr("Energy", 0)),
                effectiveCapacity()));
        lastComparator = getComparatorLevel();

        // Nur im Update-Tag enthalten (Client-Anzeige): Fehlerliste + Innenraum-Schnitt.
        input.getIntArray("ClientErrors").ifPresent(packed -> {
            List<ValidationError> errors = new ArrayList<>(packed.length / 4);
            for (int i = 0; i + 3 < packed.length; i += 4) {
                int ordinal = packed[i];
                if (ordinal >= 0 && ordinal < ValidationError.Type.values().length) {
                    errors.add(new ValidationError(ValidationError.Type.values()[ordinal],
                            new BlockPos(packed[i + 1], packed[i + 2], packed[i + 3])));
                }
            }
            lastErrors = List.copyOf(errors);
        });
        clientInteriorGrid = input.getIntArray("InteriorGrid").orElse(null);
    }

    /**
     * Liest das Layout aus NBT. Erkennt das alte Format (vor rechteckigen Hüllen)
     * am fehlenden {@code SizeX} und rechnet dessen zentriertes {@code ReactorSize}
     * mithilfe des Blockstate-{@code FACING} in die neuen Grenzfelder um.
     */
    private ReactorLayout loadLayout(ValueInput input) {
        int coreCount = input.getIntOr("CoreCount", 0);
        int rodCount = input.getIntOr("ControlRodCount", 0);
        int pipeCount = input.getIntOr("CoolingPipeCount", 0);
        int connectedPipes = input.getIntOr("ConnectedCoolingPipeCount", 0);
        int neighborContacts = input.getIntOr("CoreNeighborContacts", 0);
        int rodContacts = input.getIntOr("CoreControlRodContacts", 0);
        int coolingContacts = input.getIntOr("CoreCoolingContacts", 0);

        int sizeX = input.getIntOr("SizeX", -1);
        if (sizeX >= 0) {
            return new ReactorLayout(
                    input.getIntOr("RelMinX", 0),
                    input.getIntOr("RelMinY", 0),
                    input.getIntOr("RelMinZ", 0),
                    sizeX,
                    input.getIntOr("SizeY", 0),
                    input.getIntOr("SizeZ", 0),
                    coreCount, rodCount, pipeCount, connectedPipes,
                    neighborContacts, rodContacts, coolingContacts,
                    input.getIntOr("EnergyPortCount", 0),
                    input.getIntOr("ItemPortCount", 0));
        }

        // Migration: altes zentriertes Format (Controller mittig in einer Wand).
        int legacySize = input.getIntOr("ReactorSize", 0);
        if (legacySize < 3) {
            return ReactorLayout.EMPTY;
        }
        Direction facing = getBlockState().getValue(MultiblockReactorControllerBlock.FACING);
        Direction forward = facing.getOpposite();
        Direction right = facing.getClockWise();
        int half = legacySize / 2;
        int relMinX = Math.min(right.getStepX() * -half, right.getStepX() * half)
                + Math.min(0, forward.getStepX() * (legacySize - 1));
        int relMinZ = Math.min(right.getStepZ() * -half, right.getStepZ() * half)
                + Math.min(0, forward.getStepZ() * (legacySize - 1));
        return new ReactorLayout(
                relMinX, -half, relMinZ,
                legacySize, legacySize, legacySize,
                coreCount, rodCount, pipeCount, connectedPipes,
                neighborContacts, rodContacts, coolingContacts,
                0, 0);
    }

    // --- Client-Sync (Update-Tag: Fehlerliste + Innenraum für die Schichtansicht) ---

    /** Zellcodes des Innenraum-Schnitts für die Schichtansicht. */
    public static final int CELL_AIR = 0;
    public static final int CELL_LEAD = 1;
    public static final int CELL_CORE = 2;
    public static final int CELL_ROD = 3;
    public static final int CELL_PIPE = 4;
    public static final int CELL_OTHER = 5;

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = saveCustomOnly(registries);
        int[] packed = new int[lastErrors.size() * 4];
        for (int i = 0; i < lastErrors.size(); i++) {
            ValidationError error = lastErrors.get(i);
            packed[i * 4] = error.type().ordinal();
            packed[i * 4 + 1] = error.pos().getX();
            packed[i * 4 + 2] = error.pos().getY();
            packed[i * 4 + 3] = error.pos().getZ();
        }
        tag.putIntArray("ClientErrors", packed);
        if (layout.isAssembled() && level != null) {
            tag.putIntArray("InteriorGrid", captureInterior());
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Serialisiert den Innenraum (ohne Hülle) als flaches Raster;
     * Index = ((y · innerZ) + z) · innerX + x mit Innenmaßen size−2.
     */
    private int[] captureInterior() {
        int innerX = layout.sizeX() - 2;
        int innerY = layout.sizeY() - 2;
        int innerZ = layout.sizeZ() - 2;
        BlockPos origin = layout.boundsMin(worldPosition).offset(1, 1, 1);
        int[] grid = new int[innerX * innerY * innerZ];
        int i = 0;
        for (int y = 0; y < innerY; y++) {
            for (int z = 0; z < innerZ; z++) {
                for (int x = 0; x < innerX; x++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    grid[i++] = state.isAir() ? CELL_AIR
                            : state.is(ModBlocks.LEAD_BLOCK.get()) ? CELL_LEAD
                            : state.is(ModBlocks.REACTOR_CORE.get()) ? CELL_CORE
                            : state.is(ModBlocks.CONTROL_ROD_BLOCK.get()) ? CELL_ROD
                            : state.is(ModBlocks.COOLING_PIPE.get()) ? CELL_PIPE
                            : CELL_OTHER;
                }
            }
        }
        return grid;
    }

    /** Innenraum-Schnitt (nur auf dem Client befüllt), Reihenfolge siehe {@link #captureInterior}. */
    public int @Nullable [] getClientInteriorGrid() {
        return clientInteriorGrid;
    }

    /** Schickt Fehlerliste + Innenraum-Schnitt an die Clients (Update-Tag). */
    private void syncToClient(Level level) {
        if (!level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        if (this.level != null) {
            // Frische Fehlerliste + Innenraum-Schnitt für die Diagnose-Tabs mitsenden.
            syncToClient(this.level);
        }
        return new ModularReactorScreenHandler(syncId, playerInventory, this, propertyDelegate, worldPosition);
    }
}
