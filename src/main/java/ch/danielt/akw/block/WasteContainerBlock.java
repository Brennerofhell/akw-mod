package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.WasteContainerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Abfallbehälter für Verbrauchte Brennstäbe. Gibt den Füllstand als
 * Komparator-Signal (0–15) aus. Bei Füllstand > 50 % strahlt der Block
 * passiv (Level 0) in einem Radius von 5 Blöcken — Blei-Block schützt.
 */
public class WasteContainerBlock extends Block implements BlockEntityProvider {

    public WasteContainerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WasteContainerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                   BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return (w, pos, st, be) -> {
            if (be instanceof WasteContainerBlockEntity waste) {
                WasteContainerBlockEntity.tick(w, pos, st, waste);
            }
        };
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof WasteContainerBlockEntity be) {
            return be.getComparatorLevel();
        }
        return 0;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                  BlockHitResult hit) {
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof WasteContainerBlockEntity waste) {
                player.openHandledScreen(waste);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof WasteContainerBlockEntity waste) {
            ItemScatterer.spawn(world, pos, waste);
        }
        super.onStateReplaced(state, world, pos, moved);
    }
}
