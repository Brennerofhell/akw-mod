package ch.danielt.akw.screen;

import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class NuclearReactorScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    /** Client-Konstruktor für NUCLEAR_REACTOR (aufgerufen vom ExtendedScreenHandlerType). */
    public NuclearReactorScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, resolveInventory(playerInventory, pos), resolveDelegate(playerInventory, pos));
    }

    public NuclearReactorScreenHandler(int syncId, PlayerInventory playerInventory,
                                       Inventory inventory, PropertyDelegate propertyDelegate) {
        this(ModScreenHandlers.NUCLEAR_REACTOR, syncId, playerInventory, inventory, propertyDelegate);
        inventory.onOpen(playerInventory.player);
    }

    /** Geschützter Konstruktor für Unterklassen (anderer ScreenHandlerType). */
    protected NuclearReactorScreenHandler(ScreenHandlerType<? extends NuclearReactorScreenHandler> type,
                                          int syncId, PlayerInventory playerInventory,
                                          Inventory inventory, PropertyDelegate propertyDelegate) {
        super(type, syncId);
        checkSize(inventory, 2);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;

        // Brennstoff-Slot (Slot 0)
        this.addSlot(new Slot(inventory, NuclearReactorBlockEntity.FUEL_SLOT, 80, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.FUEL_ROD);
            }
        });

        // Abfall-Slot (Slot 1, Output-Only)
        this.addSlot(new Slot(inventory, NuclearReactorBlockEntity.WASTE_SLOT, 116, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        // Spieler-Inventar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addProperties(propertyDelegate);
    }

    private static Inventory resolveInventory(PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player.getEntityWorld().getBlockEntity(pos) instanceof NuclearReactorBlockEntity be) {
            return be;
        }
        return new net.minecraft.inventory.SimpleInventory(2);
    }

    private static PropertyDelegate resolveDelegate(PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player.getEntityWorld().getBlockEntity(pos) instanceof NuclearReactorBlockEntity be) {
            return be.getPropertyDelegate();
        }
        return new ArrayPropertyDelegate(NuclearReactorBlockEntity.PROPERTY_COUNT);
    }

    public int getEnergy() {
        return propertyDelegate.get(NuclearReactorBlockEntity.IDX_ENERGY);
    }

    public int getCapacity() {
        return propertyDelegate.get(NuclearReactorBlockEntity.IDX_CAPACITY);
    }

    public boolean isBurning() {
        return propertyDelegate.get(NuclearReactorBlockEntity.IDX_BURN_TIME) > 0;
    }

    public float getEnergyFraction() {
        int cap = getCapacity();
        return cap == 0 ? 0f : (float) getEnergy() / cap;
    }

    public int getHeat() {
        return propertyDelegate.get(NuclearReactorBlockEntity.IDX_HEAT);
    }

    public int getMaxHeat() {
        return propertyDelegate.get(NuclearReactorBlockEntity.IDX_MAX_HEAT);
    }

    public float getHeatFraction() {
        int max = getMaxHeat();
        return max == 0 ? 0f : (float) getHeat() / max;
    }

    public float getBurnFraction() {
        int total = propertyDelegate.get(NuclearReactorBlockEntity.IDX_BURN_TOTAL);
        return total == 0 ? 0f
                : (float) propertyDelegate.get(NuclearReactorBlockEntity.IDX_BURN_TIME) / total;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack original = slot.getStack();
            newStack = original.copy();
            // Slots 0-1 = Block-Inventar (Brennstoff, Abfall); ab Slot 2 = Spieler
            if (slotIndex < 2) {
                if (!this.insertItem(original, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Brennstab → Brennstoff-Slot; sonst kein Ziel
                if (!this.insertItem(original, NuclearReactorBlockEntity.FUEL_SLOT, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (original.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}
