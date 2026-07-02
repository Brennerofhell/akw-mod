package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.MultiblockReactorControllerBlock;
import ch.danielt.akw.block.ReactorItemPortBlock;
import ch.danielt.akw.reactor.ItemPortMode;
import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * BlockEntity des Item-Ports. Hält kein eigenes Inventar, sondern delegiert alle
 * Container-Zugriffe an die verlinkte Controller-BE: je nach {@link ItemPortMode}
 * ist nur der Brennstoff-Slot (Eingang) oder der Abfall-Slot (Ausgang) erreichbar.
 * Unverlinkt verhält sich der Port als leerer, gesperrter Container.
 */
public class ReactorItemPortBlockEntity extends BlockEntity implements WorldlyContainer {

    private static final int[] FUEL_SLOTS = {MultiblockReactorControllerBlockEntity.FUEL_SLOT};
    private static final int[] WASTE_SLOTS = {MultiblockReactorControllerBlockEntity.WASTE_SLOT};
    private static final int[] NO_SLOTS = {};
    private static final long NO_CONTROLLER = Long.MIN_VALUE;

    @Nullable
    private BlockPos controllerPos;

    public ReactorItemPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_ITEM_PORT.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // Kein Inventar-Drop: die Stacks gehören dem Controller, nicht dem Port.
        // (Das Default-Verhalten würde den delegierten Controller-Inhalt droppen.)
    }

    /** Setzt oder löscht die Controller-Verlinkung (nur der Controller ruft das auf). */
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

    /** Verlinkte, assemblierte Controller-BE — sonst {@code null}. */
    @Nullable
    private MultiblockReactorControllerBlockEntity resolveController() {
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

    private ItemPortMode mode() {
        return getBlockState().getValue(ReactorItemPortBlock.MODE);
    }

    // --- WorldlyContainer: Slot-Freigabe nach Modus ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (resolveController() == null) {
            return NO_SLOTS;
        }
        return switch (mode()) {
            case FUEL_INPUT -> FUEL_SLOTS;
            case WASTE_OUTPUT -> WASTE_SLOTS;
            case DISABLED -> NO_SLOTS;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return mode() == ItemPortMode.FUEL_INPUT
                && slot == MultiblockReactorControllerBlockEntity.FUEL_SLOT
                && stack.is(ModItems.FUEL_ROD)
                && resolveController() != null;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return mode() == ItemPortMode.WASTE_OUTPUT
                && slot == MultiblockReactorControllerBlockEntity.WASTE_SLOT
                && resolveController() != null;
    }

    // --- Container: vollständige Delegation an den Controller ---

    @Override
    public int getContainerSize() {
        MultiblockReactorControllerBlockEntity controller = resolveController();
        return controller == null ? 0 : controller.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        MultiblockReactorControllerBlockEntity controller = resolveController();
        return controller == null || controller.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        MultiblockReactorControllerBlockEntity controller = resolveController();
        return controller == null ? ItemStack.EMPTY : controller.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        MultiblockReactorControllerBlockEntity controller = resolveController();
        return controller == null ? ItemStack.EMPTY : controller.removeItem(slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        MultiblockReactorControllerBlockEntity controller = resolveController();
        return controller == null ? ItemStack.EMPTY : controller.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        MultiblockReactorControllerBlockEntity controller = resolveController();
        if (controller != null) {
            controller.setItem(slot, stack);
        }
    }

    @Override
    public void clearContent() {
        // Ports besitzen kein eigenes Inventar; den Controller-Inhalt nicht anfassen.
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) <= 64.0;
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
