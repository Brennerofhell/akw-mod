package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.MultiblockReactorControllerBlockEntity;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

/**
 * Controller-Block für Multiblock-Reaktoren. Rechtsklick ohne Schraubenschlüssel
 * öffnet das GUI (wenn assembliert). Rechtsklick mit Schraubenschlüssel assembliert
 * (oder disassembliert) den Reaktor.
 */
public class MultiblockReactorControllerBlock extends Block implements BlockEntityProvider {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.of("assembled");
    public static final BooleanProperty LIT = Properties.LIT;
    public static final BooleanProperty POWERED = Properties.POWERED;

    public MultiblockReactorControllerBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(ASSEMBLED, false)
                .with(LIT, false)
                .with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ASSEMBLED, LIT, POWERED);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof MultiblockReactorControllerBlockEntity be) {
            return be.getComparatorLevel();
        }
        return 0;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock,
                                   WireOrientation wireOrientation, boolean notify) {
        boolean powered = world.isReceivingRedstonePower(pos);
        if (powered != state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_ALL);
        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MultiblockReactorControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                   BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return (w, pos, st, be) -> {
            if (be instanceof MultiblockReactorControllerBlockEntity controller) {
                MultiblockReactorControllerBlockEntity.tick(w, pos, st, controller);
            }
        };
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                  PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof MultiblockReactorControllerBlockEntity controller)) {
            return ActionResult.PASS;
        }

        boolean holdingWrench = player.getMainHandStack().isOf(ModItems.REACTOR_WRENCH);

        if (holdingWrench) {
            if (state.get(ASSEMBLED)) {
                // Disassemblieren
                controller.disassemble(world, pos, state);
                player.sendMessage(Text.translatable("akw.multiblock.disassembled"), true);
            } else {
                // Assemblieren
                if (controller.tryAssemble(world, pos, state)) {
                    player.sendMessage(Text.translatable("akw.multiblock.assembled"), true);
                } else {
                    player.sendMessage(Text.translatable("akw.multiblock.invalid"), true);
                }
            }
            return ActionResult.SUCCESS;
        }

        // Kein Wrench: GUI öffnen (nur wenn assembliert)
        if (state.get(ASSEMBLED)) {
            player.openHandledScreen(controller);
        } else {
            player.sendMessage(Text.translatable("akw.multiblock.need_wrench"), true);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MultiblockReactorControllerBlockEntity controller) {
            ItemScatterer.spawn(world, pos, controller);
        }
        super.onStateReplaced(state, world, pos, moved);
    }
}
