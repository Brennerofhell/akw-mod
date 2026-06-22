package ch.danielt.akw.block.entity;

import ch.danielt.akw.energy.EnergyNet;
import ch.danielt.akw.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Energie-Kabel: kleiner FE-Puffer, der jeden Tick an alle Nachbarn weitergibt.
 * Dadurch fliesst FE entlang einer Kabelstrecke (ein Block pro Tick) von
 * Erzeugern (Reaktor) zu Verbrauchern/Speichern. Nimmt FE ueber alle Seiten an
 * und gibt es ueber alle Seiten ab.
 */
public class EnergyCableBlockEntity extends BlockEntity {

    public static final int CAPACITY = 8_192;
    public static final int TRANSFER = 2_048;

    public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(CAPACITY, TRANSFER);

    public EnergyCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CABLE.get(), pos, state);
        energyStorage.setOnChange(this::setChanged);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyCableBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        EnergyNet.pushToNeighbors(be.energyStorage, level, pos, TRANSFER);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setEnergy(input.getIntOr("Energy", 0));
    }
}
