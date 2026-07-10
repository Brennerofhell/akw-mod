package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.MultiblockReactorControllerBlock;
import ch.danielt.akw.block.ReactorRedstonePortBlock;
import ch.danielt.akw.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ReactorRedstonePortBlockEntity extends BlockEntity {

    private static final long NO_CONTROLLER = Long.MIN_VALUE;

    @Nullable
    private BlockPos controllerPos;

    public ReactorRedstonePortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_REDSTONE_PORT.get(), pos, state);
    }

    public void setController(@Nullable BlockPos pos) {
        BlockPos newPos = pos == null ? null : pos.immutable();
        if (Objects.equals(controllerPos, newPos)) {
            return;
        }
        controllerPos = newPos;
        setChanged();
    }

    @Nullable
    public MultiblockReactorControllerBlockEntity resolveController() {
        if (level == null || controllerPos == null) {
            return null;
        }
        if (!(level.getBlockEntity(controllerPos)
                instanceof MultiblockReactorControllerBlockEntity controller)) {
            return null;
        }
        if (!controller.getBlockState().getValue(MultiblockReactorControllerBlock.ASSEMBLED)) {
            return null;
        }
        return controller;
    }

    public int getOutputSignal() {
        MultiblockReactorControllerBlockEntity controller = resolveController();
        if (controller != null) {
            return controller.getComparatorLevel();
        }
        return 0;
    }

    public void notifyControllerOfSignalChange() {
        MultiblockReactorControllerBlockEntity controller = resolveController();
        if (controller != null && level != null) {
            // Signaländerung am Input-Port an den Controller melden
            BlockState controllerState = controller.getBlockState();
            level.neighborChanged(controllerPos, controllerState.getBlock(), (net.minecraft.world.level.redstone.Orientation) null);
        }
    }

    public void updateOutputState(int signal) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(ReactorRedstonePortBlock.MODE) && state.getValue(ReactorRedstonePortBlock.MODE) == ch.danielt.akw.reactor.RedstonePortMode.OUTPUT) {
            boolean isPowered = signal > 0;
            if (state.getValue(ReactorRedstonePortBlock.POWERED) != isPowered) {
                level.setBlock(worldPosition, state.setValue(ReactorRedstonePortBlock.POWERED, isPowered), Block.UPDATE_ALL);
            }
            level.updateNeighborsAt(worldPosition, state.getBlock());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("Controller", controllerPos == null ? NO_CONTROLLER : controllerPos.asLong());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long packed = input.getLongOr("Controller", NO_CONTROLLER);
        controllerPos = packed == NO_CONTROLLER ? null : BlockPos.of(packed);
    }
}
