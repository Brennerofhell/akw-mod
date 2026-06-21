package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent REACTOR_AMBIENT  = register("reactor_ambient");
    public static final SoundEvent REACTOR_ALERT    = register("reactor_alert");
    public static final SoundEvent REACTOR_MELTDOWN = register("reactor_meltdown");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(AkwMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerAll() {
        AkwMod.LOGGER.info("[Atomkraftwerk] Sounds registriert.");
    }
}
