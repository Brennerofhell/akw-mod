package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.NuclearReactorBlock;
import ch.danielt.akw.energy.EnergyNet;
import ch.danielt.akw.reactor.ComparatorMode;
import ch.danielt.akw.reactor.RedstoneMode;
import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.screen.NuclearReactorScreenHandler;
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

/**
 * Gemeinsame BlockEntity fuer alle Reaktor-Typen. Die Tier-Parameter
 * (Kapazitaet, FE/Tick, Abgaberate, Brenndauer, maxHitze, Hitze/Tick) werden aus
 * dem zugehoerigen {@link NuclearReactorBlock} gelesen, sodass alle Typen denselben
 * Code teilen.
 *
 * <p><b>Kuehlung:</b> Solange ein Brennstab brennt, baut der Reaktor Hitze auf.
 * Jeder direkt angrenzende {@link ModBlocks#COOLING_PIPE} senkt die Hitze pro Tick
 * ({@link #COOL_PER_PIPE}), dazu kommt eine geringe Eigenkuehlung ({@link #PASSIVE_COOL}).
 * Ab {@link #THROTTLE_NUMERATOR}/4 der maxHitze wird die Energie-Erzeugung gedrosselt;
 * bei Erreichen der maxHitze explodiert der Reaktor.
 */
