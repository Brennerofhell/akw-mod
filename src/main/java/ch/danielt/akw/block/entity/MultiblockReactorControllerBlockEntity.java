package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.MultiblockReactorControllerBlock;
import ch.danielt.akw.energy.EnergyNet;
import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.screen.MultiblockReactorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
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
import team.reborn.energy.api.base.SimpleEnergyStorage;

/**
 * BlockEntity für den Multiblock-Reaktor-Controller. Leistung skaliert mit
 * dem Innenvolumen des Reaktors: (outerSize − 2)³.
 */
public class MultiblockReactorControllerBlockEntity extends BlockEntity
        implements ImplementedInventory, ExtendedScreenHandlerFactory<BlockPos> {

    public static final int FUEL_SLOT = 0;

    // Gleiche PropertyDelegate-Indizes wie NuclearReactorBlockEntity → NuclearReactorScreen wiederverwendbar
    private static final int IDX_ENERGY     = NuclearReactorBlockEntity.IDX_ENERGY;
    private static final int IDX_CAPACITY   = NuclearReactorBlockEntity.IDX_CAPACITY;
    private static final int IDX_BURN_TIME  = NuclearReactorBlockEntity.IDX_BURN_TIME;
    private static final int IDX_BURN_TOTAL = NuclearReactorBlockEntity.IDX_BURN_TOTAL;
    private static final int IDX_HEAT       = NuclearReactorBlockEntity.IDX_HEAT;
    private static final int IDX_MAX_HEAT   = NuclearReactorBlockEntity.IDX_MAX_HEAT;
    private static final int PROPERTY_COUNT = NuclearReactorBlockEntity.PROPERTY_COUNT;

    private static final int RADIATION_RADIUS   = 8;
    private static final int BASE_GEN_PER_TICK  = 50;
    private static final int BASE_CAPACITY      = 200_000;
    private static final int BASE_BURN_TICKS    = 2_400;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    // Maximalwerte für 7×7×7 im Konstruktor; effektive Werte aus Methoden
    public final SimpleEnergyStorage energyStorage =
            new SimpleEnergyStorage(20_000_000L, 0L, 6_250L);

    private int burnTime;
    private int burnTimeTotal;
    private int heat;
    private int reactorSize = 0;   // 0 = nicht assembliert
    private int revalidateTimer = 0;
    private int lastComparator = -1;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case IDX_ENERGY     -> (int) Math.min(energyStorage.amount, Integer.MAX_VALUE);
                case IDX_CAPACITY   -> effCapacity();
                case IDX_BURN_TIME  -> burnTime;
                case IDX_BURN_TOTAL -> burnTimeTotal;
                case IDX_HEAT       -> heat;
                case IDX_MAX_HEAT   -> effMaxHeat();
                default             -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            switch (index) {
                case IDX_ENERGY     -> energyStorage.amount = value;
                case IDX_BURN_TIME  -> burnTime = value;
                case IDX_BURN_TOTAL -> burnTimeTotal = value;
                case IDX_HEAT       -> heat = value;
                default -> { }
            }
        }
        @Override
        public int size() { return PROPERTY_COUNT; }
    };

    public MultiblockReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIBLOCK_REACTOR_CONTROLLER, pos, state);
    }

    // --- Skalierung ---

    private int innerVolume() {
        if (reactorSize < 3) return 1;
        int e = reactorSize - 2;
        return e * e * e;
    }
    private int effGenPerTick()  { return BASE_GEN_PER_TICK * innerVolume(); }
    private int effCapacity()    { return BASE_CAPACITY * innerVolume(); }
    private int effMaxHeat()     { return 1_600 + 800 * Math.max(0, reactorSize - 3); }
    private int effBurnTicks()   { return BASE_BURN_TICKS + 400 * Math.max(0, reactorSize - 3); }

    // --- Inventar ---

    @Override
    public DefaultedList<ItemStack> getItems() { return inventory; }

    public PropertyDelegate getPropertyDelegate() { return propertyDelegate; }

    // --- Validierung ---

    public boolean tryAssemble(World world, BlockPos pos, BlockState state) {
        Direction facing = state.get(MultiblockReactorControllerBlock.FACING);
        for (int size : new int[]{7, 5, 3}) {
            if (checkStructure(world, pos, facing, size)) {
                reactorSize = size;
                heat = 0;
                world.setBlockState(pos,
                        state.with(MultiblockReactorControllerBlock.ASSEMBLED, true),
                        Block.NOTIFY_ALL);
                markDirty();
                return true;
            }
        }
        return false;
    }

    public void disassemble(World world, BlockPos pos, BlockState state) {
        reactorSize = 0;
        burnTime = 0;
        heat = 0;
        world.setBlockState(pos,
                state.with(MultiblockReactorControllerBlock.ASSEMBLED, false)
                     .with(MultiblockReactorControllerBlock.LIT, false),
                Block.NOTIFY_ALL);
        markDirty();
    }

    private boolean checkStructure(World world, BlockPos controllerPos,
                                    Direction facing, int outerSize) {
        Direction forward = facing.getOpposite();
        Direction right   = facing.rotateYClockwise();
        int half = outerSize / 2;

        for (int dx = -half; dx <= half; dx++) {
            for (int dy = -half; dy <= half; dy++) {
                // Frontwand (dz=0): alles außer Controller-Mitte muss CASING sein
                if (dx != 0 || dy != 0) {
                    BlockPos p = controllerPos.offset(right, dx).offset(Direction.UP, dy);
                    if (!world.getBlockState(p).isOf(ModBlocks.REACTOR_CASING)) return false;
                }
                // Körper hinter dem Controller
                for (int dz = 1; dz < outerSize; dz++) {
                    boolean isShell = Math.abs(dx) == half
                            || Math.abs(dy) == half
                            || dz == outerSize - 1;
                    if (isShell) {
                        BlockPos p = controllerPos
                                .offset(right, dx)
                                .offset(Direction.UP, dy)
                                .offset(forward, dz);
                        if (!world.getBlockState(p).isOf(ModBlocks.REACTOR_CASING)) return false;
                    }
                }
            }
        }
        return true;
    }

    // --- Tick ---

    public static void tick(World world, BlockPos pos, BlockState state,
                             MultiblockReactorControllerBlockEntity be) {
        if (world.isClient()) return;
        if (!state.get(MultiblockReactorControllerBlock.ASSEMBLED)) return;

        // Struktur alle 100 Ticks re-validieren
        if (++be.revalidateTimer >= 100) {
            be.revalidateTimer = 0;
            Direction facing = state.get(MultiblockReactorControllerBlock.FACING);
            if (!be.checkStructure(world, pos, facing, be.reactorSize)) {
                be.disassemble(world, pos, state);
                return;
            }
        }

        boolean wasBurning = be.burnTime > 0;
        boolean dirty = false;

        // Brennstab verbrauchen, Energie erzeugen
        if (be.burnTime > 0) {
            be.burnTime--;
            long cap = be.effCapacity();
            if (be.energyStorage.amount < cap) {
                be.energyStorage.amount = Math.min(cap, be.energyStorage.amount + be.effGenPerTick());
            }
            dirty = true;
        }

        // Neuen Brennstab zünden (kein Zünden bei Redstone-Signal)
        if (be.burnTime <= 0 && be.energyStorage.amount < be.effCapacity()
                && !state.get(MultiblockReactorControllerBlock.POWERED)) {
            ItemStack fuel = be.inventory.get(FUEL_SLOT);
            if (fuel.isOf(ModItems.FUEL_ROD)) {
                fuel.decrement(1);
                be.burnTime = be.effBurnTicks();
                be.burnTimeTotal = be.burnTime;
                dirty = true;
            }
        }

        // Hitze-Dynamik
        int oldHeat = be.heat;
        if (wasBurning) {
            be.heat += 2 * be.reactorSize;
        }
        be.heat = Math.max(0, be.heat - 1); // Passivkühlung
        if (be.heat != oldHeat) dirty = true;

        // Überhitzung → Explosion
        if (be.heat >= be.effMaxHeat()) {
            be.explode(world, pos, state);
            return;
        }

        // Energie abgeben
        if (be.energyStorage.amount > 0) {
            EnergyNet.pushToNeighbors(be.energyStorage, world, pos,
                    (long) be.effGenPerTick() * 2);
        }

        // Strahlung (pfadbasierter Blei-Block-Schutz)
        if (be.burnTime > 0 && world instanceof ServerWorld serverWorld) {
            int level = be.heat >= be.effMaxHeat() / 2 ? 1 : 0;
            Vec3d center = Vec3d.ofCenter(pos);
            Box box = new Box(pos).expand(RADIATION_RADIUS);
            serverWorld.getEntitiesByClass(PlayerEntity.class, box,
                    p -> p.squaredDistanceTo(center) <= (double) RADIATION_RADIUS * RADIATION_RADIUS)
                    .forEach(player -> {
                        if (!NuclearReactorBlockEntity.hasLeadShielding(world, pos, player.getBlockPos())) {
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

        boolean nowBurning = be.burnTime > 0;
        if (nowBurning != wasBurning) {
            world.setBlockState(pos,
                    state.with(MultiblockReactorControllerBlock.LIT, nowBurning),
                    Block.NOTIFY_ALL);
            dirty = true;
        }

        // Komparator-Update (Energie-Füllstand 0-15)
        int comparatorLevel = be.getComparatorLevel();
        if (comparatorLevel != be.lastComparator) {
            be.lastComparator = comparatorLevel;
            world.updateComparators(pos, state.getBlock());
        }

        if (dirty) be.markDirty();
    }

    /** Energie-Füllstand als Redstone-Stärke 0–15. */
    public int getComparatorLevel() {
        int cap = effCapacity();
        if (energyStorage.amount <= 0 || cap <= 0) return 0;
        return (int) Math.max(1, energyStorage.amount * 15L / cap);
    }

    private void explode(World world, BlockPos pos, BlockState state) {
        Direction facing = state.get(MultiblockReactorControllerBlock.FACING);
        Direction forward = facing.getOpposite();
        Direction right   = facing.rotateYClockwise();
        int half = reactorSize / 2;
        for (int dx = -half; dx <= half; dx++) {
            for (int dy = -half; dy <= half; dy++) {
                for (int dz = 0; dz < reactorSize; dz++) {
                    BlockPos bp = pos.offset(right, dx).offset(Direction.UP, dy).offset(forward, dz);
                    if (world.getBlockState(bp).isOf(ModBlocks.REACTOR_CASING)) {
                        world.removeBlock(bp, false);
                    }
                }
            }
        }
        world.removeBlock(pos, false);
        world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                4f + reactorSize * 2f, true, World.ExplosionSourceType.BLOCK);
    }

    // --- Persistenz ---

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory);
        view.putLong("Energy", energyStorage.amount);
        view.putInt("BurnTime", burnTime);
        view.putInt("BurnTimeTotal", burnTimeTotal);
        view.putInt("Heat", heat);
        view.putInt("ReactorSize", reactorSize);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, inventory);
        energyStorage.amount = view.getLong("Energy", 0L);
        burnTime  = view.getInt("BurnTime", 0);
        burnTimeTotal = view.getInt("BurnTimeTotal", 0);
        heat      = view.getInt("Heat", 0);
        reactorSize = view.getInt("ReactorSize", 0);
        lastComparator = getComparatorLevel();
    }

    // --- Screen ---

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new MultiblockReactorScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }
}
