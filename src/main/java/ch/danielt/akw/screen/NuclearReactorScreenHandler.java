package ch.danielt.akw.screen;

import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import ch.danielt.akw.reactor.ComparatorMode;
import ch.danielt.akw.reactor.RedstoneMode;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.registry.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class NuclearReactorScreenHandler extends AbstractContainerMenu {

    private final Container inventory;
    private final ContainerData propertyDelegate;

    /** Client-Konstruktor für NUCLEAR_REACTOR (aufgerufen vom IMenuTypeExtension). */
    public NuclearReactorScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, resolveInventory(playerInventory, pos), resolveDelegate(playerInventory, pos));
    }

    public NuclearReactorScreenHandler(int syncId, Inventory playerInventory,
                                       Container inventory, ContainerData propertyDelegate) {
        this(ModScreenHandlers.NUCLEAR_REACTOR.get(), syncId, playerInventory, inventory, propertyDelegate);
    }

    /** Geschützter Konstruktor für Unterklassen (anderer MenuType). */
    protected NuclearReactorScreenHandler(MenuType<? extends NuclearReactorScreenHandler> type,
                                          int syncId, Inventory playerInventory,
                                          Container inventory, ContainerData propertyDelegate) {
        super(type, syncId);
        checkContainerSize(inventory, 2);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;

        // Brennstoff-Slot (Slot 0)
        this.addSlot(new Slot(inventory, NuclearReactorBlockEntity.FUEL_SLOT, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.FUEL_ROD);
            }
        });

        // Abfall-Slot (Slot 1, Output-Only)
        this.addSlot(new Slot(inventory, NuclearReactorBlockEntity.WASTE_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
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

        this.addDataSlots(propertyDelegate);
    }

    private static Container resolveInventory(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof NuclearReactorBlockEntity be) {
            return be;
        }
        return new SimpleContainer(2);
    }

    private static ContainerData resolveDelegate(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof NuclearReactorBlockEntity be) {
            return be.getContainerData();
        }
        return new SimpleContainerData(NuclearReactorBlockEntity.PROPERTY_COUNT);
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

    public RedstoneMode getRedstoneMode() {
        int ord = propertyDelegate.get(NuclearReactorBlockEntity.IDX_REDSTONE_MODE);
        return ord >= 0 && ord < RedstoneMode.values().length
                ? RedstoneMode.values()[ord] : RedstoneMode.HIGH_DISABLES;
    }

    public ComparatorMode getComparatorMode() {
        int ord = propertyDelegate.get(NuclearReactorBlockEntity.IDX_COMPARATOR_MODE);
        return ord >= 0 && ord < ComparatorMode.values().length
                ? ComparatorMode.values()[ord] : ComparatorMode.ENERGY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            int next = (propertyDelegate.get(NuclearReactorBlockEntity.IDX_REDSTONE_MODE) + 1)
                    % RedstoneMode.values().length;
            propertyDelegate.set(NuclearReactorBlockEntity.IDX_REDSTONE_MODE, next);
            return true;
        }
        if (id == 1) {
            int next = (propertyDelegate.get(NuclearReactorBlockEntity.IDX_COMPARATOR_MODE) + 1)
                    % ComparatorMode.values().length;
            propertyDelegate.set(NuclearReactorBlockEntity.IDX_COMPARATOR_MODE, next);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            newStack = original.copy();
            // Slots 0-1 = Block-Inventar (Brennstoff, Abfall); ab Slot 2 = Spieler
            if (slotIndex < 2) {
                if (!this.moveItemStackTo(original, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Brennstab → Brennstoff-Slot; sonst kein Ziel
                if (!this.moveItemStackTo(original, NuclearReactorBlockEntity.FUEL_SLOT, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (original.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }
}
