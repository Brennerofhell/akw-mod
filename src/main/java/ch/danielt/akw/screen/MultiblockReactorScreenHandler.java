package ch.danielt.akw.screen;

import ch.danielt.akw.block.entity.MultiblockReactorControllerBlockEntity;
import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import ch.danielt.akw.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.math.BlockPos;

/** ScreenHandler für den Multiblock-Reaktor-Controller. Teilt GUI-Logik mit dem Single-Block-Reaktor. */
public class MultiblockReactorScreenHandler extends NuclearReactorScreenHandler {

    /** Client-Konstruktor (aufgerufen vom ExtendedScreenHandlerType mit der BlockPos). */
    public MultiblockReactorScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, resolveInventory(playerInventory, pos), resolveDelegate(playerInventory, pos));
    }

    /** Server-Konstruktor (direkte BE-Referenz). */
    public MultiblockReactorScreenHandler(int syncId, PlayerInventory playerInventory,
                                          Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.MULTIBLOCK_REACTOR, syncId, playerInventory, inventory, propertyDelegate);
        inventory.onOpen(playerInventory.player);
    }

    private static Inventory resolveInventory(PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player.getEntityWorld().getBlockEntity(pos)
                instanceof MultiblockReactorControllerBlockEntity be) {
            return be;
        }
        return new SimpleInventory(1);
    }

    private static PropertyDelegate resolveDelegate(PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player.getEntityWorld().getBlockEntity(pos)
                instanceof MultiblockReactorControllerBlockEntity be) {
            return be.getPropertyDelegate();
        }
        return new ArrayPropertyDelegate(NuclearReactorBlockEntity.PROPERTY_COUNT);
    }
}
