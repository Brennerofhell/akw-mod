package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Energie erzeugender Reaktor-Block. Eine Instanz pro Reaktor-Typ; die
 * Tier-Werte werden im Konstruktor festgelegt und von der gemeinsamen
 * {@link NuclearReactorBlockEntity} ausgelesen.
 */
public class NuclearReactorBlock extends Block implements BlockEntityProvider {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public final int capacity;
    public final int genPerTick;
    public final int maxExtract;
    public final int burnTicksPerRod;

    public NuclearReactorBlock(Settings settings, int capacity, int genPerTick,
                               int maxExtract, int burnTicksPerRod) {
        super(settings);
        this.capacity = capacity;
        this.genPerTick = genPerTick;
        this.maxExtract = maxExtract;
        this.burnTicksPerRod = burnTicksPerRod;
        setDefaultState(getDefaultState()
                .with(FACING, net.minecraft.util.math.Direction.NORTH)
                .with(LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NuclearReactorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (world.isClient) {
            return null;
        }
        return (w, pos, st, be) -> {
            if (be instanceof NuclearReactorBlockEntity reactor) {
                NuclearReactorBlockEntity.tick(w, pos, st, reactor);
            }
        };
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                 BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof NuclearReactorBlockEntity reactor) {
                player.openHandledScreen(reactor);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState,
                                   boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof NuclearReactorBlockEntity reactor) {
                ItemScatterer.spawn(world, pos, reactor);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
