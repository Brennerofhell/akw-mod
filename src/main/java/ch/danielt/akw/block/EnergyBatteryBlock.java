package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.EnergyBatteryBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Akku-Block: speichert FE und gibt den Füllstand als Komparator-Signal aus.
 */
public class EnergyBatteryBlock extends Block implements BlockEntityProvider {

    public EnergyBatteryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyBatteryBlockEntity(pos, state);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof EnergyBatteryBlockEntity be) {
            return be.getComparatorLevel();
        }
        return 0;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (world.isClient()) {
            return null;
        }
        return (w, pos, st, be) -> {
            if (be instanceof EnergyBatteryBlockEntity battery) {
                EnergyBatteryBlockEntity.tick(w, pos, st, battery);
            }
        };
    }
}
