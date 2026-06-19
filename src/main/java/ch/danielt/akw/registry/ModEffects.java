package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.effect.RadiationEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static final RegistryEntry<StatusEffect> RADIATION = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of(AkwMod.MOD_ID, "radiation"),
            new RadiationEffect());

    public static void registerAll() {
        AkwMod.LOGGER.info("[Atomkraftwerk] Effekte registriert.");
    }
}
