package ch.danielt.akw;

import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItemGroups;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.registry.ModScreenHandlers;
import ch.danielt.akw.worldgen.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

public class AkwMod implements ModInitializer {
    public static final String MOD_ID = "akw";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerAll();
        ModBlocks.registerAll();
        ModBlockEntities.registerAll();
        ModScreenHandlers.registerAll();
        ModItemGroups.registerAll();
        ModWorldGen.registerAll();

        // Reaktoren geben FE ueber alle Seiten ab (Team Reborn Energy).
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> be.energyStorage, ModBlockEntities.NUCLEAR_REACTOR);

        LOGGER.info("[Atomkraftwerk] Mod initialisiert.");
    }
}
