package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.ReactorRedstonePortBlockEntity;
import ch.danielt.akw.reactor.RedstonePortMode;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Redstone-Port für den Multiblock-Reaktor. Kann als Redstone-Eingang
 * oder als Redstone-Ausgang (über Komparatorwert) betrieben werden.
 */
public class ReactorRedstonePortBlock extends Block implements EntityBlock {

    public static final EnumProperty<RedstonePortMode> MODE = EnumProperty.create("mode", RedstonePortMode.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ReactorRedstonePortBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState()
                .setValue(MODE, RedstonePortMode.INPUT)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE, POWERED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorRedstonePortBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player.getMainHandItem().is(ModItems.REACTOR_WRENCH)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        RedstonePortMode next = state.getValue(MODE).next();
        BlockState newState = state.setValue(MODE, next);
        if (next == RedstonePortMode.OUTPUT) {
            // Beim Umschalten auf Output berechnen wir das Signal
            if (level.getBlockEntity(pos) instanceof ReactorRedstonePortBlockEntity be) {
                int sig = be.getOutputSignal();
                newState = newState.setValue(POWERED, sig > 0);
            }
        } else {
            // Input-Modus: aktuelle Neighbor-Power lesen
            newState = newState.setValue(POWERED, level.hasNeighborSignal(pos));
        }
        level.setBlock(pos, newState, Block.UPDATE_ALL);
        player.displayClientMessage(Component.translatable(next.translationKey()), true);

        // Nachbarmeldung triggern bei Moduswechsel
        level.updateNeighborsAt(pos, this);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) return;

        if (state.getValue(MODE) == RedstonePortMode.INPUT) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
                // Controller benachrichtigen, dass sich der Input-Status geändert hat
                if (level.getBlockEntity(pos) instanceof ReactorRedstonePortBlockEntity be) {
                    be.notifyControllerOfSignalChange();
                }
            }
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(MODE) == RedstonePortMode.OUTPUT;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.getValue(MODE) == RedstonePortMode.OUTPUT) {
            if (level.getBlockEntity(pos) instanceof ReactorRedstonePortBlockEntity be) {
                return be.getOutputSignal();
            }
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }
}
