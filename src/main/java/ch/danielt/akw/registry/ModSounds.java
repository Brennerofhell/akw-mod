package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, AkwMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_AMBIENT =
            register("reactor_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_ALERT =
            register("reactor_alert");
    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_MELTDOWN =
            register("reactor_meltdown");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AkwMod.MOD_ID, name)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
        AkwMod.LOGGER.info("[Atomkraftwerk] Sounds registriert.");
    }
}
