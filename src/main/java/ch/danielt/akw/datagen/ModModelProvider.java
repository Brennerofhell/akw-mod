package ch.danielt.akw.datagen;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, AkwMod.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Multiblock-System
        blockModels.createTrivialCube(ModBlocks.REACTOR_CASING.get());
        blockModels.createTrivialCube(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get());
        blockModels.createTrivialCube(ModBlocks.REACTOR_BUILDER_CONTROLLER.get());
        blockModels.createTrivialCube(ModBlocks.REACTOR_ENERGY_PORT.get());
        blockModels.createTrivialCube(ModBlocks.REACTOR_ITEM_PORT.get());

        // Einfache Würfel-Blöcke
        blockModels.createTrivialCube(ModBlocks.URANIUM_ORE.get());
        blockModels.createTrivialCube(ModBlocks.DEEPSLATE_URANIUM_ORE.get());
        blockModels.createTrivialCube(ModBlocks.REACTOR_CORE.get());
        blockModels.createTrivialCube(ModBlocks.CONTROL_ROD_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.COOLING_PIPE.get());
        blockModels.createTrivialCube(ModBlocks.LEAD_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.WASTE_CONTAINER.get());
        blockModels.createTrivialCube(ModBlocks.ENRICHED_URANIUM_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.ENERGY_CABLE.get());
        blockModels.createTrivialCube(ModBlocks.ENERGY_BATTERY.get());

        // Reaktoren: orientierbar (FACING) + LIT-Zustand — identisch zum Ofen.
        // createFurnace erzeugt Aus-/An-Modell (_front bzw. _front_on), den
        // Blockstate (HORIZONTAL_FACING × LIT) und das Item-Modell.
        for (DeferredBlock<? extends Block> block : ModBlocks.REACTORS) {
            blockModels.createFurnace(block.get(), TexturedModel.ORIENTABLE);
        }

        // Item-Modelle (flach generiert)
        itemModels.generateFlatItem(ModItems.RAW_URANIUM.get(),      ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.URANIUM_INGOT.get(),    ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ENRICHED_URANIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FUEL_ROD.get(),         ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SPENT_FUEL_ROD.get(),   ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.REACTOR_WRENCH.get(),   ModelTemplates.FLAT_ITEM);
    }
}
