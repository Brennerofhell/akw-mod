package ch.danielt.akw;

import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItemGroups;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.registry.ModScreenHandlers;
import ch.danielt.akw.worldgen.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

/**
 * Haupt-Einstiegspunkt der Mod (Fabric {@link ModInitializer}, gemeinsame
 * Server-/Client-Seite). Wird beim Laden der Mod aufgerufen und registriert in
 * fester Reihenfolge alle Inhalte: Items, Blöcke, BlockEntities, ScreenHandler,
 * Kreativ-Tab und Weltgenerierung. Die Reihenfolge ist relevant — z. B. muss
 * {@link ModBlockEntities} vor der Energie-Lookup-Registrierung geladen sein.
 *
 * <p>Zuletzt wird {@link EnergyStorage#SIDED} für den Reaktor-BlockEntity-Typ
 * registriert, damit Reaktoren ihren FE-Speicher über alle Seiten anbieten.
 *
 * <p>Client-spezifisches Setup (GUI-Screens) erfolgt getrennt in {@link AkwClient}.
 */
public class AkwMod implements ModInitializer {
    public static final String MOD_ID = "akw";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerAll();
        ModBlocks.registerAll();
        ModEffects.registerAll();
        ModBlockEntities.registerAll();
        ModScreenHandlers.registerAll();
        ModItemGroups.registerAll();
        ModWorldGen.registerAll();

        // FE-Speicher ueber alle Seiten anbieten (Team Reborn Energy).
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> be.energyStorage, ModBlockEntities.NUCLEAR_REACTOR);
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> be.energyStorage, ModBlockEntities.ENERGY_CABLE);
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> be.energyStorage, ModBlockEntities.ENERGY_BATTERY);

        LOGGER.info("[Atomkraftwerk] Mod initialisiert.");
    }
}
