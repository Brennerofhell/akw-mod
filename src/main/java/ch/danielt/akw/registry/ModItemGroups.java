package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final RegistryKey<ItemGroup> AKW_GROUP_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(AkwMod.MOD_ID, "akw"));

    public static void registerAll() {
        Registry.register(Registries.ITEM_GROUP, AKW_GROUP_KEY,
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ModItems.RAW_URANIUM))
                        .displayName(Text.translatable("itemgroup.akw"))
                        .build());

        ItemGroupEvents.modifyEntriesEvent(AKW_GROUP_KEY).register(entries -> {
            entries.add(ModItems.RAW_URANIUM);
            entries.add(ModItems.URANIUM_INGOT);
            entries.add(ModItems.FUEL_ROD);
            entries.add(ModBlocks.URANIUM_ORE);
            entries.add(ModBlocks.DEEPSLATE_URANIUM_ORE);
        });

        AkwMod.LOGGER.info("[Atomkraftwerk] Kreativ-Tab registriert.");
    }
}
