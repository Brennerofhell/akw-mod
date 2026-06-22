package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.EnergyCableBlockEntity;
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
 * Energie-Kabel-Block. Transportiert FE über sein {@link EnergyCableBlockEntity}
 * zwischen angrenzenden Blöcken (Reaktoren, Akkus, Verbrauchern).
 */
public class EnergyCableBlock extends Block implements EntityBlock {

    public EnergyCableBlock(Properties settings) {
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (w, pos, st, be) -> {
            if (be instanceof EnergyCableBlockEntity cable) {
                EnergyCableBlockEntity.tick(w, pos, st, cable);
            }
        };
    }
}
