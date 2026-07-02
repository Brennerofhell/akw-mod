package ch.danielt.akw;

import ch.danielt.akw.registry.ModBlockEntities;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModEffects;
import ch.danielt.akw.registry.ModItemGroups;
import ch.danielt.akw.registry.ModItems;
import ch.danielt.akw.registry.ModScreenHandlers;
import ch.danielt.akw.registry.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AkwMod.MOD_ID)
public class AkwMod {
    public static final String MOD_ID = "akw";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public AkwMod(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModScreenHandlers.register(modEventBus);
        ModItemGroups.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);

        LOGGER.info("[Atomkraftwerk] Mod initialisiert.");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.NUCLEAR_REACTOR.get(), (be, side) -> be.energyStorage);
        // Multiblock-Controller bewusst NICHT registriert: FE fliesst nur über Energie-Ports.
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.REACTOR_ENERGY_PORT.get(), (be, side) -> be.resolveControllerEnergy());
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.REACTOR_BUILDER_CONTROLLER.get(), (be, side) -> be.energyStorage);
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.ENERGY_CABLE.get(), (be, side) -> be.energyStorage);
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.ENERGY_BATTERY.get(), (be, side) -> be.energyStorage);

        LOGGER.info("[Atomkraftwerk] Energie-Capabilities registriert.");
    }
}
