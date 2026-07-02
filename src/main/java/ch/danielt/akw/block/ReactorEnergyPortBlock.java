package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.ReactorEnergyPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Energie-Port für den Multiblock-Reaktor: einziger FE-Abgabepunkt der Hülle.
 * Delegiert den Speicherzugriff an die Controller-BlockEntity und gibt pro Tick
 * aktiv an angrenzende Verbraucher ab.
 */
public class ReactorEnergyPortBlock extends Block implements EntityBlock {

    public ReactorEnergyPortBlock(Properties settings) {
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorEnergyPortBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (w, pos, st, be) -> {
            if (be instanceof ReactorEnergyPortBlockEntity port) {
                ReactorEnergyPortBlockEntity.tick(w, pos, st, port);
            }
        };
    }
}
