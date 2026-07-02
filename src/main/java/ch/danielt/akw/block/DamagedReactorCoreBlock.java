package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.MultiblockReactorControllerBlockEntity;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Beschädigter Reaktorkern (entsteht bei 100 % Hitze statt einer Explosion).
 * Produziert nichts; Reparatur per Schraubenschlüssel-Rechtsklick, sobald der
 * zugehörige Reaktor abgekühlt ist.
 */
public class DamagedReactorCoreBlock extends Block {

    /** Suchradius für den zugehörigen Controller (max. Hülle 9 → Controller ≤ 8 Blöcke entfernt). */
    private static final int CONTROLLER_SEARCH_RADIUS = 8;

    public DamagedReactorCoreBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().is(ModItems.REACTOR_WRENCH)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (isNearbyReactorTooHot(level, pos)) {
            player.displayClientMessage(Component.translatable("akw.damaged_core.too_hot"), true);
            return InteractionResult.SUCCESS;
        }
        level.setBlock(pos, ModBlocks.REACTOR_CORE.get().defaultBlockState(), Block.UPDATE_ALL);
        player.displayClientMessage(Component.translatable("akw.damaged_core.repaired"), true);
        return InteractionResult.SUCCESS;
    }

    private static boolean isNearbyReactorTooHot(Level level, BlockPos pos) {
        for (BlockPos current : BlockPos.betweenClosed(
                pos.offset(-CONTROLLER_SEARCH_RADIUS, -CONTROLLER_SEARCH_RADIUS, -CONTROLLER_SEARCH_RADIUS),
                pos.offset(CONTROLLER_SEARCH_RADIUS, CONTROLLER_SEARCH_RADIUS, CONTROLLER_SEARCH_RADIUS))) {
            if (level.getBlockEntity(current)
                    instanceof MultiblockReactorControllerBlockEntity controller
                    && controller.isTooHotForRepair()) {
                return true;
            }
        }
        return false;
    }
}
