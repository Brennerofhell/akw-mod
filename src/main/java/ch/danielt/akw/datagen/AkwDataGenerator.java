package ch.danielt.akw.datagen;

import ch.danielt.akw.AkwMod;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

/**
 * Datengenerierung (NeoForge {@link GatherDataEvent}). Client-seitig werden
 * Modelle und Sprachdateien erzeugt, server-seitig Rezepte, Tags, Loot-Tabellen
 * und Advancements.
 */
@EventBusSubscriber(modid = AkwMod.MOD_ID)
public final class AkwDataGenerator {

    private AkwDataGenerator() {
    }

    @SubscribeEvent
    public static void onGatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModLanguageProvider.German::new);
        event.createProvider(ModLanguageProvider.English::new);
    }

    @SubscribeEvent
    public static void onGatherServerData(GatherDataEvent.Server event) {
        event.createProvider(ModRecipeProvider::new);
        event.createProvider(ModTagsProvider::new);
        event.createProvider((output, lookup) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                        ModLootTableProvider::new, LootContextParamSets.BLOCK)),
                lookup));
        event.createProvider((output, lookup) -> new AdvancementProvider(
                output, lookup, List.of(new ModAdvancementProvider())));
    }
}
