package ch.danielt.akw.datagen;

import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.TexturedModel;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {
        // Einfache Würfel-Blöcke
        gen.registerSimpleCubeAll(ModBlocks.URANIUM_ORE);
        gen.registerSimpleCubeAll(ModBlocks.DEEPSLATE_URANIUM_ORE);
        gen.registerSimpleCubeAll(ModBlocks.REACTOR_CORE);
        gen.registerSimpleCubeAll(ModBlocks.CONTROL_ROD_BLOCK);
        gen.registerSimpleCubeAll(ModBlocks.COOLING_PIPE);
        gen.registerSimpleCubeAll(ModBlocks.LEAD_BLOCK);
        gen.registerSimpleCubeAll(ModBlocks.WASTE_CONTAINER);
        gen.registerSimpleCubeAll(ModBlocks.ENRICHED_URANIUM_BLOCK);
        gen.registerSimpleCubeAll(ModBlocks.ENERGY_CABLE);
        gen.registerSimpleCubeAll(ModBlocks.ENERGY_BATTERY);

        // Reaktoren: orientierbar (FACING) + LIT-Zustand — identisch zum Ofen.
        // registerCooker erzeugt Aus-/An-Modell (_front bzw. _front_on), den
        // Blockstate (HORIZONTAL_FACING × LIT) und das Item-Modell.
        for (Block block : ModBlocks.REACTORS) {
            gen.registerCooker(block, TexturedModel.ORIENTABLE);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerator gen) {
        gen.register(ModItems.RAW_URANIUM,       Models.GENERATED);
        gen.register(ModItems.URANIUM_INGOT,     Models.GENERATED);
        gen.register(ModItems.ENRICHED_URANIUM,  Models.GENERATED);
        gen.register(ModItems.FUEL_ROD,          Models.GENERATED);
    }
}
