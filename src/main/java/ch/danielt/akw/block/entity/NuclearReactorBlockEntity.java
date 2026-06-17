package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.NuclearReactorBlock;
import ch.danielt.akw.registry.ModBlockEntities;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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
 * (Kapazitaet, FE/Tick, Abgaberate, Brenndauer) werden aus dem zugehoerigen
 * {@link NuclearReactorBlock} gelesen, sodass alle Typen denselben Code teilen.
 */
public class NuclearReactorBlockEntity extends BlockEntity
        implements ImplementedInventory, ExtendedScreenHandlerFactory<BlockPos> {

    public static final int FUEL_SLOT = 0;

    /** PropertyDelegate-Indizes (gemeinsam von BlockEntity, ScreenHandler, Screen genutzt). */
    public static final int IDX_ENERGY = 0;
    public static final int IDX_CAPACITY = 1;
    public static final int IDX_BURN_TIME = 2;
    public static final int IDX_BURN_TOTAL = 3;
    public static final int PROPERTY_COUNT = 4;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    /** Energiespeicher: kein Input (Generator), nur Abgabe. */
    public final SimpleEnergyStorage energyStorage;
    private final int genPerTick;
    private final int burnTicksPerRod;

    private int burnTime;
    private int burnTimeTotal;

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
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case IDX_ENERGY -> energyStorage.amount = value;
                case IDX_BURN_TIME -> burnTime = value;
                case IDX_BURN_TOTAL -> burnTimeTotal = value;
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
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    public static void tick(World world, BlockPos pos, BlockState state, NuclearReactorBlockEntity be) {
        if (world.isClient) {
            return;
        }
        boolean wasBurning = be.burnTime > 0;
        boolean dirty = false;

        // Laufenden Brennstab abbrennen und Energie erzeugen
        if (be.burnTime > 0) {
            be.burnTime--;
            if (be.energyStorage.amount < be.energyStorage.capacity) {
                be.energyStorage.amount =
                        Math.min(be.energyStorage.capacity, be.energyStorage.amount + be.genPerTick);
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
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, inventory, registries);
        nbt.putLong("Energy", energyStorage.amount);
        nbt.putInt("BurnTime", burnTime);
        nbt.putInt("BurnTimeTotal", burnTimeTotal);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        Inventories.readNbt(nbt, inventory, registries);
        energyStorage.amount = nbt.getLong("Energy");
        burnTime = nbt.getInt("BurnTime");
        burnTimeTotal = nbt.getInt("BurnTimeTotal");
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
