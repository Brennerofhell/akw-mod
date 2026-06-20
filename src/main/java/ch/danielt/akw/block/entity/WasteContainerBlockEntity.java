package ch.danielt.akw.block.entity;

import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Abfallbehälter für Verbrauchte Brennstäbe. 9 Slots (1×9 Inventar, via
 * GenericContainerScreenHandler). Hopper von oben/Seiten liefern Stäbe,
 * Hopper von unten entnehmen. Komparator zeigt den Füllstand 0–15.
 */
public class WasteContainerBlockEntity extends BlockEntity
        implements ImplementedInventory, SidedInventory, NamedScreenHandlerFactory {

    public static final int SIZE = 9;
    private static final int RADIATION_RADIUS = 5;

    private static final int[] TOP_SIDE_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] BOTTOM_SLOTS   = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
    private int lastComparator = -1;

    public WasteContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WASTE_CONTAINER, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    /** Füllstand als Redstone-Stärke 0–15 (basierend auf Gesamtanzahl Items). */
    public int getComparatorLevel() {
        int total = 0;
        for (ItemStack stack : inventory) total += stack.getCount();
        if (total <= 0) return 0;
        return (int) Math.max(1, (long) total * 15 / (SIZE * 64));
    }

    public static void tick(World world, BlockPos pos, BlockState state, WasteContainerBlockEntity be) {
        if (world.isClient()) return;

        int level = be.getComparatorLevel();
        if (level != be.lastComparator) {
            be.lastComparator = level;
            world.updateComparators(pos, state.getBlock());
        }

        // Passive Strahlung bei Füllstand > 50 % (level >= 8)
        if (level >= 8 && world instanceof ServerWorld serverWorld) {
            Vec3d center = Vec3d.ofCenter(pos);
            Box box = new Box(pos).expand(RADIATION_RADIUS);
            serverWorld.getEntitiesByClass(PlayerEntity.class, box,
                    p -> p.squaredDistanceTo(center) <= (double) RADIATION_RADIUS * RADIATION_RADIUS)
                    .forEach(player -> {
                        if (!NuclearReactorBlockEntity.hasLeadShielding(world, pos, player.getBlockPos())) {
                            player.addStatusEffect(new StatusEffectInstance(
                                    ModEffects.RADIATION, 60, 0, false, true));
                        }
                    });
        }
    }

    // --- SidedInventory ---

    @Override
    public int[] getAvailableSlots(Direction side) {
        return side == Direction.DOWN ? BOTTOM_SLOTS : TOP_SIDE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.isOf(ModItems.SPENT_FUEL_ROD);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN;
    }

    // --- NamedScreenHandlerFactory ---

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, this, 1);
    }

    // --- Persistenz ---

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, inventory);
        lastComparator = getComparatorLevel();
    }
}
