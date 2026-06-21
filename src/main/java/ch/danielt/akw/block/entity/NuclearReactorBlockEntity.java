package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.NuclearReactorBlock;
import ch.danielt.akw.energy.EnergyNet;
import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.screen.NuclearReactorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleEnergyStorage;

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
        implements ImplementedInventory, SidedInventory, ExtendedScreenHandlerFactory<BlockPos> {

    public static final int FUEL_SLOT = 0;
    public static final int WASTE_SLOT = 1;

    private static final int[] FUEL_SLOTS  = {FUEL_SLOT};
    private static final int[] WASTE_SLOTS = {WASTE_SLOT};
    private static final int[] NO_SLOTS    = {};

    /** PropertyDelegate-Indizes (gemeinsam von BlockEntity, ScreenHandler, Screen genutzt). */
    public static final int IDX_ENERGY = 0;
    public static final int IDX_CAPACITY = 1;
    public static final int IDX_BURN_TIME = 2;
    public static final int IDX_BURN_TOTAL = 3;
    public static final int IDX_HEAT = 4;
    public static final int IDX_MAX_HEAT = 5;
    public static final int PROPERTY_COUNT = 6;

    /** Kuehlung pro Tick je angrenzendem Kuehlrohr. */
    public static final int COOL_PER_PIPE = 8;
    /** Eigenkuehlung pro Tick (auch ohne Kuehlrohre). */
    public static final int PASSIVE_COOL = 2;
    /** Ab diesem Anteil (Zaehler/4) der maxHitze wird die Erzeugung gedrosselt. */
    public static final int THROTTLE_NUMERATOR = 3;
    /** Hitze-Reduktion pro Tick je angrenzendem Steuerstab-Block. */
    public static final int HEAT_REDUCTION_PER_ROD = 4;
    /** Strahlungs-Radius (Blöcke) eines laufenden Reaktors. */
    private static final int RADIATION_RADIUS = 8;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    /** Energiespeicher: kein Input (Generator), nur Abgabe. */
    public final SimpleEnergyStorage energyStorage;
    private final int genPerTick;
    private final int burnTicksPerRod;
    private final int maxHeat;
    private final int heatPerTick;
    private final float explosionPower;

    private int burnTime;
    private int burnTimeTotal;
    private int heat;
    private int lastComparator = -1;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            // amount/capacity sind long; auf Integer.MAX_VALUE begrenzen, da der
            // PropertyDelegate nur int synchronisiert (verhindert Overflow-Anzeige).
            return switch (index) {
                case IDX_ENERGY -> (int) Math.min(energyStorage.amount, Integer.MAX_VALUE);
                case IDX_CAPACITY -> (int) Math.min(energyStorage.capacity, Integer.MAX_VALUE);
                case IDX_BURN_TIME -> burnTime;
                case IDX_BURN_TOTAL -> burnTimeTotal;
                case IDX_HEAT -> heat;
                case IDX_MAX_HEAT -> maxHeat;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case IDX_ENERGY -> energyStorage.amount = value;
                case IDX_BURN_TIME -> burnTime = value;
                case IDX_BURN_TOTAL -> burnTimeTotal = value;
                case IDX_HEAT -> heat = value;
                default -> { }
            }
        }

        @Override
        public int size() {
            return PROPERTY_COUNT;
        }
    };

    public NuclearReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUCLEAR_REACTOR, pos, state);
        NuclearReactorBlock block = (NuclearReactorBlock) state.getBlock();
        this.energyStorage = new SimpleEnergyStorage(block.capacity, 0, block.maxExtract);
        this.genPerTick = block.genPerTick;
        this.burnTicksPerRod = block.burnTicksPerRod;
        this.maxHeat = block.maxHeat;
        this.heatPerTick = block.heatPerTick;
        // Explosionsstaerke skaliert mit der maxHitze des Reaktor-Typs.
        this.explosionPower = Math.min(12f, Math.max(4f, block.maxHeat / 400f));
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    public static void tick(World world, BlockPos pos, BlockState state, NuclearReactorBlockEntity be) {
        if (world.isClient()) {
            return;
        }
        boolean wasBurning = be.burnTime > 0;
        boolean dirty = false;

        // Laufenden Brennstab abbrennen und Energie erzeugen (mit Hitze-Drosselung)
        if (be.burnTime > 0) {
            be.burnTime--;
            int gen = be.genPerTick;
            if (be.heat >= be.maxHeat * THROTTLE_NUMERATOR / 4) {
                gen = Math.max(1, gen / 4);   // Ueberhitzung droht -> Erzeugung drosseln
            }
            if (be.energyStorage.amount < be.energyStorage.capacity) {
                be.energyStorage.amount = Math.min(be.energyStorage.capacity, be.energyStorage.amount + gen);
            }
            dirty = true;
        }

        // Neuen Brennstab zuenden (nur wenn Abfall-Slot Platz hat und kein Redstone-Signal)
        if (be.burnTime <= 0 && be.energyStorage.amount < be.energyStorage.capacity
                && !state.get(NuclearReactorBlock.POWERED)) {
            ItemStack fuel  = be.inventory.get(FUEL_SLOT);
            ItemStack waste = be.inventory.get(WASTE_SLOT);
            boolean wasteRoom = waste.isEmpty()
                    || (waste.isOf(ModItems.SPENT_FUEL_ROD) && waste.getCount() < waste.getMaxCount());
            if (fuel.isOf(ModItems.FUEL_ROD) && wasteRoom) {
                fuel.decrement(1);
                if (waste.isEmpty()) {
                    be.inventory.set(WASTE_SLOT, new ItemStack(ModItems.SPENT_FUEL_ROD));
                } else {
                    waste.increment(1);
                }
                be.burnTime = be.burnTicksPerRod;
                be.burnTimeTotal = be.burnTicksPerRod;
                dirty = true;
            }
        }

        // Hitze-Dynamik: Aufbau (reduziert durch Steuerstäbe), Abbau durch Kühlung
        int heatPerTickEffective = Math.max(0, be.heatPerTick
                - be.countControlRods(world, pos) * HEAT_REDUCTION_PER_ROD);
        int cooling = PASSIVE_COOL + be.countCoolingPipes(world, pos) * COOL_PER_PIPE;
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
            be.explode(world, pos);
            return;
        }

        // Energie an angrenzende Verbraucher/Speicher abgeben
        if (be.energyStorage.amount > 0) {
            be.pushEnergy(world, pos);
        }

        // Strahlung: laufender Reaktor bestrahlt Spieler in der Naehe
        if (be.burnTime > 0 && world instanceof ServerWorld serverWorld) {
            int level = be.heat >= be.maxHeat / 2 ? 1 : 0;
            Vec3d center = Vec3d.ofCenter(pos);
            Box searchBox = new Box(pos).expand(RADIATION_RADIUS);
            serverWorld.getEntitiesByClass(PlayerEntity.class, searchBox,
                    p -> p.squaredDistanceTo(center) <= (double) RADIATION_RADIUS * RADIATION_RADIUS)
                    .forEach(player -> {
                        if (!hasLeadShielding(serverWorld, pos, player.getBlockPos())) {
                            player.addStatusEffect(new StatusEffectInstance(
                                    ModEffects.RADIATION, 60, level, false, true));
                            if (serverWorld.getTime() % 20 == 0) {
                                player.damage(serverWorld,
                                        serverWorld.getDamageSources().magic(),
                                        level == 0 ? 0.5f : 1.5f);
                            }
                        }
                    });
        }

        // Strahlungspartikel sichtbar machen wenn Reaktor aktiv
        if (wasBurning && world instanceof ServerWorld sw && sw.getTime() % 10 == 0) {
            sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    3, 0.2, 0.2, 0.2, 0.01);
        }

        boolean nowBurning = be.burnTime > 0;
        if (nowBurning != wasBurning) {
            world.setBlockState(pos, state.with(NuclearReactorBlock.LIT, nowBurning), Block.NOTIFY_ALL);
            dirty = true;
        }

        // Komparator-Update (Energie-Füllstand 0-15)
        int comparatorLevel = be.getComparatorLevel();
        if (comparatorLevel != be.lastComparator) {
            be.lastComparator = comparatorLevel;
            world.updateComparators(pos, state.getBlock());
        }

        if (dirty) {
            be.markDirty();
        }
    }

    /** Energie-Füllstand als Redstone-Stärke 0–15. */
    public int getComparatorLevel() {
        if (energyStorage.amount <= 0) return 0;
        return (int) Math.max(1, energyStorage.amount * 15L / energyStorage.capacity);
    }

    /** Zaehlt direkt angrenzende Kuehlrohre (max. 6). */
    private int countCoolingPipes(World world, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.offset(dir)).isOf(ModBlocks.COOLING_PIPE)) count++;
        }
        return count;
    }

    /** Zaehlt direkt angrenzende Steuerstab-Bloecke (max. 6). */
    private int countControlRods(World world, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.offset(dir)).isOf(ModBlocks.CONTROL_ROD_BLOCK)) count++;
        }
        return count;
    }

    /**
     * Prueft ob ein Blei-Block auf dem direkten Pfad zwischen Quelle und Spieler liegt.
     * Schrittweite 1 Block — reicht fuer den max. 8-Block-Radius.
     */
    static boolean hasLeadShielding(World world, BlockPos reactorPos, BlockPos playerPos) {
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
            if (world.getBlockState(check).isOf(ModBlocks.LEAD_BLOCK)) return true;
        }
        return false;
    }

    /** Reaktor entfernen (Inhalt wird ausgeworfen) und Explosion ausloesen. */
    private void explode(World world, BlockPos pos) {
        world.removeBlock(pos, false);
        world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                explosionPower, true, World.ExplosionSourceType.BLOCK);
    }

    private void pushEnergy(World world, BlockPos pos) {
        EnergyNet.pushToNeighbors(energyStorage, world, pos, energyStorage.maxExtract);
    }

    // --- SidedInventory (Hopper-Kompatibilitaet) ---

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.UP)   return FUEL_SLOTS;
        if (side == Direction.DOWN) return WASTE_SLOTS;
        return NO_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == FUEL_SLOT && stack.isOf(ModItems.FUEL_ROD);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == WASTE_SLOT;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory);
        view.putLong("Energy", energyStorage.amount);
        view.putInt("BurnTime", burnTime);
        view.putInt("BurnTimeTotal", burnTimeTotal);
        view.putInt("Heat", heat);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, inventory);
        energyStorage.amount = view.getLong("Energy", 0L);
        burnTime = view.getInt("BurnTime", 0);
        burnTimeTotal = view.getInt("BurnTimeTotal", 0);
        heat = view.getInt("Heat", 0);
        lastComparator = getComparatorLevel();
    }

    // --- ExtendedScreenHandlerFactory ---

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new NuclearReactorScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }
}
