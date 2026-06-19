package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item RAW_URANIUM = register("raw_uranium");
    public static final Item URANIUM_INGOT = register("uranium_ingot");
    public static final Item ENRICHED_URANIUM = register("enriched_uranium");
    public static final Item FUEL_ROD = register("fuel_rod");

    private static Item register(String name) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(AkwMod.MOD_ID, name));
        Item item = new Item(new Item.Settings().registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    /** Erzwingt das Laden der Klasse und damit die Feld-Registrierung. */
    public static void registerAll() {
        AkwMod.LOGGER.info("[Atomkraftwerk] Items registriert.");
    }
}
