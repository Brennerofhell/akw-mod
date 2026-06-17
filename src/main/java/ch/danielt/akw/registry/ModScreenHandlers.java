package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.screen.NuclearReactorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Registriert die {@link ScreenHandlerType}s der Mod. Der Reaktor nutzt einen
 * {@link ExtendedScreenHandlerType}, der beim Öffnen die {@link BlockPos} des
 * Reaktors zum Client synchronisiert (via {@link BlockPos#PACKET_CODEC}), damit
 * der Client-Konstruktor des ScreenHandlers die passende BlockEntity auflösen kann.
 *
 * <p>Ein gemeinsamer ScreenHandler-Typ bedient alle Reaktor-Tiers.
 */
public class ModScreenHandlers {

    public static ScreenHandlerType<NuclearReactorScreenHandler> NUCLEAR_REACTOR;

    public static void registerAll() {
        NUCLEAR_REACTOR = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(AkwMod.MOD_ID, "nuclear_reactor"),
                new ExtendedScreenHandlerType<>(
                        NuclearReactorScreenHandler::new, BlockPos.PACKET_CODEC));
        AkwMod.LOGGER.info("[Atomkraftwerk] ScreenHandler registriert.");
    }
}
