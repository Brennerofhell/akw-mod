package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.block.entity.EnergyBatteryBlockEntity;
import ch.danielt.akw.block.entity.EnergyCableBlockEntity;
import ch.danielt.akw.block.entity.NuclearReactorBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    /** Ein gemeinsamer BlockEntity-Typ fuer alle Reaktor-Bloecke. */
    public static BlockEntityType<NuclearReactorBlockEntity> NUCLEAR_REACTOR;
    public static BlockEntityType<EnergyCableBlockEntity> ENERGY_CABLE;
    public static BlockEntityType<EnergyBatteryBlockEntity> ENERGY_BATTERY;

    public static void registerAll() {
        NUCLEAR_REACTOR = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(AkwMod.MOD_ID, "nuclear_reactor"),
                FabricBlockEntityTypeBuilder.create(
                        NuclearReactorBlockEntity::new,
                        ModBlocks.REACTORS.toArray(new Block[0])).build());

        ENERGY_CABLE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(AkwMod.MOD_ID, "energy_cable"),
                FabricBlockEntityTypeBuilder.create(
                        EnergyCableBlockEntity::new, ModBlocks.ENERGY_CABLE).build());

        ENERGY_BATTERY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(AkwMod.MOD_ID, "energy_battery"),
                FabricBlockEntityTypeBuilder.create(
                        EnergyBatteryBlockEntity::new, ModBlocks.ENERGY_BATTERY).build());

        AkwMod.LOGGER.info("[Atomkraftwerk] BlockEntities registriert.");
    }
}
