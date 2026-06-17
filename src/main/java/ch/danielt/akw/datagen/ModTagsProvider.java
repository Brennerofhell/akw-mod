package ch.danielt.akw.datagen;

import ch.danielt.akw.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModTagsProvider extends FabricTagProvider<Block> {

    public ModTagsProvider(FabricDataOutput output,
                           CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BLOCK, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        // Alle AKW-Blöcke sind mit der Spitzhacke abbaubar
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.URANIUM_ORE)
                .add(ModBlocks.DEEPSLATE_URANIUM_ORE)
                .add(ModBlocks.NUCLEAR_REACTOR)
                .add(ModBlocks.ADVANCED_NUCLEAR_REACTOR)
                .add(ModBlocks.ELITE_NUCLEAR_REACTOR)
                .add(ModBlocks.BREEDER_REACTOR)
                .add(ModBlocks.THORIUM_REACTOR)
                .add(ModBlocks.FUSION_REACTOR)
                .add(ModBlocks.REACTOR_CORE)
                .add(ModBlocks.CONTROL_ROD_BLOCK)
                .add(ModBlocks.COOLING_PIPE)
                .add(ModBlocks.LEAD_BLOCK)
                .add(ModBlocks.WASTE_CONTAINER)
                .add(ModBlocks.ENRICHED_URANIUM_BLOCK);

        // Erze erfordern mindestens eine Eisen-Spitzhacke
        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.URANIUM_ORE)
                .add(ModBlocks.DEEPSLATE_URANIUM_ORE);
    }
}
