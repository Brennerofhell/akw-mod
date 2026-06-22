package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.effect.RadiationEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, AkwMod.MOD_ID);

    public static final DeferredHolder<MobEffect, RadiationEffect> RADIATION =
            MOB_EFFECTS.register("radiation", RadiationEffect::new);

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
        AkwMod.LOGGER.info("[Atomkraftwerk] Effekte registriert.");
    }
}
