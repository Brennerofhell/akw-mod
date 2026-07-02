package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItemGroups {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AkwMod.MOD_ID);

    public static final ResourceKey<CreativeModeTab> AKW_GROUP_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                    ResourceLocation.fromNamespaceAndPath(AkwMod.MOD_ID, "akw"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AKW_TAB =
            CREATIVE_TABS.register("akw", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.RAW_URANIUM.get()))
                    .title(Component.translatable("itemgroup.akw"))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.RAW_URANIUM.get());
                        output.accept(ModItems.URANIUM_INGOT.get());
                        output.accept(ModItems.ENRICHED_URANIUM.get());
                        output.accept(ModItems.FUEL_ROD.get());
                        output.accept(ModItems.SPENT_FUEL_ROD.get());
                        output.accept(ModItems.REACTOR_WRENCH.get());
                        output.accept(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get());
                        output.accept(ModBlocks.REACTOR_BUILDER_CONTROLLER.get());
                        output.accept(ModBlocks.REACTOR_ENERGY_PORT.get());
                        output.accept(ModBlocks.REACTOR_ITEM_PORT.get());
                        output.accept(ModBlocks.URANIUM_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_URANIUM_ORE.get());
                        ModBlocks.REACTORS.forEach(b -> output.accept(b.get()));
                        ModBlocks.DECOR.forEach(b -> output.accept(b.get()));
                        output.accept(ModBlocks.ENERGY_CABLE.get());
                        output.accept(ModBlocks.ENERGY_BATTERY.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
        bus.addListener(ModItemGroups::addToVanillaTabs);
        AkwMod.LOGGER.info("[Atomkraftwerk] Kreativ-Tab + Vanilla-Tabs registriert.");
    }

    private static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.RAW_URANIUM);
            event.accept(ModItems.URANIUM_INGOT);
            event.accept(ModItems.ENRICHED_URANIUM);
            event.accept(ModItems.FUEL_ROD);
            event.accept(ModItems.SPENT_FUEL_ROD);
            event.accept(ModItems.REACTOR_WRENCH);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.REACTOR_CASING);
            event.accept(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER);
            event.accept(ModBlocks.REACTOR_BUILDER_CONTROLLER);
            event.accept(ModBlocks.REACTOR_ENERGY_PORT);
            event.accept(ModBlocks.REACTOR_ITEM_PORT);
            ModBlocks.REACTORS.forEach(event::accept);
        } else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModBlocks.URANIUM_ORE);
            event.accept(ModBlocks.DEEPSLATE_URANIUM_ORE);
        } else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            ModBlocks.DECOR.forEach(event::accept);
        } else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModBlocks.ENERGY_CABLE);
            event.accept(ModBlocks.ENERGY_BATTERY);
        }
    }
}
