package ch.danielt.akw;

import ch.danielt.akw.registry.ModScreenHandlers;
import ch.danielt.akw.screen.ModularReactorScreen;
import ch.danielt.akw.screen.NuclearReactorScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AkwMod.MOD_ID, value = Dist.CLIENT)
public class AkwClient {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModScreenHandlers.NUCLEAR_REACTOR.get(), NuclearReactorScreen::new);
        event.register(ModScreenHandlers.MULTIBLOCK_REACTOR.get(), ModularReactorScreen::new);
    }
}
