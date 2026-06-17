package ch.danielt.akw.worldgen;

import ch.danielt.akw.AkwMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public class ModWorldGen {
    /** Muss exakt dem Dateinamen der Placed Feature entsprechen (worldgen/placed_feature/uranium_ore.json). */
    public static final RegistryKey<PlacedFeature> URANIUM_ORE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(AkwMod.MOD_ID, "uranium_ore"));

    public static void registerAll() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                URANIUM_ORE_PLACED);
        AkwMod.LOGGER.info("[Atomkraftwerk] Worldgen registriert.");
    }
}
