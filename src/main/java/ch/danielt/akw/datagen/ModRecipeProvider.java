package ch.danielt.akw.datagen;

import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output,
                             CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup,
                                                 RecipeExporter exporter) {
        return new RecipeGenerator(wrapperLookup, exporter) {
            @Override
            public void generate() {
                // Uran-Barren schmelzen / blasen
                CookingRecipeJsonBuilder
                        .createSmelting(Ingredient.ofItems(ModItems.RAW_URANIUM),
                                RecipeCategory.MISC, ModItems.URANIUM_INGOT, 0.7f, 200)
                        .criterion(hasItem(ModItems.RAW_URANIUM), conditionsFromItem(ModItems.RAW_URANIUM))
                        .offerTo(exporter, key("uranium_ingot_from_smelting"));

                CookingRecipeJsonBuilder
                        .createBlasting(Ingredient.ofItems(ModItems.RAW_URANIUM),
                                RecipeCategory.MISC, ModItems.URANIUM_INGOT, 0.7f, 100)
                        .criterion(hasItem(ModItems.RAW_URANIUM), conditionsFromItem(ModItems.RAW_URANIUM))
                        .offerTo(exporter, key("uranium_ingot_from_blasting"));

                // Angereichertes Uran: 2 Uran-Barren → 1 Angereichertes Uran
                createShaped(RecipeCategory.MISC, ModItems.ENRICHED_URANIUM)
                        .pattern("UU")
                        .input('U', ModItems.URANIUM_INGOT)
                        .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                        .offerTo(exporter, key("enriched_uranium"));

                // Brennstab: 3 Angereicherte Uran-Einheiten (statt rohe Barren)
                createShaped(RecipeCategory.MISC, ModItems.FUEL_ROD)
                        .pattern("E").pattern("E").pattern("E")
                        .input('E', ModItems.ENRICHED_URANIUM)
                        .criterion(hasItem(ModItems.ENRICHED_URANIUM), conditionsFromItem(ModItems.ENRICHED_URANIUM))
                        .offerTo(exporter, key("fuel_rod"));

                // Reaktoren
                createShaped(RecipeCategory.MISC, ModBlocks.NUCLEAR_REACTOR)
                        .pattern("III").pattern("UFU").pattern("IRI")
                        .input('I', Items.IRON_INGOT)
                        .input('U', ModItems.URANIUM_INGOT)
                        .input('F', Items.FURNACE)
                        .input('R', Items.REDSTONE)
                        .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                        .offerTo(exporter, key("nuclear_reactor"));

                createShaped(RecipeCategory.MISC, ModBlocks.ADVANCED_NUCLEAR_REACTOR)
                        .pattern("GCG").pattern("CNC").pattern("GCG")
                        .input('G', Items.GOLD_INGOT)
                        .input('C', ModBlocks.CONTROL_ROD_BLOCK)
                        .input('N', ModBlocks.NUCLEAR_REACTOR)
                        .criterion(hasItem(ModBlocks.NUCLEAR_REACTOR), conditionsFromItem(ModBlocks.NUCLEAR_REACTOR))
                        .offerTo(exporter, key("advanced_nuclear_reactor"));

                createShaped(RecipeCategory.MISC, ModBlocks.ELITE_NUCLEAR_REACTOR)
                        .pattern("DCD").pattern("CAC").pattern("DCD")
                        .input('D', Items.DIAMOND)
                        .input('C', ModBlocks.COOLING_PIPE)
                        .input('A', ModBlocks.ADVANCED_NUCLEAR_REACTOR)
                        .criterion(hasItem(ModBlocks.ADVANCED_NUCLEAR_REACTOR),
                                conditionsFromItem(ModBlocks.ADVANCED_NUCLEAR_REACTOR))
                        .offerTo(exporter, key("elite_nuclear_reactor"));

                createShaped(RecipeCategory.MISC, ModBlocks.BREEDER_REACTOR)
                        .pattern("CCC").pattern("UNU").pattern("CCC")
                        .input('C', Items.COPPER_INGOT)
                        .input('U', ModBlocks.ENRICHED_URANIUM_BLOCK)
                        .input('N', ModBlocks.NUCLEAR_REACTOR)
                        .criterion(hasItem(ModBlocks.NUCLEAR_REACTOR), conditionsFromItem(ModBlocks.NUCLEAR_REACTOR))
                        .offerTo(exporter, key("breeder_reactor"));

                createShaped(RecipeCategory.MISC, ModBlocks.THORIUM_REACTOR)
                        .pattern("MEM").pattern("ENE").pattern("MEM")
                        .input('M', Items.EMERALD)
                        .input('E', ModBlocks.ENRICHED_URANIUM_BLOCK)
                        .input('N', ModBlocks.NUCLEAR_REACTOR)
                        .criterion(hasItem(ModBlocks.NUCLEAR_REACTOR), conditionsFromItem(ModBlocks.NUCLEAR_REACTOR))
                        .offerTo(exporter, key("thorium_reactor"));

                createShaped(RecipeCategory.MISC, ModBlocks.FUSION_REACTOR)
                        .pattern("NDN").pattern("DED").pattern("NDN")
                        .input('N', Items.NETHERITE_INGOT)
                        .input('D', Items.DIAMOND_BLOCK)
                        .input('E', ModBlocks.ELITE_NUCLEAR_REACTOR)
                        .criterion(hasItem(ModBlocks.ELITE_NUCLEAR_REACTOR),
                                conditionsFromItem(ModBlocks.ELITE_NUCLEAR_REACTOR))
                        .offerTo(exporter, key("fusion_reactor"));

                // Bausteine
                createShaped(RecipeCategory.MISC, ModBlocks.REACTOR_CORE)
                        .pattern("UUU").pattern("UFU").pattern("UUU")
                        .input('U', ModItems.URANIUM_INGOT)
                        .input('F', ModItems.FUEL_ROD)
                        .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                        .offerTo(exporter, key("reactor_core"));

                createShaped(RecipeCategory.MISC, ModBlocks.CONTROL_ROD_BLOCK)
                        .pattern("IUI").pattern("IUI").pattern("IUI")
                        .input('I', Items.IRON_INGOT)
                        .input('U', ModItems.URANIUM_INGOT)
                        .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                        .offerTo(exporter, key("control_rod_block"));

                createShaped(RecipeCategory.MISC, ModBlocks.COOLING_PIPE, 2)
                        .pattern("C C").pattern("C C").pattern("C C")
                        .input('C', Items.COPPER_INGOT)
                        .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                        .offerTo(exporter, key("cooling_pipe"));

                createShaped(RecipeCategory.MISC, ModBlocks.LEAD_BLOCK)
                        .pattern("IUI").pattern("UIU").pattern("IUI")
                        .input('I', Items.IRON_INGOT)
                        .input('U', ModItems.URANIUM_INGOT)
                        .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                        .offerTo(exporter, key("lead_block"));

                createShaped(RecipeCategory.MISC, ModBlocks.WASTE_CONTAINER)
                        .pattern("IFI").pattern("IFI").pattern("III")
                        .input('I', Items.IRON_INGOT)
                        .input('F', ModItems.FUEL_ROD)
                        .criterion(hasItem(ModItems.FUEL_ROD), conditionsFromItem(ModItems.FUEL_ROD))
                        .offerTo(exporter, key("waste_container"));

                createShaped(RecipeCategory.MISC, ModBlocks.ENRICHED_URANIUM_BLOCK)
                        .pattern("UUU").pattern("UUU").pattern("UUU")
                        .input('U', ModItems.URANIUM_INGOT)
                        .criterion(hasItem(ModItems.URANIUM_INGOT), conditionsFromItem(ModItems.URANIUM_INGOT))
                        .offerTo(exporter, key("enriched_uranium_block"));

                // Energie-Infrastruktur
                createShaped(RecipeCategory.REDSTONE, ModBlocks.ENERGY_CABLE, 3)
                        .pattern("CRC")
                        .input('C', Items.COPPER_INGOT)
                        .input('R', Items.REDSTONE)
                        .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                        .offerTo(exporter, key("energy_cable"));

                createShaped(RecipeCategory.REDSTONE, ModBlocks.ENERGY_BATTERY)
                        .pattern("ICI").pattern("RRR").pattern("ICI")
                        .input('I', Items.IRON_INGOT)
                        .input('C', Items.COPPER_INGOT)
                        .input('R', Items.REDSTONE)
                        .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                        .offerTo(exporter, key("energy_battery"));
            }
        };
    }

    @Override
    public String getName() {
        return "AKW Recipes";
    }

    private static RegistryKey<Recipe<?>> key(String path) {
        return RegistryKey.of(RegistryKeys.RECIPE, Identifier.of("akw", path));
    }
}
