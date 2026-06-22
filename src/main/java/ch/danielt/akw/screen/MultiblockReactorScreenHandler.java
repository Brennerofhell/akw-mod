package ch.danielt.akw.screen;

import ch.danielt.akw.block.entity.MultiblockReactorControllerBlockEntity;
import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import ch.danielt.akw.registry.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

/** ScreenHandler für den Multiblock-Reaktor-Controller. Teilt GUI-Logik mit dem Single-Block-Reaktor. */
public class MultiblockReactorScreenHandler extends NuclearReactorScreenHandler {

    /** Client-Konstruktor (aufgerufen vom IMenuTypeExtension mit der BlockPos). */
    public MultiblockReactorScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, resolveInventory(playerInventory, pos), resolveDelegate(playerInventory, pos));
    }

    /** Server-Konstruktor (direkte BE-Referenz). */
    public MultiblockReactorScreenHandler(int syncId, Inventory playerInventory,
                                          Container inventory, ContainerData propertyDelegate) {
        super(ModScreenHandlers.MULTIBLOCK_REACTOR.get(), syncId, playerInventory, inventory, propertyDelegate);
    }

    private static Container resolveInventory(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos)
                instanceof MultiblockReactorControllerBlockEntity be) {
            return be;
        }
        return new SimpleContainer(2);
    }

    private static ContainerData resolveDelegate(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos)
                instanceof MultiblockReactorControllerBlockEntity be) {
            return be.getPropertyDelegate();
        }
        return new SimpleContainerData(NuclearReactorBlockEntity.PROPERTY_COUNT);
    }
}
