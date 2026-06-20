package ch.danielt.akw.datagen;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Erzeugt die Advancement-Kette fuer den AKW-Mod (v0.9.5).
 * Kette: Uranabbau → Schmelzen → Anreicherung → Brennstab →
 *        Erster Reaktor → Energie online → (Elite / Fusion / Multiblock)
 */
public class ModAdvancementProvider extends FabricAdvancementProvider {

    public ModAdvancementProvider(FabricDataOutput output,
                                   CompletableFuture<RegistryWrapper.WrapperLookup> lookup) {
        super(output, lookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup lookup,
                                     Consumer<AdvancementEntry> exporter) {

        // --- Wurzel: Uran abbauen ---
        AdvancementEntry mineUranium = Advancement.Builder.create()
                .display(
                        ModItems.RAW_URANIUM,
                        Text.translatable("advancements.akw.mine_uranium.title"),
                        Text.translatable("advancements.akw.mine_uranium.desc"),
                        Identifier.ofVanilla("textures/gui/advancements/backgrounds/stone.png"),
                        AdvancementFrame.TASK, true, true, false)
                .criterion("has_raw_uranium",
                        InventoryChangedCriterion.Conditions.items(ModItems.RAW_URANIUM))
                .build(exporter, AkwMod.MOD_ID + ":story/mine_uranium");

        // --- Uran-Barren schmelzen ---
        AdvancementEntry smeltUranium = Advancement.Builder.create()
                .parent(mineUranium)
                .display(
                        ModItems.URANIUM_INGOT,
                        Text.translatable("advancements.akw.smelt_uranium.title"),
                        Text.translatable("advancements.akw.smelt_uranium.desc"),
                        null,
                        AdvancementFrame.TASK, true, true, false)
                .criterion("has_uranium_ingot",
                        InventoryChangedCriterion.Conditions.items(ModItems.URANIUM_INGOT))
                .build(exporter, AkwMod.MOD_ID + ":story/smelt_uranium");

        // --- Uran anreichern (Icon: ENRICHED_URANIUM_BLOCK, da kein separates Item) ---
        AdvancementEntry enrich = Advancement.Builder.create()
                .parent(smeltUranium)
                .display(
                        ModBlocks.ENRICHED_URANIUM_BLOCK,
                        Text.translatable("advancements.akw.enrich.title"),
                        Text.translatable("advancements.akw.enrich.desc"),
                        null,
                        AdvancementFrame.TASK, true, true, false)
                .criterion("has_enriched_uranium",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.ENRICHED_URANIUM_BLOCK))
                .build(exporter, AkwMod.MOD_ID + ":story/enrich");

        // --- Brennstab herstellen ---
        AdvancementEntry fuelRod = Advancement.Builder.create()
                .parent(enrich)
                .display(
                        ModItems.FUEL_ROD,
                        Text.translatable("advancements.akw.fuel_rod.title"),
                        Text.translatable("advancements.akw.fuel_rod.desc"),
                        null,
                        AdvancementFrame.TASK, true, true, false)
                .criterion("has_fuel_rod",
                        InventoryChangedCriterion.Conditions.items(ModItems.FUEL_ROD))
                .build(exporter, AkwMod.MOD_ID + ":story/fuel_rod");

        // --- Erster Reaktor (NUCLEAR_REACTOR = Index 0 in REACTORS-Liste) ---
        AdvancementEntry firstReactor = Advancement.Builder.create()
                .parent(fuelRod)
                .display(
                        ModBlocks.NUCLEAR_REACTOR,
                        Text.translatable("advancements.akw.first_reactor.title"),
                        Text.translatable("advancements.akw.first_reactor.desc"),
                        null,
                        AdvancementFrame.GOAL, true, true, false)
                .criterion("has_reactor",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.NUCLEAR_REACTOR))
                .build(exporter, AkwMod.MOD_ID + ":story/first_reactor");

        // --- Energie online: Energiekabel im Inventar ---
        AdvancementEntry energyOnline = Advancement.Builder.create()
                .parent(firstReactor)
                .display(
                        ModBlocks.ENERGY_CABLE,
                        Text.translatable("advancements.akw.energy_online.title"),
                        Text.translatable("advancements.akw.energy_online.desc"),
                        null,
                        AdvancementFrame.GOAL, true, true, false)
                .criterion("has_cable",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.ENERGY_CABLE))
                .build(exporter, AkwMod.MOD_ID + ":story/energy_online");

        // --- Elite-Reaktor (ELITE_NUCLEAR_REACTOR) ---
        Advancement.Builder.create()
                .parent(energyOnline)
                .display(
                        ModBlocks.ELITE_NUCLEAR_REACTOR,
                        Text.translatable("advancements.akw.elite_reactor.title"),
                        Text.translatable("advancements.akw.elite_reactor.desc"),
                        null,
                        AdvancementFrame.CHALLENGE, true, true, false)
                .criterion("has_elite",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.ELITE_NUCLEAR_REACTOR))
                .build(exporter, AkwMod.MOD_ID + ":story/elite_reactor");

        // --- Fusionsreaktor (FUSION_REACTOR) ---
        Advancement.Builder.create()
                .parent(energyOnline)
                .display(
                        ModBlocks.FUSION_REACTOR,
                        Text.translatable("advancements.akw.fusion_reactor.title"),
                        Text.translatable("advancements.akw.fusion_reactor.desc"),
                        null,
                        AdvancementFrame.CHALLENGE, true, true, false)
                .criterion("has_fusion",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.FUSION_REACTOR))
                .build(exporter, AkwMod.MOD_ID + ":story/fusion_reactor");

        // --- Multiblock-Reaktor-Controller (Icon: REACTOR_CORE als Platzhalter) ---
        Advancement.Builder.create()
                .parent(energyOnline)
                .display(
                        ModBlocks.REACTOR_CORE,
                        Text.translatable("advancements.akw.multiblock.title"),
                        Text.translatable("advancements.akw.multiblock.desc"),
                        null,
                        AdvancementFrame.CHALLENGE, true, true, false)
                .criterion("has_controller",
                        InventoryChangedCriterion.Conditions.items(
                                ModBlocks.REACTOR_CORE))
                .build(exporter, AkwMod.MOD_ID + ":story/multiblock");
    }
}
