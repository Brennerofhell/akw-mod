package ch.danielt.akw.datagen;

import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {

    public ModLootTableProvider(FabricDataOutput dataOutput,
                                CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        // Erze: Silk Touch → Block, sonst Roh-Uran (Fortune wirkt)
        addDrop(ModBlocks.URANIUM_ORE, oreDrops(ModBlocks.URANIUM_ORE, ModItems.RAW_URANIUM));
        addDrop(ModBlocks.DEEPSLATE_URANIUM_ORE,
                oreDrops(ModBlocks.DEEPSLATE_URANIUM_ORE, ModItems.RAW_URANIUM));

        // Reaktoren
        addDrop(ModBlocks.NUCLEAR_REACTOR);
        addDrop(ModBlocks.ADVANCED_NUCLEAR_REACTOR);
        addDrop(ModBlocks.ELITE_NUCLEAR_REACTOR);
        addDrop(ModBlocks.BREEDER_REACTOR);
        addDrop(ModBlocks.THORIUM_REACTOR);
        addDrop(ModBlocks.FUSION_REACTOR);

        // Bausteine
        addDrop(ModBlocks.REACTOR_CORE);
        addDrop(ModBlocks.CONTROL_ROD_BLOCK);
        addDrop(ModBlocks.COOLING_PIPE);
        addDrop(ModBlocks.LEAD_BLOCK);
        addDrop(ModBlocks.WASTE_CONTAINER);
        addDrop(ModBlocks.ENRICHED_URANIUM_BLOCK);

        // Energie-Infrastruktur
        addDrop(ModBlocks.ENERGY_CABLE);
        addDrop(ModBlocks.ENERGY_BATTERY);
    }
}
