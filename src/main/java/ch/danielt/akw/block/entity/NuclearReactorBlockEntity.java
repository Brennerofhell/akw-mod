package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.NuclearReactorBlock;
import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.screen.NuclearReactorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
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
        implements ImplementedInventory, ExtendedScreenHandlerFactory<BlockPos> {

    public static final int FUEL_SLOT = 0;

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

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

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

        // Neuen Brennstab zuenden, wenn Platz fuer Energie ist
        if (be.burnTime <= 0 && be.energyStorage.amount < be.energyStorage.capacity) {
            ItemStack fuel = be.inventory.get(FUEL_SLOT);
            if (fuel.isOf(ModItems.FUEL_ROD)) {
                fuel.decrement(1);
                be.burnTime = be.burnTicksPerRod;
                be.burnTimeTotal = be.burnTicksPerRod;
                dirty = true;
            }
        }

        // Hitze-Dynamik: Aufbau beim Brennen, Abbau durch Eigenkuehlung + Kuehlrohre
        int cooling = PASSIVE_COOL + be.countCoolingPipes(world, pos) * COOL_PER_PIPE;
        int oldHeat = be.heat;
        if (wasBurning) {
            be.heat += be.heatPerTick;
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

        boolean nowBurning = be.burnTime > 0;
        if (nowBurning != wasBurning) {
            world.setBlockState(pos, state.with(NuclearReactorBlock.LIT, nowBurning), Block.NOTIFY_ALL);
            dirty = true;
        }
        if (dirty) {
            be.markDirty();
        }
    }

    /** Zaehlt direkt angrenzende Kuehlrohre (max. 6). */
    private int countCoolingPipes(World world, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.offset(dir)).isOf(ModBlocks.COOLING_PIPE)) {
                count++;
            }
        }
        return count;
    }

    /** Reaktor entfernen (Inhalt wird ausgeworfen) und Explosion ausloesen. */
    private void explode(World world, BlockPos pos) {
        world.removeBlock(pos, false);
        world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                explosionPower, true, World.ExplosionSourceType.BLOCK);
    }

    private void pushEnergy(World world, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            EnergyStorage target = EnergyStorage.SIDED.find(world, pos.offset(dir), dir.getOpposite());
            if (target == null) {
                continue;
            }
            try (Transaction tx = Transaction.openOuter()) {
                EnergyStorageUtil.move(energyStorage, target, energyStorage.maxExtract, tx);
                tx.commit();
            }
        }
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
