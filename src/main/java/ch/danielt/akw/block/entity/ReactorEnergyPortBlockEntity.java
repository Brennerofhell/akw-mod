package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.MultiblockReactorControllerBlock;
import ch.danielt.akw.energy.EnergyNet;
import ch.danielt.akw.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * BlockEntity des Energie-Ports. Hält keinen eigenen Speicher, sondern nur die
 * Position des Controllers; Capability-Zugriffe und der aktive Push laufen
 * direkt gegen dessen {@link MutableEnergyStorage}. Verlinkt/entlinkt wird
 * ausschließlich vom Controller (Assemble/Revalidierung/Disassemble).
 */
public class ReactorEnergyPortBlockEntity extends BlockEntity {

    /** Sentinel für „kein Controller" im NBT (kein gültiges BlockPos-Packing in Weltgrenzen). */
    private static final long NO_CONTROLLER = Long.MIN_VALUE;

    @Nullable
    private BlockPos controllerPos;

    public ReactorEnergyPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_ENERGY_PORT.get(), pos, state);
    }

    /** Setzt oder löscht die Controller-Verlinkung und invalidiert den Capability-Cache. */
    public void setController(@Nullable BlockPos pos) {
        BlockPos newPos = pos == null ? null : pos.immutable();
        if (Objects.equals(controllerPos, newPos)) {
            return;
        }
        controllerPos = newPos;
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    /** Energiespeicher des verlinkten, assemblierten Controllers — sonst {@code null}. */
    @Nullable
    public MutableEnergyStorage resolveControllerEnergy() {
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
        return controller.energyStorage;
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            ReactorEnergyPortBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        MutableEnergyStorage storage = be.resolveControllerEnergy();
        if (storage != null && storage.getEnergyStored() > 0) {
            EnergyNet.pushToNeighbors(storage, level, pos, storage.getMaxExtract());
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
