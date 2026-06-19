package ch.danielt.akw.block.entity;

import ch.danielt.akw.energy.EnergyNet;
import ch.danielt.akw.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/**
 * Akku-Block: grosser FE-Puffer, der Reaktor-Spitzen aufnimmt und an
 * Verbraucher/Kabel weitergibt. Nimmt FE über alle Seiten an und gibt es über
 * alle Seiten ab. Der Füllstand wird als Komparator-Signal (0–15) ausgegeben,
 * sodass man den Ladestand per Redstone ablesen kann (GUI folgt in der Politur).
 */
public class EnergyBatteryBlockEntity extends BlockEntity {

    public static final long CAPACITY = 1_000_000;
    public static final long TRANSFER = 4_096;

    public final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(CAPACITY, TRANSFER, TRANSFER) {
        @Override
        protected void onFinalCommit() {
            EnergyBatteryBlockEntity.this.markDirty();
        }
    };

    private int lastComparator;

    public EnergyBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_BATTERY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, EnergyBatteryBlockEntity be) {
        if (world.isClient()) {
            return;
        }
        EnergyNet.pushToNeighbors(be.energyStorage, world, pos, TRANSFER);

        int level = be.getComparatorLevel();
        if (level != be.lastComparator) {
            be.lastComparator = level;
            world.updateComparators(pos, state.getBlock());
        }
    }

    /** Füllstand als Redstone-Stärke 0–15. */
    public int getComparatorLevel() {
        if (energyStorage.amount <= 0) {
            return 0;
        }
        return (int) Math.max(1, energyStorage.amount * 15 / energyStorage.capacity);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putLong("Energy", energyStorage.amount);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        energyStorage.amount = view.getLong("Energy", 0L);
        lastComparator = getComparatorLevel();
    }
}
