package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.MultiblockReactorControllerBlock;
import ch.danielt.akw.energy.EnergyNet;
import ch.danielt.akw.reactor.ComparatorMode;
import ch.danielt.akw.reactor.ReactorLayout;
import ch.danielt.akw.reactor.ReactorSimulation;
import ch.danielt.akw.reactor.ReactorValidator;
import ch.danielt.akw.reactor.RedstoneMode;
import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.screen.MultiblockReactorScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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

/** Controller und Laufzeit-Zustand des modular aufgebauten Reaktors. */
public class MultiblockReactorControllerBlockEntity extends BlockEntity
        implements ImplementedInventory, WorldlyContainer, MenuProvider {

    public static final int FUEL_SLOT = 0;
    public static final int WASTE_SLOT = 1;
    private static final int[] FUEL_SLOTS = {FUEL_SLOT};
    private static final int[] WASTE_SLOTS = {WASTE_SLOT};
    private static final int[] NO_SLOTS = {};

    private static final int IDX_ENERGY = NuclearReactorBlockEntity.IDX_ENERGY;
    private static final int IDX_CAPACITY = NuclearReactorBlockEntity.IDX_CAPACITY;
    private static final int IDX_BURN_TIME = NuclearReactorBlockEntity.IDX_BURN_TIME;
    private static final int IDX_BURN_TOTAL = NuclearReactorBlockEntity.IDX_BURN_TOTAL;
    private static final int IDX_HEAT = NuclearReactorBlockEntity.IDX_HEAT;
    private static final int IDX_MAX_HEAT = NuclearReactorBlockEntity.IDX_MAX_HEAT;
    private static final int IDX_REDSTONE_MODE = NuclearReactorBlockEntity.IDX_REDSTONE_MODE;
    private static final int IDX_COMPARATOR_MODE = NuclearReactorBlockEntity.IDX_COMPARATOR_MODE;
    private static final int PROPERTY_COUNT = NuclearReactorBlockEntity.PROPERTY_COUNT;
    private static final int RADIATION_RADIUS = 8;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    /** Generator-Speicher; die effektive Kapazität wird vom Layout begrenzt. */
    public final MutableEnergyStorage energyStorage =
            new MutableEnergyStorage(20_000_000, 0, 32_768);

    private ReactorLayout layout = ReactorLayout.EMPTY;
    private ReactorValidator.Error lastAssemblyError = ReactorValidator.Error.MISSING_CASING;
    private int activeCores;
    private int burnTime;
    private int burnTimeTotal;
    private int heat;
    private int revalidateTimer;
    private int lastComparator = -1;
    private RedstoneMode redstoneMode = RedstoneMode.HIGH_DISABLES;
    private ComparatorMode comparatorMode = ComparatorMode.ENERGY;

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
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return PROPERTY_COUNT;
        }
    };

    public MultiblockReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIBLOCK_REACTOR_CONTROLLER.get(), pos, state);
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

    public Component getLastAssemblyError() {
        return Component.translatable(lastAssemblyError.translationKey());
    }

    public boolean tryAssemble(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(MultiblockReactorControllerBlock.FACING);
        ReactorValidator.Result result = ReactorValidator.find(level, pos, facing);
        lastAssemblyError = result.error();
        if (!result.valid()) {
            return false;
        }

        layout = result.layout();
        heat = 0;
        activeCores = 0;
        energyStorage.setEnergy(Math.min(energyStorage.getEnergyStored(), effectiveCapacity()));
        level.setBlock(pos, state.setValue(MultiblockReactorControllerBlock.ASSEMBLED, true),
                Block.UPDATE_ALL);
        setChanged();
        return true;
    }

    public void disassemble(Level level, BlockPos pos, BlockState state) {
        layout = ReactorLayout.EMPTY;
        activeCores = 0;
        burnTime = 0;
        heat = 0;
        level.setBlock(pos,
                state.setValue(MultiblockReactorControllerBlock.ASSEMBLED, false)
                        .setValue(MultiblockReactorControllerBlock.LIT, false),
                Block.UPDATE_ALL);
        setChanged();
    }

    private int effectiveCapacity() {
        return ReactorSimulation.capacity(layout);
    }

    private int effectiveMaxHeat() {
        return ReactorSimulation.maxHeat(layout);
    }

    private ReactorSimulation.ReactorStats currentStats() {
        return ReactorSimulation.calculate(layout, activeCores);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            MultiblockReactorControllerBlockEntity be) {
        if (level.isClientSide() || !state.getValue(MultiblockReactorControllerBlock.ASSEMBLED)) {
            return;
        }

        if (++be.revalidateTimer >= 100) {
            be.revalidateTimer = 0;
            Direction facing = state.getValue(MultiblockReactorControllerBlock.FACING);
            ReactorValidator.Result result = ReactorValidator.validate(
                    level, pos, facing, be.layout.outerSize());
            if (!result.valid()) {
                be.lastAssemblyError = result.error();
                be.disassemble(level, pos, state);
                return;
            }
            be.layout = result.layout();
            be.activeCores = Math.min(be.activeCores, be.layout.coreCount());
            be.energyStorage.setEnergy(Math.min(be.energyStorage.getEnergyStored(), be.effectiveCapacity()));
        }

        boolean wasBurning = be.burnTime > 0;
        boolean dirty = false;
        boolean powered = state.getValue(MultiblockReactorControllerBlock.POWERED);

        // EMERGENCY_STOP: laufenden Brennzyklus sofort beenden
        if (powered && be.redstoneMode == RedstoneMode.EMERGENCY_STOP && be.burnTime > 0) {
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

        boolean canIgnite = switch (be.redstoneMode) {
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
        if (be.heat >= be.effectiveMaxHeat() * 9 / 10 && be.burnTime > 0) {
            be.burnTime = 0;
            be.activeCores = 0;
            dirty = true;
        }

        if (be.energyStorage.getEnergyStored() > 0) {
            EnergyNet.pushToNeighbors(be.energyStorage, level, pos,
                    Math.max(160, stats.generationPerTick() * 2));
        }

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
        int size = layout.outerSize();
        disassemble(level, pos, state);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                4f + size, true, Level.ExplosionInteraction.BLOCK);
    }

    // --- WorldlyContainer (Hopper-Kompatibilitaet) ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) return FUEL_SLOTS;
        if (side == Direction.DOWN) return WASTE_SLOTS;
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return slot == FUEL_SLOT && stack.is(ModItems.FUEL_ROD);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == WASTE_SLOT;
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
        output.putInt("ReactorSize", layout.outerSize());
        output.putInt("CoreCount", layout.coreCount());
        output.putInt("ControlRodCount", layout.controlRodCount());
        output.putInt("CoolingPipeCount", layout.coolingPipeCount());
        output.putInt("ConnectedCoolingPipeCount", layout.connectedCoolingPipeCount());
        output.putInt("CoreNeighborContacts", layout.coreNeighborContacts());
        output.putInt("CoreControlRodContacts", layout.coreControlRodContacts());
        output.putInt("CoreCoolingContacts", layout.coreCoolingContacts());
        output.putInt("RedstoneMode", redstoneMode.ordinal());
        output.putInt("ComparatorMode", comparatorMode.ordinal());
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
        layout = new ReactorLayout(
                input.getIntOr("ReactorSize", 0),
                input.getIntOr("CoreCount", 0),
                input.getIntOr("ControlRodCount", 0),
                input.getIntOr("CoolingPipeCount", 0),
                input.getIntOr("ConnectedCoolingPipeCount", 0),
                input.getIntOr("CoreNeighborContacts", 0),
                input.getIntOr("CoreControlRodContacts", 0),
                input.getIntOr("CoreCoolingContacts", 0));
        activeCores = Math.min(activeCores, layout.coreCount());
        energyStorage.setEnergy(Math.min(Math.max(0, input.getIntOr("Energy", 0)),
                effectiveCapacity()));
        lastComparator = getComparatorLevel();
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new MultiblockReactorScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }
}
