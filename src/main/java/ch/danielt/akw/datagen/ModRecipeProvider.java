package ch.danielt.akw.datagen;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider.Runner {

    public ModRecipeProvider(PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "AKW Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            protected void buildRecipes() {
                // Uran-Barren schmelzen / blasen
                SimpleCookingRecipeBuilder
                        .smelting(Ingredient.of(ModItems.RAW_URANIUM), RecipeCategory.MISC,
                                ModItems.URANIUM_INGOT, 0.7f, 200)
                        .unlockedBy(getHasName(ModItems.RAW_URANIUM), has(ModItems.RAW_URANIUM))
                        .save(output, key("uranium_ingot_from_smelting"));

                SimpleCookingRecipeBuilder
                        .blasting(Ingredient.of(ModItems.RAW_URANIUM), RecipeCategory.MISC,
                                ModItems.URANIUM_INGOT, 0.7f, 100)
                        .unlockedBy(getHasName(ModItems.RAW_URANIUM), has(ModItems.RAW_URANIUM))
                        .save(output, key("uranium_ingot_from_blasting"));

                // Angereichertes Uran: 2 Uran-Barren → 1 Angereichertes Uran
                shaped(RecipeCategory.MISC, ModItems.ENRICHED_URANIUM)
                        .pattern("UU")
                        .define('U', ModItems.URANIUM_INGOT)
                        .unlockedBy(getHasName(ModItems.URANIUM_INGOT), has(ModItems.URANIUM_INGOT))
                        .save(output, key("enriched_uranium"));

                // Brennstab: 3 Angereicherte Uran-Einheiten
                shaped(RecipeCategory.MISC, ModItems.FUEL_ROD)
                        .pattern("E").pattern("E").pattern("E")
                        .define('E', ModItems.ENRICHED_URANIUM)
                        .unlockedBy(getHasName(ModItems.ENRICHED_URANIUM), has(ModItems.ENRICHED_URANIUM))
                        .save(output, key("fuel_rod"));

                // Reaktoren
                shaped(RecipeCategory.MISC, ModBlocks.NUCLEAR_REACTOR)
                        .pattern("III").pattern("UFU").pattern("IRI")
                        .define('I', Items.IRON_INGOT)
                        .define('U', ModItems.URANIUM_INGOT)
                        .define('F', Items.FURNACE)
                        .define('R', Items.REDSTONE)
                        .unlockedBy(getHasName(ModItems.URANIUM_INGOT), has(ModItems.URANIUM_INGOT))
                        .save(output, key("nuclear_reactor"));

                shaped(RecipeCategory.MISC, ModBlocks.ADVANCED_NUCLEAR_REACTOR)
                        .pattern("GCG").pattern("CNC").pattern("GCG")
                        .define('G', Items.GOLD_INGOT)
                        .define('C', ModBlocks.CONTROL_ROD_BLOCK)
                        .define('N', ModBlocks.NUCLEAR_REACTOR)
                        .unlockedBy(getHasName(ModBlocks.NUCLEAR_REACTOR), has(ModBlocks.NUCLEAR_REACTOR))
                        .save(output, key("advanced_nuclear_reactor"));

                shaped(RecipeCategory.MISC, ModBlocks.ELITE_NUCLEAR_REACTOR)
                        .pattern("DCD").pattern("CAC").pattern("DCD")
                        .define('D', Items.DIAMOND)
                        .define('C', ModBlocks.COOLING_PIPE)
                        .define('A', ModBlocks.ADVANCED_NUCLEAR_REACTOR)
                        .unlockedBy(getHasName(ModBlocks.ADVANCED_NUCLEAR_REACTOR),
                                has(ModBlocks.ADVANCED_NUCLEAR_REACTOR))
                        .save(output, key("elite_nuclear_reactor"));

                shaped(RecipeCategory.MISC, ModBlocks.BREEDER_REACTOR)
                        .pattern("CCC").pattern("UNU").pattern("CCC")
                        .define('C', Items.COPPER_INGOT)
                        .define('U', ModBlocks.ENRICHED_URANIUM_BLOCK)
                        .define('N', ModBlocks.NUCLEAR_REACTOR)
                        .unlockedBy(getHasName(ModBlocks.NUCLEAR_REACTOR), has(ModBlocks.NUCLEAR_REACTOR))
                        .save(output, key("breeder_reactor"));

                shaped(RecipeCategory.MISC, ModBlocks.THORIUM_REACTOR)
                        .pattern("MEM").pattern("ENE").pattern("MEM")
                        .define('M', Items.EMERALD)
                        .define('E', ModBlocks.ENRICHED_URANIUM_BLOCK)
                        .define('N', ModBlocks.NUCLEAR_REACTOR)
                        .unlockedBy(getHasName(ModBlocks.NUCLEAR_REACTOR), has(ModBlocks.NUCLEAR_REACTOR))
                        .save(output, key("thorium_reactor"));

                shaped(RecipeCategory.MISC, ModBlocks.FUSION_REACTOR)
                        .pattern("NDN").pattern("DED").pattern("NDN")
                        .define('N', Items.NETHERITE_INGOT)
                        .define('D', Items.DIAMOND_BLOCK)
                        .define('E', ModBlocks.ELITE_NUCLEAR_REACTOR)
                        .unlockedBy(getHasName(ModBlocks.ELITE_NUCLEAR_REACTOR),
                                has(ModBlocks.ELITE_NUCLEAR_REACTOR))
                        .save(output, key("fusion_reactor"));

                // Bausteine
                shaped(RecipeCategory.MISC, ModBlocks.REACTOR_CORE)
                        .pattern("UUU").pattern("UFU").pattern("UUU")
                        .define('U', ModItems.URANIUM_INGOT)
                        .define('F', ModItems.FUEL_ROD)
                        .unlockedBy(getHasName(ModItems.URANIUM_INGOT), has(ModItems.URANIUM_INGOT))
                        .save(output, key("reactor_core"));

                shaped(RecipeCategory.MISC, ModBlocks.CONTROL_ROD_BLOCK)
                        .pattern("IUI").pattern("IUI").pattern("IUI")
                        .define('I', Items.IRON_INGOT)
                        .define('U', ModItems.URANIUM_INGOT)
                        .unlockedBy(getHasName(ModItems.URANIUM_INGOT), has(ModItems.URANIUM_INGOT))
                        .save(output, key("control_rod_block"));

                shaped(RecipeCategory.MISC, ModBlocks.COOLING_PIPE, 2)
                        .pattern("C C").pattern("C C").pattern("C C")
                        .define('C', Items.COPPER_INGOT)
                        .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                        .save(output, key("cooling_pipe"));

                shaped(RecipeCategory.MISC, ModBlocks.LEAD_BLOCK)
                        .pattern("IUI").pattern("UIU").pattern("IUI")
                        .define('I', Items.IRON_INGOT)
                        .define('U', ModItems.URANIUM_INGOT)
                        .unlockedBy(getHasName(ModItems.URANIUM_INGOT), has(ModItems.URANIUM_INGOT))
                        .save(output, key("lead_block"));

                shaped(RecipeCategory.MISC, ModBlocks.WASTE_CONTAINER)
                        .pattern("IFI").pattern("IFI").pattern("III")
                        .define('I', Items.IRON_INGOT)
                        .define('F', ModItems.FUEL_ROD)
                        .unlockedBy(getHasName(ModItems.FUEL_ROD), has(ModItems.FUEL_ROD))
                        .save(output, key("waste_container"));

                shaped(RecipeCategory.MISC, ModBlocks.ENRICHED_URANIUM_BLOCK)
                        .pattern("UUU").pattern("UUU").pattern("UUU")
                        .define('U', ModItems.URANIUM_INGOT)
                        .unlockedBy(getHasName(ModItems.URANIUM_INGOT), has(ModItems.URANIUM_INGOT))
                        .save(output, key("enriched_uranium_block"));

                // Multiblock-Reaktor-System
                shaped(RecipeCategory.MISC, ModItems.REACTOR_WRENCH)
                        .pattern(" I").pattern("IS")
                        .define('I', Items.IRON_INGOT)
                        .define('S', Items.STICK)
                        .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                        .save(output, key("reactor_wrench"));

                shaped(RecipeCategory.MISC, ModBlocks.REACTOR_CASING, 4)
                        .pattern("ILI").pattern("LIL").pattern("ILI")
                        .define('I', Items.IRON_INGOT)
                        .define('L', ModBlocks.LEAD_BLOCK)
                        .unlockedBy(getHasName(ModBlocks.LEAD_BLOCK), has(ModBlocks.LEAD_BLOCK))
                        .save(output, key("reactor_casing"));

                shaped(RecipeCategory.MISC, ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER)
                        .pattern("CRC").pattern("RNR").pattern("CRC")
                        .define('C', ModBlocks.REACTOR_CASING)
                        .define('R', Items.REDSTONE_BLOCK)
                        .define('N', ModBlocks.NUCLEAR_REACTOR)
                        .unlockedBy(getHasName(ModBlocks.REACTOR_CASING), has(ModBlocks.REACTOR_CASING))
                        .save(output, key("multiblock_reactor_controller"));

                // Energie-Port: Kabel auf Hüllenblock
                shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_ENERGY_PORT)
                        .pattern("E").pattern("C")
                        .define('E', ModBlocks.ENERGY_CABLE)
                        .define('C', ModBlocks.REACTOR_CASING)
                        .unlockedBy(getHasName(ModBlocks.REACTOR_CASING), has(ModBlocks.REACTOR_CASING))
                        .save(output, key("reactor_energy_port"));

                // Item-Port: Trichter auf Hüllenblock
                shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_ITEM_PORT)
                        .pattern("H").pattern("C")
                        .define('H', Items.HOPPER)
                        .define('C', ModBlocks.REACTOR_CASING)
                        .unlockedBy(getHasName(ModBlocks.REACTOR_CASING), has(ModBlocks.REACTOR_CASING))
                        .save(output, key("reactor_item_port"));

                shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_BUILDER_CONTROLLER)
                        .pattern("CRC").pattern("EBE").pattern("CRC")
                        .define('C', ModBlocks.REACTOR_CASING)
                        .define('R', Items.REDSTONE_BLOCK)
                        .define('E', ModBlocks.ENERGY_CABLE)
                        .define('B', ModBlocks.ENERGY_BATTERY)
                        .unlockedBy(getHasName(ModBlocks.REACTOR_CASING), has(ModBlocks.REACTOR_CASING))
                        .save(output, key("reactor_builder_controller"));

                // Energie-Infrastruktur
                shaped(RecipeCategory.REDSTONE, ModBlocks.ENERGY_CABLE, 3)
                        .pattern("CRC")
                        .define('C', Items.COPPER_INGOT)
                        .define('R', Items.REDSTONE)
                        .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                        .save(output, key("energy_cable"));

                shaped(RecipeCategory.REDSTONE, ModBlocks.ENERGY_BATTERY)
                        .pattern("ICI").pattern("RRR").pattern("ICI")
                        .define('I', Items.IRON_INGOT)
                        .define('C', Items.COPPER_INGOT)
                        .define('R', Items.REDSTONE)
                        .unlockedBy(getHasName(Items.REDSTONE), has(Items.REDSTONE))
                        .save(output, key("energy_battery"));
            }
        };
    }

    private static ResourceKey<Recipe<?>> key(String path) {
        return ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(AkwMod.MOD_ID, path));
    }
}
