package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AkwMod.MOD_ID);

    public static final DeferredItem<Item> RAW_URANIUM      = ITEMS.registerSimpleItem("raw_uranium");
    public static final DeferredItem<Item> URANIUM_INGOT    = ITEMS.registerSimpleItem("uranium_ingot");
    public static final DeferredItem<Item> ENRICHED_URANIUM = ITEMS.registerSimpleItem("enriched_uranium");
    public static final DeferredItem<Item> FUEL_ROD         = ITEMS.registerSimpleItem("fuel_rod");
    public static final DeferredItem<Item> SPENT_FUEL_ROD   = ITEMS.registerSimpleItem("spent_fuel_rod");
    public static final DeferredItem<Item> REACTOR_WRENCH   = ITEMS.registerSimpleItem("reactor_wrench");

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        AkwMod.LOGGER.info("[Atomkraftwerk] Items registriert.");
    }
}
