package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
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

    public static void registerAll() {
        NUCLEAR_REACTOR = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(AkwMod.MOD_ID, "nuclear_reactor"),
                FabricBlockEntityTypeBuilder.create(
                        NuclearReactorBlockEntity::new,
                        ModBlocks.REACTORS.toArray(new Block[0])).build());
        AkwMod.LOGGER.info("[Atomkraftwerk] BlockEntities registriert.");
    }
}
