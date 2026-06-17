package ch.danielt.akw;

import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItemGroups;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.worldgen.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AkwMod implements ModInitializer {
    public static final String MOD_ID = "akw";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerAll();
        ModBlocks.registerAll();
        ModItemGroups.registerAll();
        ModWorldGen.registerAll();
        LOGGER.info("[Atomkraftwerk] Mod initialisiert.");
    }
}
