package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Registriert den Kreativ-Tab „Atomkraftwerk" und füllt ihn. Der Tab nutzt
 * Roh-Uran als Icon und listet Items, Erze sowie — über die Listen
 * {@link ModBlocks#REACTORS} und {@link ModBlocks#DECOR} — alle Reaktor-Typen
 * und Bausteine. Neue Reaktoren/Bausteine erscheinen dadurch automatisch im Tab,
 * sobald sie in {@link ModBlocks} registriert sind.
 *
 * <p>Zusätzlich werden alle Inhalte — wie bei den meisten Mods üblich — in die
 * passenden Vanilla-Tabs einsortiert (Zutaten, Naturblöcke, Funktionsblöcke,
 * Baublöcke), sodass man sie auch im Kontext der normalen Kreativ-Suche findet.
 */
public class ModItemGroups {
    public static final RegistryKey<ItemGroup> AKW_GROUP_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(AkwMod.MOD_ID, "akw"));

    public static void registerAll() {
        Registry.register(Registries.ITEM_GROUP, AKW_GROUP_KEY,
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ModItems.RAW_URANIUM))
                        .displayName(Text.translatable("itemgroup.akw"))
                        .build());

        // Eigener „Atomkraftwerk"-Tab: alles gesammelt.
        ItemGroupEvents.modifyEntriesEvent(AKW_GROUP_KEY).register(entries -> {
            entries.add(ModItems.RAW_URANIUM);
            entries.add(ModItems.URANIUM_INGOT);
            entries.add(ModItems.FUEL_ROD);
            entries.add(ModBlocks.URANIUM_ORE);
            entries.add(ModBlocks.DEEPSLATE_URANIUM_ORE);
            ModBlocks.REACTORS.forEach(entries::add);
            ModBlocks.DECOR.forEach(entries::add);
        });

        // Zusätzlich in die passenden Vanilla-Tabs (wie andere Mods).
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(ModItems.RAW_URANIUM);
            entries.add(ModItems.URANIUM_INGOT);
            entries.add(ModItems.FUEL_ROD);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> {
            entries.add(ModBlocks.URANIUM_ORE);
            entries.add(ModBlocks.DEEPSLATE_URANIUM_ORE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries ->
                ModBlocks.REACTORS.forEach(entries::add));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries ->
                ModBlocks.DECOR.forEach(entries::add));

        AkwMod.LOGGER.info("[Atomkraftwerk] Kreativ-Tab + Vanilla-Tabs registriert.");
    }
}