public class NuclearReactorBlockEntity extends BlockEntity
        implements ImplementedInventory, WorldlyContainer, MenuProvider {

    public static final int FUEL_SLOT = 0;
    public static final int WASTE_SLOT = 1;

    private static final int[] FUEL_SLOTS  = {FUEL_SLOT};
    private static final int[] WASTE_SLOTS = {WASTE_SLOT};
    private static final int[] NO_SLOTS    = {};

    /** ContainerData-Indizes (gemeinsam von BlockEntity, ScreenHandler, Screen genutzt). */
    public static final int IDX_ENERGY_LOW = 0;
    public static final int IDX_ENERGY_HIGH = 1;
    public static final int IDX_CAPACITY_LOW = 2;
    public static final int IDX_CAPACITY_HIGH = 3;
    public static final int IDX_BURN_TIME = 4;
    public static final int IDX_BURN_TOTAL = 5;
    public static final int IDX_HEAT_LOW = 6;
    public static final int IDX_HEAT_HIGH = 7;
    public static final int IDX_MAX_HEAT_LOW = 8;
    public static final int IDX_MAX_HEAT_HIGH = 9;
    public static final int IDX_REDSTONE_MODE = 10;
    public static final int IDX_COMPARATOR_MODE = 11;
    public static final int PROPERTY_COUNT = 12;

    /** Kuehlung pro Tick je angrenzendem Kuehlrohr. */
    public static final int COOL_PER_PIPE = 8;
    /** Eigenkuehlung pro Tick (auch ohne Kuehlrohre). */
    public static final int PASSIVE_COOL = 2;
    /** Ab diesem Anteil (Zaehler/4) der maxHitze wird die Erzeugung gedrosselt. */
    public static final int THROTTLE_NUMERATOR = 3;
    /** Hitze-Reduktion pro Tick je angrenzendem Steuerstab-Block. */
    public static final int HEAT_REDUCTION_PER_ROD = 4;
    /** Strahlungs-Radius (Bloecke) eines laufenden Reaktors. */
    private static final int RADIATION_RADIUS = 8;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    /** Energiespeicher: kein Input (Generator), nur Abgabe. */
    public final MutableEnergyStorage energyStorage;
    private final int genPerTick;
    private final int burnTicksPerRod;
    private final int maxHeat;
    private final int heatPerTick;
    private final float explosionPower;

    private int burnTime;
    private int burnTimeTotal;
    private int heat;
    private int lastComparator = -1;
    private RedstoneMode redstoneMode = RedstoneMode.HIGH_DISABLES;
    private ComparatorMode comparatorMode = ComparatorMode.ENERGY;

    private final ContainerData containerData = new ContainerData() {
        private int energyLow, energyHigh;
        private int capacityLow, capacityHigh;
        private int heatLow, heatHigh;
        private int maxHeatLow, maxHeatHigh;

        @Override
        public int get(int index) {
            return switch (index) {
                case IDX_ENERGY_LOW -> getLowWord(Math.min(energyStorage.getEnergyStored(), Integer.MAX_VALUE));
                case IDX_ENERGY_HIGH -> getHighWord(Math.min(energyStorage.getEnergyStored(), Integer.MAX_VALUE));
                case IDX_CAPACITY_LOW -> getLowWord(Math.min(energyStorage.getMaxEnergyStored(), Integer.MAX_VALUE));
                case IDX_CAPACITY_HIGH -> getHighWord(Math.min(energyStorage.getMaxEnergyStored(), Integer.MAX_VALUE));
                case IDX_BURN_TIME -> burnTime;
                case IDX_BURN_TOTAL -> burnTimeTotal;
                case IDX_HEAT_LOW -> getLowWord(heat);
                case IDX_HEAT_HIGH -> getHighWord(heat);
                case IDX_MAX_HEAT_LOW -> getLowWord(maxHeat);
                case IDX_MAX_HEAT_HIGH -> getHighWord(maxHeat);
                case IDX_REDSTONE_MODE -> redstoneMode.ordinal();
                case IDX_COMPARATOR_MODE -> comparatorMode.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case IDX_ENERGY_LOW -> {
                    energyLow = value;
                    energyStorage.setEnergy(combineWords(energyLow, energyHigh));
                }
                case IDX_ENERGY_HIGH -> {
                    energyHigh = value;
                    energyStorage.setEnergy(combineWords(energyLow, energyHigh));
                }
                case IDX_CAPACITY_LOW -> capacityLow = value;
                case IDX_CAPACITY_HIGH -> capacityHigh = value;
                case IDX_BURN_TIME -> burnTime = value;
                case IDX_BURN_TOTAL -> burnTimeTotal = value;
                case IDX_HEAT_LOW -> {
                    heatLow = value;
                    heat = combineWords(heatLow, heatHigh);
                }
                case IDX_HEAT_HIGH -> {
                    heatHigh = value;
                    heat = combineWords(heatLow, heatHigh);
                }
                case IDX_MAX_HEAT_LOW -> maxHeatLow = value;
                case IDX_MAX_HEAT_HIGH -> maxHeatHigh = value;
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

    public NuclearReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUCLEAR_REACTOR.get(), pos, state);
        NuclearReactorBlock block = (NuclearReactorBlock) state.getBlock();
        this.energyStorage = new MutableEnergyStorage(block.capacity, 0, block.maxExtract);
        this.genPerTick = block.genPerTick;
        this.burnTicksPerRod = block.burnTicksPerRod;
        this.maxHeat = block.maxHeat;
        this.heatPerTick = block.heatPerTick;
        // Explosionsstaerke skaliert mit der maxHitze des Reaktor-Typs.
        this.explosionPower = Math.min(12f, Math.max(4f, block.maxHeat / 400f));
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NuclearReactorBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        boolean wasBurning = be.burnTime > 0;
        boolean dirty = false;

        // EMERGENCY_STOP: laufenden Brennstab sofort stoppen, wenn Signal anliegt
        boolean powered = state.getValue(NuclearReactorBlock.POWERED);
        if (powered && be.redstoneMode == RedstoneMode.EMERGENCY_STOP && be.burnTime > 0) {
            be.burnTime = 0;
            be.burnTimeTotal = 0;
            dirty = true;
        }

        // Laufenden Brennstab abbrennen und Energie erzeugen (mit Hitze-Drosselung)
        if (be.burnTime > 0) {
            be.burnTime--;
            int gen = be.genPerTick;
            if (be.heat >= be.maxHeat * THROTTLE_NUMERATOR / 4) {
                gen = Math.max(1, gen / 4);   // Ueberhitzung droht -> Erzeugung drosseln
            }
            if (be.energyStorage.getEnergyStored() < be.energyStorage.getMaxEnergyStored()) {
                be.energyStorage.setEnergy(Math.min(
                        be.energyStorage.getMaxEnergyStored(),
                        be.energyStorage.getEnergyStored() + gen));
            }
            dirty = true;
        }

        // Neuen Brennstab zuenden (abhaengig vom Redstone-Modus)
        boolean canIgnite = switch (be.redstoneMode) {
            case IGNORED -> true;
            case HIGH_ENABLES -> powered;
            case HIGH_DISABLES, EMERGENCY_STOP -> !powered;
        };
        if (be.burnTime <= 0 && be.energyStorage.getEnergyStored() < be.energyStorage.getMaxEnergyStored() && canIgnite) {
            ItemStack fuel  = be.inventory.get(FUEL_SLOT);
            ItemStack waste = be.inventory.get(WASTE_SLOT);
            boolean wasteRoom = waste.isEmpty()
                    || (waste.is(ModItems.SPENT_FUEL_ROD) && waste.getCount() < waste.getMaxStackSize());
            if (fuel.is(ModItems.FUEL_ROD) && wasteRoom) {
                fuel.shrink(1);
                if (waste.isEmpty()) {
                    be.inventory.set(WASTE_SLOT, new ItemStack(ModItems.SPENT_FUEL_ROD.get()));
                } else {
                    waste.grow(1);
                }
                be.burnTime = be.burnTicksPerRod;
                be.burnTimeTotal = be.burnTicksPerRod;
                dirty = true;
            }
        }

        // Hitze-Dynamik: Aufbau (reduziert durch Steuerstaebe), Abbau durch Kuehlung
        int heatPerTickEffective = Math.max(0, be.heatPerTick
                - be.countControlRods(level, pos) * HEAT_REDUCTION_PER_ROD);
        int cooling = PASSIVE_COOL + be.countCoolingPipes(level, pos) * COOL_PER_PIPE;
        int oldHeat = be.heat;
        if (wasBurning) {
            be.heat += heatPerTickEffective;
        }
        be.heat = Math.max(0, be.heat - cooling);
        if (be.heat != oldHeat) {
            dirty = true;
        }

        // Ueberhitzung -> Explosion (Reaktor wird zerstoert)
        if (be.heat >= be.maxHeat) {
            be.explode(level, pos);
            return;
        }

        // Energie an angrenzende Verbraucher/Speicher abgeben
        if (be.energyStorage.getEnergyStored() > 0) {
            be.pushEnergy(level, pos);
        }

        // Strahlung: laufender Reaktor bestrahlt Spieler in der Naehe
        if (be.burnTime > 0 && level instanceof ServerLevel serverLevel) {
            int radLevel = be.heat >= be.maxHeat / 2 ? 1 : 0;
            Vec3 center = Vec3.atCenterOf(pos);
            AABB searchBox = new AABB(pos).inflate(RADIATION_RADIUS);
            serverLevel.getEntitiesOfClass(Player.class, searchBox,
                    p -> p.distanceToSqr(center) <= (double) RADIATION_RADIUS * RADIATION_RADIUS)
                    .forEach(player -> {
                        if (!hasLeadShielding(serverLevel, pos, player.blockPosition())) {
                            player.addEffect(new MobEffectInstance(
                                    ModEffects.RADIATION, 60, radLevel, false, true));
                            if (serverLevel.getGameTime() % 20 == 0) {
                                player.hurt(
                                        serverLevel.damageSources().magic(),
                                        radLevel == 0 ? 0.5f : 1.5f);
                            }
                        }
                    });
        }

        // Strahlungspartikel sichtbar machen wenn Reaktor aktiv
        if (wasBurning && level instanceof ServerLevel sw && sw.getGameTime() % 10 == 0) {
            sw.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    3, 0.2, 0.2, 0.2, 0.01);
        }

        boolean nowBurning = be.burnTime > 0;
        if (nowBurning != wasBurning) {
            level.setBlock(pos, state.setValue(NuclearReactorBlock.LIT, nowBurning), Block.UPDATE_ALL);
            dirty = true;
        }

        // Komparator-Update (Energie-Fuellstand 0-15)
        int comparatorLevel = be.getComparatorLevel();
        if (comparatorLevel != be.lastComparator) {
            be.lastComparator = comparatorLevel;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        if (dirty) {
            be.setChanged();
        }
    }

    /** Komparator-Ausgang 0–15, Quelle abhaengig von {@code comparatorMode}. */
    public int getComparatorLevel() {
        return switch (comparatorMode) {
            case ENERGY -> {
                int stored = energyStorage.getEnergyStored();
                int cap = energyStorage.getMaxEnergyStored();
                if (stored <= 0 || cap <= 0) yield 0;
                yield (int) Math.max(1, (long) stored * 15 / cap);
            }
            case TEMPERATURE -> {
                if (heat <= 0 || maxHeat <= 0) yield 0;
                yield Math.max(1, heat * 15 / maxHeat);
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

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public ComparatorMode getComparatorMode() {
        return comparatorMode;
    }

    /** Zaehlt direkt angrenzende Kuehlrohre (max. 6). */
    private int countCoolingPipes(Level level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(ModBlocks.COOLING_PIPE.get())) count++;
        }
        return count;
    }

    /** Zaehlt direkt angrenzende Steuerstab-Bloecke (max. 6). */
    private int countControlRods(Level level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(ModBlocks.CONTROL_ROD_BLOCK.get())) count++;
        }
        return count;
    }

    /**
     * Prueft ob ein Blei-Block auf dem direkten Pfad zwischen Quelle und Spieler liegt.
     * Schrittweite 1 Block — reicht fuer den max. 8-Block-Radius.
     */
    static boolean hasLeadShielding(Level level, BlockPos reactorPos, BlockPos playerPos) {
        double dx = playerPos.getX() - reactorPos.getX();
        double dy = playerPos.getY() - reactorPos.getY();
        double dz = playerPos.getZ() - reactorPos.getZ();
        int steps = (int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz));
        if (steps == 0) return false;
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            BlockPos check = new BlockPos(
                    (int) Math.floor(reactorPos.getX() + dx * t + 0.5),
                    (int) Math.floor(reactorPos.getY() + dy * t + 0.5),
                    (int) Math.floor(reactorPos.getZ() + dz * t + 0.5));
            if (level.getBlockState(check).is(ModBlocks.LEAD_BLOCK.get())) return true;
        }
        return false;
    }

    /** Reaktor entfernen (Inhalt wird ausgeworfen) und Explosion ausloesen. */
    private void explode(Level level, BlockPos pos) {
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                explosionPower, true, Level.ExplosionInteraction.BLOCK);
    }

    private void pushEnergy(Level level, BlockPos pos) {
        EnergyNet.pushToNeighbors(energyStorage, level, pos, energyStorage.getMaxExtract());
    }

    // --- WorldlyContainer (Hopper-Kompatibilitaet) ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP)   return FUEL_SLOTS;
        if (side == Direction.DOWN) return WASTE_SLOTS;
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == FUEL_SLOT && stack.is(ModItems.FUEL_ROD);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == WASTE_SLOT;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("Energy", energyStorage.getEnergyStored());
        output.putInt("BurnTime", burnTime);
        output.putInt("BurnTimeTotal", burnTimeTotal);
        output.putInt("Heat", heat);
        output.putInt("RedstoneMode", redstoneMode.ordinal());
        output.putInt("ComparatorMode", comparatorMode.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        energyStorage.setEnergy(input.getIntOr("Energy", 0));
        burnTime = input.getIntOr("BurnTime", 0);
        burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);
        heat = input.getIntOr("Heat", 0);
        int rsOrd = input.getIntOr("RedstoneMode", RedstoneMode.HIGH_DISABLES.ordinal());
        redstoneMode = rsOrd >= 0 && rsOrd < RedstoneMode.values().length
                ? RedstoneMode.values()[rsOrd] : RedstoneMode.HIGH_DISABLES;
        int cmpOrd = input.getIntOr("ComparatorMode", ComparatorMode.ENERGY.ordinal());
        comparatorMode = cmpOrd >= 0 && cmpOrd < ComparatorMode.values().length
                ? ComparatorMode.values()[cmpOrd] : ComparatorMode.ENERGY;
        lastComparator = getComparatorLevel();
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new NuclearReactorScreenHandler(syncId, playerInventory, this.getBlockPos());
    }

    public static int getLowWord(int val) {
        return val & 0xFFFF;
    }

    public static int getHighWord(int val) {
        return (val >> 16) & 0xFFFF;
    }

    public static int combineWords(int low, int high) {
        return ((high & 0xFFFF) << 16) | (low & 0xFFFF);
    }
}
