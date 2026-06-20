package ch.danielt.akw.datagen;

import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLanguageProvider {

    public static class German extends FabricLanguageProvider {

        public German(FabricDataOutput dataOutput,
                      CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, "de_de", registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup registries,
                                         TranslationBuilder builder) {
            builder.add("itemgroup.akw", "Atomkraftwerk");

            builder.add(ModItems.RAW_URANIUM,   "Roh-Uran");
            builder.add(ModItems.URANIUM_INGOT, "Uran-Barren");
            builder.add(ModItems.FUEL_ROD,      "Brennstab");

            builder.add(ModBlocks.URANIUM_ORE,           "Uranerz");
            builder.add(ModBlocks.DEEPSLATE_URANIUM_ORE, "Tiefenschiefer-Uranerz");

            builder.add(ModBlocks.NUCLEAR_REACTOR,          "Reaktor");
            builder.add(ModBlocks.ADVANCED_NUCLEAR_REACTOR, "Fortgeschrittener Reaktor");
            builder.add(ModBlocks.ELITE_NUCLEAR_REACTOR,    "Elite-Reaktor");
            builder.add(ModBlocks.BREEDER_REACTOR,          "Brutreaktor");
            builder.add(ModBlocks.THORIUM_REACTOR,          "Thorium-Reaktor");
            builder.add(ModBlocks.FUSION_REACTOR,           "Fusionsreaktor");

            builder.add(ModBlocks.REACTOR_CORE,           "Reaktorkern");
            builder.add(ModBlocks.CONTROL_ROD_BLOCK,      "Steuerstab-Block");
            builder.add(ModBlocks.COOLING_PIPE,           "Kühlrohr");
            builder.add(ModBlocks.LEAD_BLOCK,             "Blei-Block");
            builder.add(ModBlocks.WASTE_CONTAINER,        "Abfallbehälter");
            builder.add(ModBlocks.ENRICHED_URANIUM_BLOCK, "Angereicherter-Uran-Block");

            builder.add(ModBlocks.ENERGY_CABLE,           "Energie-Kabel");
            builder.add(ModBlocks.ENERGY_BATTERY,         "Akku-Block");

            // Advancements
            builder.add("advancements.akw.mine_uranium.title",  "Uranabbau beginnt");
            builder.add("advancements.akw.mine_uranium.desc",   "Baue ein Atomkraftwerk und verändere die Welt");
            builder.add("advancements.akw.smelt_uranium.title", "Erstes Metall");
            builder.add("advancements.akw.smelt_uranium.desc",  "Schmelze Roh-Uran zu einem Uran-Barren");
            builder.add("advancements.akw.enrich.title",        "Anreicherung");
            builder.add("advancements.akw.enrich.desc",         "Stelle Angereichertes Uran her");
            builder.add("advancements.akw.fuel_rod.title",      "Brennstab bereit");
            builder.add("advancements.akw.fuel_rod.desc",       "Fertige einen Brennstab an");
            builder.add("advancements.akw.first_reactor.title", "Erster Reaktor");
            builder.add("advancements.akw.first_reactor.desc",  "Baue deinen ersten Kernreaktor");
            builder.add("advancements.akw.energy_online.title", "Energie online");
            builder.add("advancements.akw.energy_online.desc",  "Verbinde den Reaktor mit dem Stromnetz");
            builder.add("advancements.akw.elite_reactor.title", "Elite-Klasse");
            builder.add("advancements.akw.elite_reactor.desc",  "Baue einen Elite-Nuklearreaktor");
            builder.add("advancements.akw.fusion_reactor.title","Kernfusion");
            builder.add("advancements.akw.fusion_reactor.desc", "Zähme die Kraft der Sonne");
            builder.add("advancements.akw.multiblock.title",    "Multiblock-Meister");
            builder.add("advancements.akw.multiblock.desc",     "Aktiviere einen Multiblock-Reaktor");
        }
    }

    public static class English extends FabricLanguageProvider {

        public English(FabricDataOutput dataOutput,
                       CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup registries,
                                         TranslationBuilder builder) {
            builder.add("itemgroup.akw", "Nuclear Power Plant");

            builder.add(ModItems.RAW_URANIUM,   "Raw Uranium");
            builder.add(ModItems.URANIUM_INGOT, "Uranium Ingot");
            builder.add(ModItems.FUEL_ROD,      "Fuel Rod");

            builder.add(ModBlocks.URANIUM_ORE,           "Uranium Ore");
            builder.add(ModBlocks.DEEPSLATE_URANIUM_ORE, "Deepslate Uranium Ore");

            builder.add(ModBlocks.NUCLEAR_REACTOR,          "Nuclear Reactor");
            builder.add(ModBlocks.ADVANCED_NUCLEAR_REACTOR, "Advanced Nuclear Reactor");
            builder.add(ModBlocks.ELITE_NUCLEAR_REACTOR,    "Elite Nuclear Reactor");
            builder.add(ModBlocks.BREEDER_REACTOR,          "Breeder Reactor");
            builder.add(ModBlocks.THORIUM_REACTOR,          "Thorium Reactor");
            builder.add(ModBlocks.FUSION_REACTOR,           "Fusion Reactor");

            builder.add(ModBlocks.REACTOR_CORE,           "Reactor Core");
            builder.add(ModBlocks.CONTROL_ROD_BLOCK,      "Control Rod Block");
            builder.add(ModBlocks.COOLING_PIPE,           "Cooling Pipe");
            builder.add(ModBlocks.LEAD_BLOCK,             "Lead Block");
            builder.add(ModBlocks.WASTE_CONTAINER,        "Waste Container");
            builder.add(ModBlocks.ENRICHED_URANIUM_BLOCK, "Enriched Uranium Block");

            builder.add(ModBlocks.ENERGY_CABLE,           "Energy Cable");
            builder.add(ModBlocks.ENERGY_BATTERY,         "Battery Block");

            // Advancements
            builder.add("advancements.akw.mine_uranium.title",  "Nuclear Beginnings");
            builder.add("advancements.akw.mine_uranium.desc",   "Mine uranium and start your nuclear journey");
            builder.add("advancements.akw.smelt_uranium.title", "First Metal");
            builder.add("advancements.akw.smelt_uranium.desc",  "Smelt raw uranium into an ingot");
            builder.add("advancements.akw.enrich.title",        "Enrichment");
            builder.add("advancements.akw.enrich.desc",         "Produce enriched uranium");
            builder.add("advancements.akw.fuel_rod.title",      "Fuel Rod Ready");
            builder.add("advancements.akw.fuel_rod.desc",       "Craft a fuel rod");
            builder.add("advancements.akw.first_reactor.title", "First Reactor");
            builder.add("advancements.akw.first_reactor.desc",  "Build your first nuclear reactor");
            builder.add("advancements.akw.energy_online.title", "Power Online");
            builder.add("advancements.akw.energy_online.desc",  "Connect the reactor to the power grid");
            builder.add("advancements.akw.elite_reactor.title", "Elite Class");
            builder.add("advancements.akw.elite_reactor.desc",  "Build an elite nuclear reactor");
            builder.add("advancements.akw.fusion_reactor.title","Nuclear Fusion");
            builder.add("advancements.akw.fusion_reactor.desc", "Harness the power of the sun");
            builder.add("advancements.akw.multiblock.title",    "Multiblock Master");
            builder.add("advancements.akw.multiblock.desc",     "Activate a multiblock reactor");
        }
    }
}
