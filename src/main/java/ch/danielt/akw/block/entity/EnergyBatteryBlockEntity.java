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
 * Akku-Block: grosser FE-Puffer, der Reaktor-Spitzen aufnimmt und an
 * Verbraucher/Kabel weitergibt. Nimmt FE ueber alle Seiten an und gibt es ueber
 * alle Seiten ab. Der Fuellstand wird als Komparator-Signal (0–15) ausgegeben,
 * sodass man den Ladestand per Redstone ablesen kann (GUI folgt in der Politur).
 */
public class EnergyBatteryBlockEntity extends BlockEntity {

    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER = 4_096;

    public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(CAPACITY, TRANSFER);

    private int lastComparator;

    public EnergyBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_BATTERY.get(), pos, state);
        energyStorage.setOnChange(this::setChanged);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyBatteryBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        EnergyNet.pushToNeighbors(be.energyStorage, level, pos, TRANSFER);

        int lvl = be.getComparatorLevel();
        if (lvl != be.lastComparator) {
            be.lastComparator = lvl;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    /** Fuellstand als Redstone-Staerke 0–15. */
    public int getComparatorLevel() {
        if (energyStorage.getEnergyStored() <= 0) {
            return 0;
        }
        return (int) Math.max(1, (long) energyStorage.getEnergyStored() * 15 / energyStorage.getMaxEnergyStored());
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
        lastComparator = getComparatorLevel();
    }
}
