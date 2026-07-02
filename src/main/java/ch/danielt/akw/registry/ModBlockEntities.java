package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.block.entity.EnergyBatteryBlockEntity;
import ch.danielt.akw.block.entity.EnergyCableBlockEntity;
import ch.danielt.akw.block.entity.MultiblockReactorControllerBlockEntity;
import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import ch.danielt.akw.block.entity.ReactorBuilderControllerBlockEntity;
import ch.danielt.akw.block.entity.ReactorEnergyPortBlockEntity;
import ch.danielt.akw.block.entity.ReactorItemPortBlockEntity;
import ch.danielt.akw.block.entity.WasteContainerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AkwMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NuclearReactorBlockEntity>> NUCLEAR_REACTOR =
            BLOCK_ENTITIES.register("nuclear_reactor", () -> new BlockEntityType<>(
                    NuclearReactorBlockEntity::new,
                    ModBlocks.NUCLEAR_REACTOR.get(),
                    ModBlocks.ADVANCED_NUCLEAR_REACTOR.get(),
                    ModBlocks.ELITE_NUCLEAR_REACTOR.get(),
                    ModBlocks.BREEDER_REACTOR.get(),
                    ModBlocks.THORIUM_REACTOR.get(),
                    ModBlocks.FUSION_REACTOR.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockReactorControllerBlockEntity>> MULTIBLOCK_REACTOR_CONTROLLER =
            BLOCK_ENTITIES.register("multiblock_reactor_controller", () -> new BlockEntityType<>(
                    MultiblockReactorControllerBlockEntity::new,
                    ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorEnergyPortBlockEntity>> REACTOR_ENERGY_PORT =
            BLOCK_ENTITIES.register("reactor_energy_port", () -> new BlockEntityType<>(
                    ReactorEnergyPortBlockEntity::new,
                    ModBlocks.REACTOR_ENERGY_PORT.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorItemPortBlockEntity>> REACTOR_ITEM_PORT =
            BLOCK_ENTITIES.register("reactor_item_port", () -> new BlockEntityType<>(
                    ReactorItemPortBlockEntity::new,
                    ModBlocks.REACTOR_ITEM_PORT.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorBuilderControllerBlockEntity>> REACTOR_BUILDER_CONTROLLER =
            BLOCK_ENTITIES.register("reactor_builder_controller", () -> new BlockEntityType<>(
                    ReactorBuilderControllerBlockEntity::new,
                    ModBlocks.REACTOR_BUILDER_CONTROLLER.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyCableBlockEntity>> ENERGY_CABLE =
            BLOCK_ENTITIES.register("energy_cable", () -> new BlockEntityType<>(
                    EnergyCableBlockEntity::new,
                    ModBlocks.ENERGY_CABLE.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBatteryBlockEntity>> ENERGY_BATTERY =
            BLOCK_ENTITIES.register("energy_battery", () -> new BlockEntityType<>(
                    EnergyBatteryBlockEntity::new,
                    ModBlocks.ENERGY_BATTERY.get()
            ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WasteContainerBlockEntity>> WASTE_CONTAINER =
            BLOCK_ENTITIES.register("waste_container", () -> new BlockEntityType<>(
                    WasteContainerBlockEntity::new,
                    ModBlocks.WASTE_CONTAINER.get()
            ));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
        AkwMod.LOGGER.info("[Atomkraftwerk] BlockEntities registriert.");
    }
}
