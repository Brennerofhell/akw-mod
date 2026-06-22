package ch.danielt.akw.worldgen;

import ch.danielt.akw.AkwMod;

/**
 * NeoForge: Uranium ore worldgen is handled via JSON BiomeModifier.
 * See: src/main/resources/data/akw/neoforge/biome_modifier/add_uranium_ore.json
 */
public class ModWorldGen {
    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        // Worldgen via JSON BiomeModifier — no Java registration needed
        AkwMod.LOGGER.info("[Atomkraftwerk] Worldgen registriert.");
    }
}
