package ch.danielt.akw.datagen;

import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

/** Block-Loot-Tabellen: Erze mit Silk-Touch/Fortune, alle anderen Bloecke droppen sich selbst. */
public class ModLootTableProvider extends BlockLootSubProvider {

    public ModLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.VANILLA_SET, registries);
    }

    private static final List<Block> KNOWN_BLOCKS = List.of(
            ModBlocks.URANIUM_ORE.get(),
            ModBlocks.DEEPSLATE_URANIUM_ORE.get(),
            ModBlocks.NUCLEAR_REACTOR.get(),
            ModBlocks.ADVANCED_NUCLEAR_REACTOR.get(),
            ModBlocks.ELITE_NUCLEAR_REACTOR.get(),
            ModBlocks.BREEDER_REACTOR.get(),
            ModBlocks.THORIUM_REACTOR.get(),
            ModBlocks.FUSION_REACTOR.get(),
            ModBlocks.REACTOR_CORE.get(),
            ModBlocks.CONTROL_ROD_BLOCK.get(),
            ModBlocks.COOLING_PIPE.get(),
            ModBlocks.LEAD_BLOCK.get(),
            ModBlocks.WASTE_CONTAINER.get(),
            ModBlocks.ENRICHED_URANIUM_BLOCK.get(),
            ModBlocks.REACTOR_CASING.get(),
            ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get(),
            ModBlocks.REACTOR_BUILDER_CONTROLLER.get(),
            ModBlocks.REACTOR_ENERGY_PORT.get(),
            ModBlocks.REACTOR_ITEM_PORT.get(),
            ModBlocks.REACTOR_REDSTONE_PORT.get(),
            ModBlocks.DAMAGED_REACTOR_CORE.get(),
            ModBlocks.REACTOR_GLASS.get(),
            ModBlocks.GRAPHITE_MODERATOR.get(),
            ModBlocks.ENERGY_CABLE.get(),
            ModBlocks.ENERGY_BATTERY.get());

    @Override
    protected void generate() {
        // Erze: Silk Touch → Block, sonst Roh-Uran (Fortune wirkt)
        add(ModBlocks.URANIUM_ORE.get(),
                createOreDrop(ModBlocks.URANIUM_ORE.get(), ModItems.RAW_URANIUM.get()));
        add(ModBlocks.DEEPSLATE_URANIUM_ORE.get(),
                createOreDrop(ModBlocks.DEEPSLATE_URANIUM_ORE.get(), ModItems.RAW_URANIUM.get()));

        // Reaktoren
        dropSelf(ModBlocks.NUCLEAR_REACTOR.get());
        dropSelf(ModBlocks.ADVANCED_NUCLEAR_REACTOR.get());
        dropSelf(ModBlocks.ELITE_NUCLEAR_REACTOR.get());
        dropSelf(ModBlocks.BREEDER_REACTOR.get());
        dropSelf(ModBlocks.THORIUM_REACTOR.get());
        dropSelf(ModBlocks.FUSION_REACTOR.get());

        // Bausteine
        dropSelf(ModBlocks.REACTOR_CORE.get());
        dropSelf(ModBlocks.CONTROL_ROD_BLOCK.get());
        dropSelf(ModBlocks.COOLING_PIPE.get());
        dropSelf(ModBlocks.LEAD_BLOCK.get());
        dropSelf(ModBlocks.WASTE_CONTAINER.get());
        dropSelf(ModBlocks.ENRICHED_URANIUM_BLOCK.get());
        dropSelf(ModBlocks.REACTOR_CASING.get());
        dropSelf(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get());
        dropSelf(ModBlocks.REACTOR_BUILDER_CONTROLLER.get());
        dropSelf(ModBlocks.REACTOR_ENERGY_PORT.get());
        dropSelf(ModBlocks.REACTOR_ITEM_PORT.get());
        dropSelf(ModBlocks.REACTOR_REDSTONE_PORT.get());
        dropSelf(ModBlocks.DAMAGED_REACTOR_CORE.get());
        dropSelf(ModBlocks.REACTOR_GLASS.get());
        dropSelf(ModBlocks.GRAPHITE_MODERATOR.get());

        // Energie-Infrastruktur
        dropSelf(ModBlocks.ENERGY_CABLE.get());
        dropSelf(ModBlocks.ENERGY_BATTERY.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return KNOWN_BLOCKS;
    }
}
