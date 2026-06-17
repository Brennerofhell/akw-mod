package ch.danielt.akw;

import ch.danielt.akw.registry.ModScreenHandlers;
import ch.danielt.akw.screen.NuclearReactorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AkwClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.NUCLEAR_REACTOR, NuclearReactorScreen::new);
    }
}
