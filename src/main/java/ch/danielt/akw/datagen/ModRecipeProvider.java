package ch.danielt.akw.datagen;

import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output,
                             CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        // Uran-Barren schmelzen / blasen
        CookingRecipeJsonBuilder
                .createSmelting(Ingredient.ofItems(ModItems.RAW_URANIUM),
                        RecipeCategory.MISC, ModItems.URANIUM_INGOT, 0.7f, 200)
                .criterion(hasItem(ModItems.RAW_URANIUM), conditionsFromItem(ModItems.RAW_URANIUM))
                .offerTo(exporter, Identifier.of("akw", "uranium_ingot_from_smelting"));

        CookingRecipeJsonBuilder
                .createBlasting(Ingredient.ofItems(ModItems.RAW_URANIUM),
                        RecipeCategory.MISC, ModItems.URANIUM_INGOT, 0.7f, 100)
                .criterion(hasItem(ModItems.RAW_URANIUM), conditionsFromItem(ModItems.RAW_URANIUM))
                .offerTo(exporter, Identifier.of("akw", "uranium_ingot_from_blasting"));

        // Brennstab
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.FUEL_ROD)
                .pattern("I").pattern("I").pattern("I")
                .input('I', ModItems.URANIUM_INGOT)
                .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                .offerTo(exporter, Identifier.of("akw", "fuel_rod"));

        // Reaktoren
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.NUCLEAR_REACTOR)
                .pattern("III").pattern("UFU").pattern("IRI")
                .input('I', Items.IRON_INGOT)
                .input('U', ModItems.URANIUM_INGOT)
                .input('F', Items.FURNACE)
                .input('R', Items.REDSTONE)
                .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                .offerTo(exporter, Identifier.of("akw", "nuclear_reactor"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ADVANCED_NUCLEAR_REACTOR)
                .pattern("GCG").pattern("CNC").pattern("GCG")
                .input('G', Items.GOLD_INGOT)
                .input('C', ModBlocks.CONTROL_ROD_BLOCK)
                .input('N', ModBlocks.NUCLEAR_REACTOR)
                .criterion(hasItem(ModBlocks.NUCLEAR_REACTOR), conditionsFromItem(ModBlocks.NUCLEAR_REACTOR))
                .offerTo(exporter, Identifier.of("akw", "advanced_nuclear_reactor"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ELITE_NUCLEAR_REACTOR)
                .pattern("DCD").pattern("CAC").pattern("DCD")
                .input('D', Items.DIAMOND)
                .input('C', ModBlocks.COOLING_PIPE)
                .input('A', ModBlocks.ADVANCED_NUCLEAR_REACTOR)
                .criterion(hasItem(ModBlocks.ADVANCED_NUCLEAR_REACTOR),
                        conditionsFromItem(ModBlocks.ADVANCED_NUCLEAR_REACTOR))
                .offerTo(exporter, Identifier.of("akw", "elite_nuclear_reactor"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.BREEDER_REACTOR)
                .pattern("CCC").pattern("UNU").pattern("CCC")
                .input('C', Items.COPPER_INGOT)
                .input('U', ModBlocks.ENRICHED_URANIUM_BLOCK)
                .input('N', ModBlocks.NUCLEAR_REACTOR)
                .criterion(hasItem(ModBlocks.NUCLEAR_REACTOR), conditionsFromItem(ModBlocks.NUCLEAR_REACTOR))
                .offerTo(exporter, Identifier.of("akw", "breeder_reactor"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.THORIUM_REACTOR)
                .pattern("MEM").pattern("ENE").pattern("MEM")
                .input('M', Items.EMERALD)
                .input('E', ModBlocks.ENRICHED_URANIUM_BLOCK)
                .input('N', ModBlocks.NUCLEAR_REACTOR)
                .criterion(hasItem(ModBlocks.NUCLEAR_REACTOR), conditionsFromItem(ModBlocks.NUCLEAR_REACTOR))
                .offerTo(exporter, Identifier.of("akw", "thorium_reactor"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.FUSION_REACTOR)
                .pattern("NDN").pattern("DED").pattern("NDN")
                .input('N', Items.NETHERITE_INGOT)
                .input('D', Items.DIAMOND_BLOCK)
                .input('E', ModBlocks.ELITE_NUCLEAR_REACTOR)
                .criterion(hasItem(ModBlocks.ELITE_NUCLEAR_REACTOR),
                        conditionsFromItem(ModBlocks.ELITE_NUCLEAR_REACTOR))
                .offerTo(exporter, Identifier.of("akw", "fusion_reactor"));

        // Bausteine
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.REACTOR_CORE)
                .pattern("UUU").pattern("UFU").pattern("UUU")
                .input('U', ModItems.URANIUM_INGOT)
                .input('F', ModItems.FUEL_ROD)
                .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                .offerTo(exporter, Identifier.of("akw", "reactor_core"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.CONTROL_ROD_BLOCK)
                .pattern("IUI").pattern("IUI").pattern("IUI")
                .input('I', Items.IRON_INGOT)
                .input('U', ModItems.URANIUM_INGOT)
                .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                .offerTo(exporter, Identifier.of("akw", "control_rod_block"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.COOLING_PIPE, 2)
                .pattern("C C").pattern("C C").pattern("C C")
                .input('C', Items.COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter, Identifier.of("akw", "cooling_pipe"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.LEAD_BLOCK)
                .pattern("IUI").pattern("UIU").pattern("IUI")
                .input('I', Items.IRON_INGOT)
                .input('U', ModItems.URANIUM_INGOT)
                .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                .offerTo(exporter, Identifier.of("akw", "lead_block"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.WASTE_CONTAINER)
                .pattern("IFI").pattern("IFI").pattern("III")
                .input('I', Items.IRON_INGOT)
                .input('F', ModItems.FUEL_ROD)
                .criterion(hasItem(ModItems.FUEL_ROD), conditionsFromItem(ModItems.FUEL_ROD))
                .offerTo(exporter, Identifier.of("akw", "waste_container"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENRICHED_URANIUM_BLOCK)
                .pattern("UUU").pattern("UUU").pattern("UUU")
                .input('U', ModItems.URANIUM_INGOT)
                .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                .offerTo(exporter, Identifier.of("akw", "enriched_uranium_block"));
    }
}
