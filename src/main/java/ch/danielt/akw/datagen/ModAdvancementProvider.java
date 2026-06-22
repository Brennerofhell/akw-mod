package ch.danielt.akw.datagen;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * Erzeugt die Advancement-Kette: Uranabbau → Schmelzen → Anreicherung → Brennstab
 * → Erster Reaktor → Energie online → (Elite / Fusion / Multiblock).
 */
public class ModAdvancementProvider implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {

        AdvancementHolder mineUranium = Advancement.Builder.advancement()
                .display(
                        ModItems.RAW_URANIUM,
                        Component.translatable("advancements.akw.mine_uranium.title"),
                        Component.translatable("advancements.akw.mine_uranium.desc"),
                        ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                        AdvancementType.TASK, true, true, false)
                .addCriterion("has_raw_uranium",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RAW_URANIUM))
                .save(consumer, AkwMod.MOD_ID + ":story/mine_uranium");

        AdvancementHolder smeltUranium = Advancement.Builder.advancement()
                .parent(mineUranium)
                .display(
                        ModItems.URANIUM_INGOT,
                        Component.translatable("advancements.akw.smelt_uranium.title"),
                        Component.translatable("advancements.akw.smelt_uranium.desc"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_uranium_ingot",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.URANIUM_INGOT))
                .save(consumer, AkwMod.MOD_ID + ":story/smelt_uranium");

        AdvancementHolder enrich = Advancement.Builder.advancement()
                .parent(smeltUranium)
                .display(
                        ModItems.ENRICHED_URANIUM,
                        Component.translatable("advancements.akw.enrich.title"),
                        Component.translatable("advancements.akw.enrich.desc"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_enriched_uranium",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENRICHED_URANIUM))
                .save(consumer, AkwMod.MOD_ID + ":story/enrich");

        AdvancementHolder fuelRod = Advancement.Builder.advancement()
                .parent(enrich)
                .display(
                        ModItems.FUEL_ROD,
                        Component.translatable("advancements.akw.fuel_rod.title"),
                        Component.translatable("advancements.akw.fuel_rod.desc"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_fuel_rod",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FUEL_ROD))
                .save(consumer, AkwMod.MOD_ID + ":story/fuel_rod");

        AdvancementHolder firstReactor = Advancement.Builder.advancement()
                .parent(fuelRod)
                .display(
                        ModBlocks.NUCLEAR_REACTOR,
                        Component.translatable("advancements.akw.first_reactor.title"),
                        Component.translatable("advancements.akw.first_reactor.desc"),
                        null, AdvancementType.GOAL, true, true, false)
                .addCriterion("has_reactor",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.NUCLEAR_REACTOR))
                .save(consumer, AkwMod.MOD_ID + ":story/first_reactor");

        AdvancementHolder energyOnline = Advancement.Builder.advancement()
                .parent(firstReactor)
                .display(
                        ModBlocks.ENERGY_CABLE,
                        Component.translatable("advancements.akw.energy_online.title"),
                        Component.translatable("advancements.akw.energy_online.desc"),
                        null, AdvancementType.GOAL, true, true, false)
                .addCriterion("has_cable",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ENERGY_CABLE))
                .save(consumer, AkwMod.MOD_ID + ":story/energy_online");

        Advancement.Builder.advancement()
                .parent(energyOnline)
                .display(
                        ModBlocks.ELITE_NUCLEAR_REACTOR,
                        Component.translatable("advancements.akw.elite_reactor.title"),
                        Component.translatable("advancements.akw.elite_reactor.desc"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("has_elite",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ELITE_NUCLEAR_REACTOR))
                .save(consumer, AkwMod.MOD_ID + ":story/elite_reactor");

        Advancement.Builder.advancement()
                .parent(energyOnline)
                .display(
                        ModBlocks.FUSION_REACTOR,
                        Component.translatable("advancements.akw.fusion_reactor.title"),
                        Component.translatable("advancements.akw.fusion_reactor.desc"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("has_fusion",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.FUSION_REACTOR))
                .save(consumer, AkwMod.MOD_ID + ":story/fusion_reactor");

        Advancement.Builder.advancement()
                .parent(energyOnline)
                .display(
                        ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER,
                        Component.translatable("advancements.akw.multiblock.title"),
                        Component.translatable("advancements.akw.multiblock.desc"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("has_controller",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER))
                .save(consumer, AkwMod.MOD_ID + ":story/multiblock");
    }
}
