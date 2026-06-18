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
 * Energie-Kabel: kleiner FE-Puffer, der jeden Tick an alle Nachbarn weitergibt.
 * Dadurch fliesst FE entlang einer Kabelstrecke (ein Block pro Tick) von
 * Erzeugern (Reaktor) zu Verbrauchern/Speichern. Nimmt FE über alle Seiten an
 * und gibt es über alle Seiten ab ({@link team.reborn.energy.api.EnergyStorage#SIDED}).
 */
public class EnergyCableBlockEntity extends BlockEntity {

    public static final long CAPACITY = 8_192;
    public static final long TRANSFER = 2_048;

    public final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(CAPACITY, TRANSFER, TRANSFER) {
        @Override
        protected void onFinalCommit() {
            EnergyCableBlockEntity.this.markDirty();
        }
    };

    public EnergyCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CABLE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, EnergyCableBlockEntity be) {
        if (world.isClient()) {
            return;
        }
        EnergyNet.pushToNeighbors(be.energyStorage, world, pos, TRANSFER);
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
    }
}
