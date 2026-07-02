package ch.danielt.akw.screen;

import ch.danielt.akw.block.entity.MultiblockReactorControllerBlockEntity;
import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import ch.danielt.akw.reactor.ComparatorMode;
import ch.danielt.akw.reactor.ReactorStatus;
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
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menü des Multiblock-Reaktor-Controllers (Tab-GUI, 176×222): zwei Slots,
 * erweiterte ContainerData (18 Properties) und Button-Protokoll für
 * Modi, Ein/Aus, Abschalttemperatur und Steuerstab-Regler.
 */
public class ModularReactorScreenHandler extends AbstractContainerMenu {

    public static final int BUTTON_REDSTONE = 0;
    public static final int BUTTON_COMPARATOR = 1;
    public static final int BUTTON_ENABLED = 2;
    public static final int BUTTON_SHUTDOWN_TEMP = 3;
    public static final int BUTTON_SAFETY = 4;
    /** Button-IDs {@code BUTTON_ROD_BASE + n} setzen den Steuerstabwert auf n % (0–100). */
    public static final int BUTTON_ROD_BASE = 100;
    private static final int SHUTDOWN_TEMP_STEP = 5;

    private final Container inventory;
    private final ContainerData propertyDelegate;
    private final BlockPos pos;

    /** Client-Konstruktor (aufgerufen vom IMenuTypeExtension mit der BlockPos). */
    public ModularReactorScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory,
                resolveInventory(playerInventory, pos), resolveDelegate(playerInventory, pos), pos);
    }

    /** Server-Konstruktor (direkte BE-Referenz). */
    public ModularReactorScreenHandler(int syncId, Inventory playerInventory,
                                       Container inventory, ContainerData propertyDelegate,
                                       BlockPos pos) {
        super(ModScreenHandlers.MULTIBLOCK_REACTOR.get(), syncId);
        checkContainerSize(inventory, 2);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.pos = pos;

        // Brennstoff-Slot (Slot 0)
        this.addSlot(new Slot(inventory, MultiblockReactorControllerBlockEntity.FUEL_SLOT, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.FUEL_ROD);
            }
        });

        // Abfall-Slot (Slot 1, Output-Only)
        this.addSlot(new Slot(inventory, MultiblockReactorControllerBlockEntity.WASTE_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Spieler-Inventar (GUI-Höhe 222 → Inventar ab y=140, Hotbar y=198)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }

        this.addDataSlots(propertyDelegate);
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
        return new SimpleContainerData(MultiblockReactorControllerBlockEntity.MB_PROPERTY_COUNT);
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    // --- Property-Getter ---

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

    public int getCoreCount() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_CORE_COUNT);
    }

    public int getProductionPerTick() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_PRODUCTION);
    }

    public int getCoolingPerTick() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_COOLING);
    }

    public int getControlRodInsertion() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_CONTROL_ROD);
    }

    public boolean isEnabled() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_ENABLED) != 0;
    }

    public int getShutdownTempPercent() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_SHUTDOWN_TEMP);
    }

    public int getErrorCount() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_ERROR_COUNT);
    }

    public int getSizeX() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_SIZE_X);
    }

    public int getSizeY() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_SIZE_Y);
    }

    public int getSizeZ() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_SIZE_Z);
    }

    public boolean isAssembled() {
        return getSizeX() >= 3;
    }

    public ReactorStatus getStatus() {
        return ReactorStatus.byOrdinal(
                propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_STATUS),
                ReactorStatus.UNASSEMBLED);
    }

    public boolean isSafetyOverride() {
        return propertyDelegate.get(MultiblockReactorControllerBlockEntity.IDX_SAFETY) != 0;
    }

    // --- Buttons (laufen serverseitig) ---

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_REDSTONE) {
            int next = (propertyDelegate.get(NuclearReactorBlockEntity.IDX_REDSTONE_MODE) + 1)
                    % RedstoneMode.values().length;
            propertyDelegate.set(NuclearReactorBlockEntity.IDX_REDSTONE_MODE, next);
            return true;
        }
        if (id == BUTTON_COMPARATOR) {
            int next = (propertyDelegate.get(NuclearReactorBlockEntity.IDX_COMPARATOR_MODE) + 1)
                    % ComparatorMode.values().length;
            propertyDelegate.set(NuclearReactorBlockEntity.IDX_COMPARATOR_MODE, next);
            return true;
        }
        if (id == BUTTON_ENABLED) {
            propertyDelegate.set(MultiblockReactorControllerBlockEntity.IDX_ENABLED,
                    isEnabled() ? 0 : 1);
            return true;
        }
        if (id == BUTTON_SHUTDOWN_TEMP) {
            int next = getShutdownTempPercent() + SHUTDOWN_TEMP_STEP;
            if (next > MultiblockReactorControllerBlockEntity.MAX_SHUTDOWN_TEMP) {
                next = MultiblockReactorControllerBlockEntity.MIN_SHUTDOWN_TEMP;
            }
            propertyDelegate.set(MultiblockReactorControllerBlockEntity.IDX_SHUTDOWN_TEMP, next);
            return true;
        }
        if (id == BUTTON_SAFETY) {
            propertyDelegate.set(MultiblockReactorControllerBlockEntity.IDX_SAFETY,
                    isSafetyOverride() ? 0 : 1);
            return true;
        }
        if (id >= BUTTON_ROD_BASE && id <= BUTTON_ROD_BASE + 100) {
            propertyDelegate.set(MultiblockReactorControllerBlockEntity.IDX_CONTROL_ROD,
                    id - BUTTON_ROD_BASE);
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
                if (!this.moveItemStackTo(original,
                        MultiblockReactorControllerBlockEntity.FUEL_SLOT, 1, false)) {
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
