package ch.danielt.akw.block;

import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Energie erzeugender Reaktor-Block. Eine Instanz pro Reaktor-Typ; die
 * Tier-Werte werden im Konstruktor festgelegt und von der gemeinsamen
 * {@link NuclearReactorBlockEntity} ausgelesen.
 */
public class NuclearReactorBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    /** Redstone-Signal an → kein neues Zünden; laufender Stab brennt noch ab. */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public final int capacity;
    public final int genPerTick;
    public final int maxExtract;
    public final int burnTicksPerRod;
    /** Maximale Hitze; bei Erreichen explodiert der Reaktor. */
    public final int maxHeat;
    /** Hitzeaufbau pro Tick, solange ein Brennstab aktiv ist. */
    public final int heatPerTick;

    public NuclearReactorBlock(Properties settings, int capacity, int genPerTick,
                               int maxExtract, int burnTicksPerRod, int maxHeat, int heatPerTick) {
        super(settings);
        this.capacity = capacity;
        this.genPerTick = genPerTick;
        this.maxExtract = maxExtract;
        this.burnTicksPerRod = burnTicksPerRod;
        this.maxHeat = maxHeat;
        this.heatPerTick = heatPerTick;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, POWERED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof NuclearReactorBlockEntity be) {
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
        return new NuclearReactorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (w, pos, st, be) -> {
            if (be instanceof NuclearReactorBlockEntity reactor) {
                NuclearReactorBlockEntity.tick(w, pos, st, reactor);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof NuclearReactorBlockEntity reactor && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(reactor, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }

}
