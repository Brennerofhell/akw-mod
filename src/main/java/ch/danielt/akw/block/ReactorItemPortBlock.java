package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.ReactorItemPortBlockEntity;
import ch.danielt.akw.reactor.ItemPortMode;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Item-Port für den Multiblock-Reaktor: leitet Hopper-Zugriffe je nach Modus auf
 * den Brennstoff- oder Abfall-Slot des Controllers. Rechtsklick ohne
 * Schraubenschlüssel wechselt den Modus.
 */
public class ReactorItemPortBlock extends Block implements EntityBlock {

    public static final EnumProperty<ItemPortMode> MODE = EnumProperty.create("mode", ItemPortMode.class);

    public ReactorItemPortBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(MODE, ItemPortMode.FUEL_INPUT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorItemPortBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player.getMainHandItem().is(ModItems.REACTOR_WRENCH)) {
            // Assemblierung läuft über den Controller — Wrench hier durchreichen.
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemPortMode next = state.getValue(MODE).next();
        level.setBlock(pos, state.setValue(MODE, next), Block.UPDATE_ALL);
        player.displayClientMessage(Component.translatable(next.translationKey()), true);
        return InteractionResult.SUCCESS;
    }
}
