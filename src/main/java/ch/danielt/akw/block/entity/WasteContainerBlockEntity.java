package ch.danielt.akw.block.entity;

import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Abfallbehaelter fuer Verbrauchte Brennstaebe. 9 Slots (1×9 Inventar, via
 * ChestMenu / GENERIC_9x1). Hopper von oben/Seiten liefern Staebe,
 * Hopper von unten entnehmen. Komparator zeigt den Fuellstand 0–15.
 */
public class WasteContainerBlockEntity extends BlockEntity
        implements ImplementedInventory, WorldlyContainer, MenuProvider {

    public static final int SIZE = 9;
    private static final int RADIATION_RADIUS = 5;

    private static final int[] TOP_SIDE_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] BOTTOM_SLOTS   = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int lastComparator = -1;

    public WasteContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WASTE_CONTAINER.get(), pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    /** Fuellstand als Redstone-Staerke 0–15 (basierend auf Gesamtanzahl Items). */
    public int getComparatorLevel() {
        int total = 0;
        for (ItemStack stack : inventory) total += stack.getCount();
        if (total <= 0) return 0;
        return (int) Math.max(1, (long) total * 15 / (SIZE * 64));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WasteContainerBlockEntity be) {
        if (level.isClientSide()) return;

        int lvl = be.getComparatorLevel();
        if (lvl != be.lastComparator) {
            be.lastComparator = lvl;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        // Partikel und Strahlung bei Fuellstand > 50 % (level >= 8)
        if (lvl >= 8 && level instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameTime() % 20 == 0) {
                serverLevel.sendParticles(ParticleTypes.GLOW_SQUID_INK,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        2, 0.3, 0.2, 0.3, 0.0);
            }
            Vec3 center = Vec3.atCenterOf(pos);
            AABB box = new AABB(pos).inflate(RADIATION_RADIUS);
            serverLevel.getEntitiesOfClass(Player.class, box,
                    p -> p.distanceToSqr(center) <= (double) RADIATION_RADIUS * RADIATION_RADIUS)
                    .forEach(player -> {
                        if (!NuclearReactorBlockEntity.hasLeadShielding(level, pos, player.blockPosition())) {
                            player.addEffect(new MobEffectInstance(
                                    ModEffects.RADIATION, 60, 0, false, true));
                        }
                    });
        }
    }

    // --- WorldlyContainer (Hopper-Kompatibilitaet) ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? BOTTOM_SLOTS : TOP_SIDE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.is(ModItems.SPENT_FUEL_ROD);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN;
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ChestMenu(MenuType.GENERIC_9x1, syncId, playerInventory, this, 1);
    }

    // --- Persistenz ---

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        lastComparator = getComparatorLevel();
    }
}
