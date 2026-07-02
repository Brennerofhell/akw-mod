package ch.danielt.akw.datagen;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModTagsProvider extends BlockTagsProvider {

    public ModTagsProvider(PackOutput output,
                           CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture, AkwMod.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Alle AKW-Blöcke sind mit der Spitzhacke abbaubar
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.URANIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_URANIUM_ORE.get())
                .add(ModBlocks.NUCLEAR_REACTOR.get())
                .add(ModBlocks.ADVANCED_NUCLEAR_REACTOR.get())
                .add(ModBlocks.ELITE_NUCLEAR_REACTOR.get())
                .add(ModBlocks.BREEDER_REACTOR.get())
                .add(ModBlocks.THORIUM_REACTOR.get())
                .add(ModBlocks.FUSION_REACTOR.get())
                .add(ModBlocks.REACTOR_CORE.get())
                .add(ModBlocks.CONTROL_ROD_BLOCK.get())
                .add(ModBlocks.COOLING_PIPE.get())
                .add(ModBlocks.LEAD_BLOCK.get())
                .add(ModBlocks.WASTE_CONTAINER.get())
                .add(ModBlocks.ENRICHED_URANIUM_BLOCK.get())
                .add(ModBlocks.ENERGY_CABLE.get())
                .add(ModBlocks.ENERGY_BATTERY.get())
                .add(ModBlocks.REACTOR_CASING.get())
                .add(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get())
                .add(ModBlocks.REACTOR_BUILDER_CONTROLLER.get())
                .add(ModBlocks.REACTOR_ENERGY_PORT.get())
                .add(ModBlocks.REACTOR_ITEM_PORT.get());

        // Erze erfordern mindestens eine Eisen-Spitzhacke
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.URANIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_URANIUM_ORE.get());
    }
}
