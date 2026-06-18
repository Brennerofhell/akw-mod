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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Energie erzeugender Reaktor-Block. Eine Instanz pro Reaktor-Typ; die
 * Tier-Werte werden im Konstruktor festgelegt und von der gemeinsamen
 * {@link NuclearReactorBlockEntity} ausgelesen.
 */
public class NuclearReactorBlock extends Block implements BlockEntityProvider {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public final int capacity;
    public final int genPerTick;
    public final int maxExtract;
    public final int burnTicksPerRod;
    /** Maximale Hitze; bei Erreichen explodiert der Reaktor. */
    public final int maxHeat;
    /** Hitzeaufbau pro Tick, solange ein Brennstab aktiv ist. */
    public final int heatPerTick;

    public NuclearReactorBlock(Settings settings, int capacity, int genPerTick,
                               int maxExtract, int burnTicksPerRod, int maxHeat, int heatPerTick) {
        super(settings);
        this.capacity = capacity;
        this.genPerTick = genPerTick;
        this.maxExtract = maxExtract;
        this.burnTicksPerRod = burnTicksPerRod;
        this.maxHeat = maxHeat;
        this.heatPerTick = heatPerTick;
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
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
        if (world.isClient()) {
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
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof NuclearReactorBlockEntity reactor) {
                player.openHandledScreen(reactor);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof NuclearReactorBlockEntity reactor) {
            ItemScatterer.spawn(world, pos, reactor);
        }
        super.onStateReplaced(state, world, pos, moved);
    }
}
