package ch.danielt.akw.datagen;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider {

    public static class German extends LanguageProvider {

        public German(PackOutput output) {
            super(output, AkwMod.MOD_ID, "de_de");
        }

        @Override
        protected void addTranslations() {
            add("itemgroup.akw", "Atomkraftwerk");

            addItem(ModItems.RAW_URANIUM,       "Roh-Uran");
            addItem(ModItems.URANIUM_INGOT,     "Uran-Barren");
            addItem(ModItems.ENRICHED_URANIUM,  "Angereichertes Uran");
            addItem(ModItems.FUEL_ROD,          "Brennstab");
            addItem(ModItems.SPENT_FUEL_ROD,    "Verbrauchter Brennstab");
            addItem(ModItems.REACTOR_WRENCH,    "Reaktor-Schraubenschlüssel");

            addBlock(ModBlocks.URANIUM_ORE,           "Uranerz");
            addBlock(ModBlocks.DEEPSLATE_URANIUM_ORE, "Tiefenschiefer-Uranerz");

            addBlock(ModBlocks.NUCLEAR_REACTOR,          "Reaktor");
            addBlock(ModBlocks.ADVANCED_NUCLEAR_REACTOR, "Fortgeschrittener Reaktor");
            addBlock(ModBlocks.ELITE_NUCLEAR_REACTOR,    "Elite-Reaktor");
            addBlock(ModBlocks.BREEDER_REACTOR,          "Brutreaktor");
            addBlock(ModBlocks.THORIUM_REACTOR,          "Thorium-Reaktor");
            addBlock(ModBlocks.FUSION_REACTOR,           "Fusionsreaktor");

            addBlock(ModBlocks.REACTOR_CORE,           "Reaktorkern");
            addBlock(ModBlocks.CONTROL_ROD_BLOCK,      "Steuerstab-Block");
            addBlock(ModBlocks.COOLING_PIPE,           "Kühlrohr");
            addBlock(ModBlocks.LEAD_BLOCK,             "Blei-Block");
            addBlock(ModBlocks.WASTE_CONTAINER,        "Abfallbehälter");
            addBlock(ModBlocks.ENRICHED_URANIUM_BLOCK, "Angereicherter-Uran-Block");

            addBlock(ModBlocks.ENERGY_CABLE,           "Energie-Kabel");
            addBlock(ModBlocks.ENERGY_BATTERY,         "Akku-Block");

            addBlock(ModBlocks.REACTOR_CASING,                  "Reaktor-Gehäuse");
            addBlock(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER,   "Multiblock-Reaktor");
            addBlock(ModBlocks.REACTOR_BUILDER_CONTROLLER,      "Bauroboter-Kontrollblock");

            add("akw.builder.started", "Bau gestartet: %s Bloecke verbleiben.");
            add("akw.builder.paused", "Bau pausiert: %s von %s Bloecken gesetzt.");
            add("akw.builder.blocked", "Baustelle blockiert bei %s/%s/%s.");
            add("akw.builder.missing_materials",
                    "Material fehlt: %s Gehaeuse, %s Kern, %s Controller.");
            add("akw.builder.missing_energy", "Noch %s FE fuer den Bau benoetigt.");
            add("akw.builder.robot", "Reaktor-Bauroboter");
            add("akw.builder.status.idle", "Bereit");
            add("akw.builder.status.paused", "Pausiert");
            add("akw.builder.status.building", "Baut");
            add("akw.builder.status.blocked", "Blockiert");
            add("akw.builder.status.materials", "Material fehlt");
            add("akw.builder.status.energy", "Energie fehlt");
            add("akw.builder.status.complete", "Reaktor fertig gebaut");
            add("akw.builder.status.invalid", "Fertiger Reaktor ist ungueltig");

            add("effect.akw.radiation", "Strahlung");
            add("akw.multiblock.assembled",   "Reaktor assembliert: %s Kerne, %s verbundene Kühlrohre.");
            add("akw.multiblock.disassembled","Reaktor deaktiviert.");
            add("akw.multiblock.invalid",     "Ungültige Struktur — prüfe Gehäuse-Blöcke.");
            addBlock(ModBlocks.REACTOR_ENERGY_PORT, "Reaktor-Energie-Port");
            addBlock(ModBlocks.REACTOR_ITEM_PORT, "Reaktor-Item-Port");
            addBlock(ModBlocks.DAMAGED_REACTOR_CORE, "Beschädigter Reaktorkern");
            add("akw.item_port.mode.fuel_input", "Modus: Brennstoff-Eingang");
            add("akw.item_port.mode.waste_output", "Modus: Abfall-Ausgang");
            add("akw.item_port.mode.disabled", "Modus: Deaktiviert");

            add("akw.reactor.error.gap", "Lücke in der Hülle bei %s, %s, %s.");
            add("akw.reactor.error.foreign_block", "Ungültiger Block bei %s, %s, %s.");
            add("akw.reactor.error.no_core", "Kein Reaktorkern im Innenraum gefunden.");
            add("akw.reactor.error.no_energy_port", "Kein Energie-Port in der Hülle.");
            add("akw.reactor.error.too_large", "Hülle größer als 9×9×9 oder nicht geschlossen.");
            add("akw.reactor.error.disconnected_pipe", "Kühlrohr ohne Verbindung zur Hülle bei %s, %s, %s.");

            // Multiblock-GUI (Tabs + Kennzahlen)
            add("akw.gui.tab.overview",    "Übersicht");
            add("akw.gui.tab.control",     "Steuerung");
            add("akw.gui.tab.diagnostics", "Diagnose");
            add("akw.gui.size",            "Größe: %s×%s×%s");
            add("akw.gui.cores",           "Kerne: %s");
            add("akw.gui.energy",          "FE: %s / %s");
            add("akw.gui.production",      "Erzeugung: %s FE/t");
            add("akw.gui.cooling",         "Kühlung: %s HU/t");
            add("akw.gui.heat",            "Hitze: %s / %s");
            add("akw.gui.status",             "Status: %s");
            add("akw.gui.status.unassembled", "Nicht assembliert — siehe Diagnose-Tab.");
            add("akw.reactor.status.unassembled", "Nicht assembliert");
            add("akw.reactor.status.offline",     "Bereit");
            add("akw.reactor.status.starting",    "Anfahren…");
            add("akw.reactor.status.running",     "In Betrieb");
            add("akw.reactor.status.scram",       "SCRAM — Nachzerfallswärme");
            add("akw.reactor.status.cooldown",    "Abkühlung");
            add("akw.reactor.status.damaged",     "BESCHÄDIGT — Kerne reparieren");
            add("akw.gui.control_rods",    "Steuerstäbe: %s%%");
            add("akw.gui.enabled.on",      "Reaktor: AN");
            add("akw.gui.enabled.off",     "Reaktor: AUS");
            add("akw.gui.shutdown_temp",   "Abschaltung: %s%%");
            add("akw.gui.no_errors",       "Keine Fehler.");
            add("akw.gui.more_errors",     "… und %s weitere Fehler.");
            add("akw.gui.layer",           "Schicht %s/%s");
            add("akw.gui.safety.on",       "Sicherung: AN (keine Explosion)");
            add("akw.gui.safety.off",      "Sicherung: ÜBERBRÜCKT ⚠");
            add("akw.damaged_core.repaired", "Kern repariert.");
            add("akw.damaged_core.too_hot",  "Reaktor noch zu heiß für die Reparatur.");

            // Redstone-Modi
            add("akw.redstone_mode.ignored",       "Ignoriert");
            add("akw.redstone_mode.high_enables",  "Signal AN → aktiv");
            add("akw.redstone_mode.high_disables", "Signal AN → Pause");
            add("akw.redstone_mode.emergency_stop","Signal AN → Not-Aus");

            // Komparator-Modi
            add("akw.comparator_mode.energy",      "Komparator: Energie");
            add("akw.comparator_mode.temperature", "Komparator: Temperatur");
            add("akw.comparator_mode.fuel",        "Komparator: Brennstoff");
            add("akw.comparator_mode.waste",       "Komparator: Abfall");

            // Sound-Untertitel
            add("subtitles.akw.reactor_ambient",  "Reaktor läuft");
            add("subtitles.akw.reactor_alert",    "Reaktor-Alarm");
            add("subtitles.akw.reactor_meltdown", "Kernschmelze!");

            // Advancements
            add("advancements.akw.mine_uranium.title",   "Uranabbau beginnt");
            add("advancements.akw.mine_uranium.desc",    "Baue dein erstes Atomkraftwerk");
            add("advancements.akw.smelt_uranium.title",  "Erstes Metall");
            add("advancements.akw.smelt_uranium.desc",   "Schmelze Roh-Uran zu einem Uran-Barren");
            add("advancements.akw.enrich.title",         "Anreicherung");
            add("advancements.akw.enrich.desc",          "Stelle angereichertes Uran her");
            add("advancements.akw.fuel_rod.title",       "Brennstab bereit");
            add("advancements.akw.fuel_rod.desc",        "Fertige einen Brennstab an");
            add("advancements.akw.first_reactor.title",  "Erster Reaktor");
            add("advancements.akw.first_reactor.desc",   "Baue deinen ersten Kernreaktor");
            add("advancements.akw.energy_online.title",  "Energie online");
            add("advancements.akw.energy_online.desc",   "Verbinde den Reaktor mit dem Stromnetz");
            add("advancements.akw.elite_reactor.title",  "Elite-Klasse");
            add("advancements.akw.elite_reactor.desc",   "Baue einen Elite-Nuklearreaktor");
            add("advancements.akw.fusion_reactor.title", "Kernfusion");
            add("advancements.akw.fusion_reactor.desc",  "Zähme die Kraft der Sonne");
            add("advancements.akw.multiblock.title",     "Multiblock-Meister");
            add("advancements.akw.multiblock.desc",      "Aktiviere einen Multiblock-Reaktor");
        }
    }

    public static class English extends LanguageProvider {

        public English(PackOutput output) {
            super(output, AkwMod.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add("itemgroup.akw", "Nuclear Power Plant");

            addItem(ModItems.RAW_URANIUM,       "Raw Uranium");
            addItem(ModItems.URANIUM_INGOT,     "Uranium Ingot");
            addItem(ModItems.ENRICHED_URANIUM,  "Enriched Uranium");
            addItem(ModItems.FUEL_ROD,          "Fuel Rod");
            addItem(ModItems.SPENT_FUEL_ROD,    "Spent Fuel Rod");
            addItem(ModItems.REACTOR_WRENCH,    "Reactor Wrench");

            addBlock(ModBlocks.URANIUM_ORE,           "Uranium Ore");
            addBlock(ModBlocks.DEEPSLATE_URANIUM_ORE, "Deepslate Uranium Ore");

            addBlock(ModBlocks.NUCLEAR_REACTOR,          "Nuclear Reactor");
            addBlock(ModBlocks.ADVANCED_NUCLEAR_REACTOR, "Advanced Nuclear Reactor");
            addBlock(ModBlocks.ELITE_NUCLEAR_REACTOR,    "Elite Nuclear Reactor");
            addBlock(ModBlocks.BREEDER_REACTOR,          "Breeder Reactor");
            addBlock(ModBlocks.THORIUM_REACTOR,          "Thorium Reactor");
            addBlock(ModBlocks.FUSION_REACTOR,           "Fusion Reactor");

            addBlock(ModBlocks.REACTOR_CORE,           "Reactor Core");
            addBlock(ModBlocks.CONTROL_ROD_BLOCK,      "Control Rod Block");
            addBlock(ModBlocks.COOLING_PIPE,           "Cooling Pipe");
            addBlock(ModBlocks.LEAD_BLOCK,             "Lead Block");
            addBlock(ModBlocks.WASTE_CONTAINER,        "Waste Container");
            addBlock(ModBlocks.ENRICHED_URANIUM_BLOCK, "Enriched Uranium Block");

            addBlock(ModBlocks.ENERGY_CABLE,           "Energy Cable");
            addBlock(ModBlocks.ENERGY_BATTERY,         "Battery Block");

            addBlock(ModBlocks.REACTOR_CASING,                  "Reactor Casing");
            addBlock(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER,   "Multiblock Reactor");
            addBlock(ModBlocks.REACTOR_BUILDER_CONTROLLER,      "Reactor Builder Controller");

            add("akw.builder.started", "Build started: %s blocks remaining.");
            add("akw.builder.paused", "Build paused: %s of %s blocks placed.");
            add("akw.builder.blocked", "Build site blocked at %s/%s/%s.");
            add("akw.builder.missing_materials",
                    "Missing material: %s casing, %s core, %s controller.");
            add("akw.builder.missing_energy", "%s FE still required for construction.");
            add("akw.builder.robot", "Reactor Builder Robot");
            add("akw.builder.status.idle", "Ready");
            add("akw.builder.status.paused", "Paused");
            add("akw.builder.status.building", "Building");
            add("akw.builder.status.blocked", "Blocked");
            add("akw.builder.status.materials", "Missing material");
            add("akw.builder.status.energy", "Missing energy");
            add("akw.builder.status.complete", "Reactor construction complete");
            add("akw.builder.status.invalid", "Completed reactor is invalid");

            add("effect.akw.radiation", "Radiation");
            add("akw.multiblock.assembled",   "Reactor assembled: %s cores, %s connected cooling pipes.");
            add("akw.multiblock.disassembled","Reactor deactivated.");
            add("akw.multiblock.invalid",     "Invalid structure — check casing blocks.");
            addBlock(ModBlocks.REACTOR_ENERGY_PORT, "Reactor Energy Port");
            addBlock(ModBlocks.REACTOR_ITEM_PORT, "Reactor Item Port");
            addBlock(ModBlocks.DAMAGED_REACTOR_CORE, "Damaged Reactor Core");
            add("akw.item_port.mode.fuel_input", "Mode: Fuel input");
            add("akw.item_port.mode.waste_output", "Mode: Waste output");
            add("akw.item_port.mode.disabled", "Mode: Disabled");

            add("akw.reactor.error.gap", "Gap in the casing at %s, %s, %s.");
            add("akw.reactor.error.foreign_block", "Invalid block at %s, %s, %s.");
            add("akw.reactor.error.no_core", "No reactor core found in the interior.");
            add("akw.reactor.error.no_energy_port", "No energy port in the casing.");
            add("akw.reactor.error.too_large", "Casing larger than 9×9×9 or not closed.");
            add("akw.reactor.error.disconnected_pipe", "Cooling pipe not connected to the casing at %s, %s, %s.");

            // Multiblock GUI (tabs + stats)
            add("akw.gui.tab.overview",    "Overview");
            add("akw.gui.tab.control",     "Control");
            add("akw.gui.tab.diagnostics", "Diagnostics");
            add("akw.gui.size",            "Size: %s×%s×%s");
            add("akw.gui.cores",           "Cores: %s");
            add("akw.gui.energy",          "FE: %s / %s");
            add("akw.gui.production",      "Output: %s FE/t");
            add("akw.gui.cooling",         "Cooling: %s HU/t");
            add("akw.gui.heat",            "Heat: %s / %s");
            add("akw.gui.status",             "Status: %s");
            add("akw.gui.status.unassembled", "Not assembled — see Diagnostics tab.");
            add("akw.reactor.status.unassembled", "Not assembled");
            add("akw.reactor.status.offline",     "Ready");
            add("akw.reactor.status.starting",    "Starting up…");
            add("akw.reactor.status.running",     "Running");
            add("akw.reactor.status.scram",       "SCRAM — decay heat");
            add("akw.reactor.status.cooldown",    "Cooling down");
            add("akw.reactor.status.damaged",     "DAMAGED — repair cores");
            add("akw.gui.control_rods",    "Control rods: %s%%");
            add("akw.gui.enabled.on",      "Reactor: ON");
            add("akw.gui.enabled.off",     "Reactor: OFF");
            add("akw.gui.shutdown_temp",   "Shutdown: %s%%");
            add("akw.gui.no_errors",       "No errors.");
            add("akw.gui.more_errors",     "… and %s more errors.");
            add("akw.gui.layer",           "Layer %s/%s");
            add("akw.gui.safety.on",       "Failsafe: ON (no explosion)");
            add("akw.gui.safety.off",      "Failsafe: OVERRIDDEN ⚠");
            add("akw.damaged_core.repaired", "Core repaired.");
            add("akw.damaged_core.too_hot",  "Reactor still too hot to repair.");

            // Redstone modes
            add("akw.redstone_mode.ignored",       "Ignored");
            add("akw.redstone_mode.high_enables",  "Signal ON → active");
            add("akw.redstone_mode.high_disables", "Signal ON → pause");
            add("akw.redstone_mode.emergency_stop","Signal ON → SCRAM");

            // Comparator modes
            add("akw.comparator_mode.energy",      "Comparator: Energy");
            add("akw.comparator_mode.temperature", "Comparator: Temperature");
            add("akw.comparator_mode.fuel",        "Comparator: Fuel");
            add("akw.comparator_mode.waste",       "Comparator: Waste");

            // Sound subtitles
            add("subtitles.akw.reactor_ambient",  "Reactor running");
            add("subtitles.akw.reactor_alert",    "Reactor alert");
            add("subtitles.akw.reactor_meltdown", "Meltdown!");

            // Advancements
            add("advancements.akw.mine_uranium.title",   "Nuclear Beginnings");
            add("advancements.akw.mine_uranium.desc",    "Mine uranium and start your nuclear journey");
            add("advancements.akw.smelt_uranium.title",  "First Metal");
            add("advancements.akw.smelt_uranium.desc",   "Smelt raw uranium into an ingot");
            add("advancements.akw.enrich.title",         "Enrichment");
            add("advancements.akw.enrich.desc",          "Produce enriched uranium");
            add("advancements.akw.fuel_rod.title",       "Fuel Rod Ready");
            add("advancements.akw.fuel_rod.desc",        "Craft a fuel rod");
            add("advancements.akw.first_reactor.title",  "First Reactor");
            add("advancements.akw.first_reactor.desc",   "Build your first nuclear reactor");
            add("advancements.akw.energy_online.title",  "Power Online");
            add("advancements.akw.energy_online.desc",   "Connect the reactor to the power grid");
            add("advancements.akw.elite_reactor.title",  "Elite Class");
            add("advancements.akw.elite_reactor.desc",   "Build an elite nuclear reactor");
            add("advancements.akw.fusion_reactor.title", "Nuclear Fusion");
            add("advancements.akw.fusion_reactor.desc",  "Harness the power of the sun");
            add("advancements.akw.multiblock.title",     "Multiblock Master");
            add("advancements.akw.multiblock.desc",      "Activate a multiblock reactor");
        }
    }
}
