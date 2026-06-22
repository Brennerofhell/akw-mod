package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.screen.MultiblockReactorScreenHandler;
import ch.danielt.akw.screen.NuclearReactorScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModScreenHandlers {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, AkwMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<NuclearReactorScreenHandler>> NUCLEAR_REACTOR =
            MENU_TYPES.register("nuclear_reactor", () ->
                    IMenuTypeExtension.create((syncId, inv, buf) ->
                            new NuclearReactorScreenHandler(syncId, inv, buf.readBlockPos())));

    public static final DeferredHolder<MenuType<?>, MenuType<NuclearReactorScreenHandler>> MULTIBLOCK_REACTOR =
            MENU_TYPES.register("multiblock_reactor", () ->
                    IMenuTypeExtension.create((syncId, inv, buf) ->
                            new MultiblockReactorScreenHandler(syncId, inv, buf.readBlockPos())));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
        AkwMod.LOGGER.info("[Atomkraftwerk] ScreenHandler registriert.");
    }
}
