package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.EnergyCableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Energie-Kabel-Block. Transportiert FE über sein {@link EnergyCableBlockEntity}
 * zwischen angrenzenden Blöcken (Reaktoren, Akkus, Verbrauchern).
 */
public class EnergyCableBlock extends Block implements BlockEntityProvider {

    public EnergyCableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (world.isClient()) {
            return null;
        }
        return (w, pos, st, be) -> {
            if (be instanceof EnergyCableBlockEntity cable) {
                EnergyCableBlockEntity.tick(w, pos, st, cable);
            }
        };
    }
}
