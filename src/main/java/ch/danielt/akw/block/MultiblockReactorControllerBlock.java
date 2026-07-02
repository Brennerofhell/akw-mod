package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.MultiblockReactorControllerBlockEntity;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Controller-Block für Multiblock-Reaktoren. Rechtsklick ohne Schraubenschlüssel
 * öffnet das GUI (wenn assembliert). Rechtsklick mit Schraubenschlüssel assembliert
 * (oder disassembliert) den Reaktor.
 */
public class MultiblockReactorControllerBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public MultiblockReactorControllerBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ASSEMBLED, false)
                .setValue(LIT, false)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ASSEMBLED, LIT, POWERED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof MultiblockReactorControllerBlockEntity be) {
            return be.getComparatorLevel();
        }
        return 0;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                @Nullable Orientation orientation, boolean movedByPiston) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiblockReactorControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (w, pos, st, be) -> {
            if (be instanceof MultiblockReactorControllerBlockEntity controller) {
                MultiblockReactorControllerBlockEntity.tick(w, pos, st, controller);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MultiblockReactorControllerBlockEntity controller)) {
            return InteractionResult.PASS;
        }

        boolean holdingWrench = player.getMainHandItem().is(ModItems.REACTOR_WRENCH);

        if (holdingWrench) {
            if (state.getValue(ASSEMBLED)) {
                // Disassemblieren
                controller.disassemble(level, pos, state);
                player.displayClientMessage(Component.translatable("akw.multiblock.disassembled"), true);
            } else {
                // Assemblieren
                if (controller.tryAssemble(level, pos, state)) {
                    player.displayClientMessage(Component.translatable("akw.multiblock.assembled",
                            controller.getLayout().coreCount(),
                            controller.getLayout().connectedCoolingPipeCount()), true);
                } else {
                    player.displayClientMessage(Component.translatable("akw.multiblock.invalid"), true);
                    for (Component line : controller.getLastErrorComponents()) {
                        player.displayClientMessage(line, false);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Kein Wrench: GUI öffnen — auch unassembliert (Diagnose-Tab zeigt die Fehlerliste)
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(controller, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

}
