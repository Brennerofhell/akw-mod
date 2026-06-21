package ch.danielt.akw;

import ch.danielt.akw.registry.ModScreenHandlers;
import ch.danielt.akw.screen.NuclearReactorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

/**
 * Client-seitiger Einstiegspunkt ({@link ClientModInitializer}). Läuft nur auf
 * der physischen Client-Seite und verknüpft die rein visuellen Bestandteile, die
 * es auf einem Server nicht gibt — hier die Zuordnung des Reaktor-ScreenHandlers
 * zu seinem {@link NuclearReactorScreen}. Spiel-/Datenlogik gehört in {@link AkwMod}.
 */
public class AkwClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.NUCLEAR_REACTOR, NuclearReactorScreen::new);
        // MULTIBLOCK_REACTOR hat Typ ScreenHandlerType<NuclearReactorScreenHandler> → direkt kompatibel
        HandledScreens.register(ModScreenHandlers.MULTIBLOCK_REACTOR, NuclearReactorScreen::new);
    }
}
