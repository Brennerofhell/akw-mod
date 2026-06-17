package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item RAW_URANIUM = register("raw_uranium", new Item(new Item.Settings()));
    public static final Item URANIUM_INGOT = register("uranium_ingot", new Item(new Item.Settings()));
    public static final Item FUEL_ROD = register("fuel_rod", new Item(new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(AkwMod.MOD_ID, name), item);
    }

    /** Erzwingt das Laden der Klasse und damit die Feld-Registrierung. */
    public static void registerAll() {
        AkwMod.LOGGER.info("[Atomkraftwerk] Items registriert.");
    }
}
