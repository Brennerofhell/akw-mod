package ch.danielt.akw.block.entity;

import ch.danielt.akw.block.MultiblockReactorControllerBlock;
import ch.danielt.akw.block.ReactorBuilderControllerBlock;
import ch.danielt.akw.reactor.ReactorValidator;
import ch.danielt.akw.reactor.ValidationError;
import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Serverseitige Auftrags-, Material- und Energielogik des Bauroboters. */
public class ReactorBuilderControllerBlockEntity extends BlockEntity
        implements ImplementedInventory, WorldlyContainer, MenuProvider {

    public static final int SIZE = 27;
    public static final int CAPACITY = 1_000_000;
    public static final int MAX_INSERT = 4_096;
    public static final int ENERGY_PER_BLOCK = 500;
    private static final int BUILD_INTERVAL = 5;
    private static final int[] SLOTS = createSlots();

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int buildIndex;
    private int buildCooldown;
    private boolean building;
    private String statusKey = "akw.builder.status.idle";
    private UUID robotUuid;

    public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(CAPACITY, MAX_INSERT, 0);

    public ReactorBuilderControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_BUILDER_CONTROLLER.get(), pos, state);
        energyStorage.setOnChange(this::setChanged);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            ReactorBuilderControllerBlockEntity be) {
        if (level.isClientSide() || !be.building || state.getValue(ReactorBuilderControllerBlock.POWERED)) {
            return;
        }
        be.ensureRobot(level, pos);
        if (be.buildCooldown++ < BUILD_INTERVAL - 1) {
            return;
        }
        be.buildCooldown = 0;
        be.buildNext(level, pos, state);
    }

    public Component toggleBuilding(Level level, BlockPos pos, BlockState state) {
        if (building) {
            setBuilding(level, pos, state, false, "akw.builder.status.paused");
            return Component.translatable("akw.builder.paused", buildIndex, createPlan(pos, state).size());
        }

        List<BuildStep> plan = createPlan(pos, state);
        if (buildIndex >= plan.size()) {
            buildIndex = 0;
        }
        Component problem = validateStart(level, plan);
        if (problem != null) {
            return problem;
        }
        setBuilding(level, pos, state, true, "akw.builder.status.building");
        ensureRobot(level, pos);
        return Component.translatable("akw.builder.started", plan.size() - buildIndex);
    }

    private Component validateStart(Level level, List<BuildStep> plan) {
        int casingNeeded = 0;
        int coreNeeded = 0;
        int controllerNeeded = 0;

        for (int i = buildIndex; i < plan.size(); i++) {
            BuildStep step = plan.get(i);
            BlockState current = level.getBlockState(step.pos());
            if (current.is(step.state().getBlock())) {
                continue;
            }
            if (!current.isAir()) {
                statusKey = "akw.builder.status.blocked";
                return Component.translatable("akw.builder.blocked",
                        step.pos().getX(), step.pos().getY(), step.pos().getZ());
            }
            if (step.state().is(ModBlocks.REACTOR_CASING.get())) {
                casingNeeded++;
            } else if (step.state().is(ModBlocks.REACTOR_CORE.get())) {
                coreNeeded++;
            } else if (step.state().is(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get())) {
                controllerNeeded++;
            }
        }

        int missingCasing = Math.max(0, casingNeeded - countMaterial(ModBlocks.REACTOR_CASING.get().asItem()));
        int missingCore = Math.max(0, coreNeeded - countMaterial(ModBlocks.REACTOR_CORE.get().asItem()));
        int missingController = Math.max(0,
                controllerNeeded - countMaterial(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get().asItem()));
        if (missingCasing + missingCore + missingController > 0) {
            statusKey = "akw.builder.status.materials";
            return Component.translatable("akw.builder.missing_materials",
                    missingCasing, missingCore, missingController);
        }

        int requiredEnergy = (casingNeeded + coreNeeded + controllerNeeded) * ENERGY_PER_BLOCK;
        if (energyStorage.getEnergyStored() < requiredEnergy) {
            statusKey = "akw.builder.status.energy";
            return Component.translatable("akw.builder.missing_energy",
                    requiredEnergy - energyStorage.getEnergyStored());
        }
        return null;
    }

    private void buildNext(Level level, BlockPos pos, BlockState state) {
        List<BuildStep> plan = createPlan(pos, state);
        while (buildIndex < plan.size()) {
            BuildStep step = plan.get(buildIndex);
            BlockState current = level.getBlockState(step.pos());
            if (current.is(step.state().getBlock())) {
                buildIndex++;
                continue;
            }
            if (!current.isAir()) {
                setBuilding(level, pos, state, false, "akw.builder.status.blocked");
                return;
            }
            int slot = findItem(step.state().getBlock().asItem());
            if (slot < 0) {
                setBuilding(level, pos, state, false, "akw.builder.status.materials");
                return;
            }
            if (energyStorage.getEnergyStored() < ENERGY_PER_BLOCK) {
                setBuilding(level, pos, state, false, "akw.builder.status.energy");
                return;
            }

            inventory.get(slot).shrink(1);
            energyStorage.setEnergy(energyStorage.getEnergyStored() - ENERGY_PER_BLOCK);
            moveRobot(level, step.pos());
            level.setBlock(step.pos(), step.state(), Block.UPDATE_ALL);
            buildIndex++;
            setChanged();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
            break;
        }

        if (buildIndex >= plan.size()) {
            finish(level, pos, state);
        }
    }

    private void finish(Level level, BlockPos pos, BlockState state) {
        Direction builderFacing = state.getValue(ReactorBuilderControllerBlock.FACING);
        BlockPos controllerPos = pos.relative(builderFacing, 2);
        ReactorValidator.Result result = ReactorValidator.find(level, controllerPos);
        // Der Roboter baut nur die Casing-Hülle; ein fehlender Energie-Port
        // gilt daher nicht als Baufehler (der Spieler rüstet ihn nach).
        boolean complete = result.errors().stream().noneMatch(error ->
                error.type().blocksAssembly()
                        && error.type() != ValidationError.Type.NO_ENERGY_PORT);
        setBuilding(level, pos, state, false,
                complete ? "akw.builder.status.complete" : "akw.builder.status.invalid");
        moveRobot(level, pos.above());
    }

    private void setBuilding(Level level, BlockPos pos, BlockState state,
                             boolean value, String newStatusKey) {
        building = value;
        statusKey = newStatusKey;
        buildCooldown = 0;
        if (state.getValue(ReactorBuilderControllerBlock.ACTIVE) != value) {
            level.setBlock(pos, state.setValue(ReactorBuilderControllerBlock.ACTIVE, value),
                    Block.UPDATE_ALL);
        }
        setChanged();
    }

    private List<BuildStep> createPlan(BlockPos builderPos, BlockState builderState) {
        Direction builderFacing = builderState.getValue(ReactorBuilderControllerBlock.FACING);
        Direction reactorFacing = builderFacing.getOpposite();
        Direction right = reactorFacing.getClockWise();
        BlockPos controllerPos = builderPos.relative(builderFacing, 2);
        List<BuildStep> shell = new ArrayList<>(25);
        BuildStep core = null;
        BuildStep controller = null;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz < 3; dz++) {
                    BlockPos target = controllerPos.relative(right, dx)
                            .relative(Direction.UP, dy).relative(builderFacing, dz);
                    boolean controllerPosition = dx == 0 && dy == 0 && dz == 0;
                    boolean shellPosition = Math.abs(dx) == 1 || Math.abs(dy) == 1
                            || dz == 0 || dz == 2;
                    if (controllerPosition) {
                        BlockState controllerState = ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get()
                                .defaultBlockState().setValue(MultiblockReactorControllerBlock.FACING, reactorFacing);
                        controller = new BuildStep(target, controllerState);
                    } else if (shellPosition) {
                        shell.add(new BuildStep(target, ModBlocks.REACTOR_CASING.get().defaultBlockState()));
                    } else {
                        core = new BuildStep(target, ModBlocks.REACTOR_CORE.get().defaultBlockState());
                    }
                }
            }
        }
        List<BuildStep> plan = new ArrayList<>(27);
        plan.addAll(shell);
        plan.add(core);
        plan.add(controller);
        return plan;
    }

    private int countMaterial(Item item) {
        int count = 0;
        for (ItemStack stack : inventory) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int findItem(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    private void ensureRobot(Level level, BlockPos controllerPos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (robotUuid != null && serverLevel.getEntity(robotUuid) instanceof ArmorStand) {
            return;
        }

        ArmorStand robot = new ArmorStand(serverLevel,
                controllerPos.getX() + 0.5, controllerPos.getY() + 1.0,
                controllerPos.getZ() + 0.5);
        robot.setNoGravity(true);
        robot.setInvulnerable(true);
        robot.setShowArms(true);
        robot.setNoBasePlate(true);
        robot.setCustomName(Component.translatable("akw.builder.robot"));
        robot.setCustomNameVisible(true);
        robot.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModBlocks.REACTOR_BUILDER_CONTROLLER));
        robot.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.REACTOR_WRENCH.get()));
        if (serverLevel.addFreshEntity(robot)) {
            robotUuid = robot.getUUID();
            setChanged();
        }
    }

    private void moveRobot(Level level, BlockPos target) {
        if (level instanceof ServerLevel serverLevel && robotUuid != null
                && serverLevel.getEntity(robotUuid) instanceof ArmorStand robot) {
            robot.teleportTo(target.getX() + 0.5, target.getY() + 0.25, target.getZ() + 0.5);
        }
    }

    public void releaseRobot(Level level) {
        if (level instanceof ServerLevel serverLevel && robotUuid != null
                && serverLevel.getEntity(robotUuid) instanceof ArmorStand robot) {
            robot.discard();
        }
        robotUuid = null;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // Bau-Roboter freigeben, bevor die BlockEntity entfernt wird; super droppt den Inhalt.
        releaseRobot(this.level);
        super.preRemoveSideEffects(pos, state);
    }

    public int getComparatorLevel() {
        int total = createPlan(getBlockPos(), getBlockState()).size();
        if (buildIndex <= 0) {
            return 0;
        }
        return Math.min(15, Math.max(1, buildIndex * 15 / total));
    }

    // --- WorldlyContainer (Hopper-Kompatibilitaet) ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.is(ModBlocks.REACTOR_CASING.get().asItem())
                || stack.is(ModBlocks.REACTOR_CORE.get().asItem())
                || stack.is(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get().asItem());
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return !building;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ChestMenu(MenuType.GENERIC_9x3, syncId, playerInventory, this, 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("Energy", energyStorage.getEnergyStored());
        output.putInt("BuildIndex", buildIndex);
        output.putInt("BuildCooldown", buildCooldown);
        output.putBoolean("Building", building);
        output.putString("StatusKey", statusKey);
        if (robotUuid != null) {
            output.putLong("RobotUuidMost", robotUuid.getMostSignificantBits());
            output.putLong("RobotUuidLeast", robotUuid.getLeastSignificantBits());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        energyStorage.setEnergy(Math.clamp(input.getIntOr("Energy", 0), 0, CAPACITY));
        buildIndex = Math.clamp(input.getIntOr("BuildIndex", 0), 0, 27);
        buildCooldown = Math.max(0, input.getIntOr("BuildCooldown", 0));
        building = input.getBooleanOr("Building", false);
        statusKey = input.getStringOr("StatusKey", "akw.builder.status.idle");
        long robotMost = input.getLongOr("RobotUuidMost", 0L);
        long robotLeast = input.getLongOr("RobotUuidLeast", 0L);
        robotUuid = robotMost == 0L && robotLeast == 0L
                ? null : new UUID(robotMost, robotLeast);
    }

    private static int[] createSlots() {
        int[] slots = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            slots[i] = i;
        }
        return slots;
    }

    private record BuildStep(BlockPos pos, BlockState state) {
    }
}
