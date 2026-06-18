package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.block.NuclearReactorBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModBlocks {

    /** Alle Reaktor-Typen (fuer BlockEntity-Typ und Kreativ-Tab). */
    public static final List<Block> REACTORS = new ArrayList<>();
    /** Alle einfachen Reaktor-Bausteine. */
    public static final List<Block> DECOR = new ArrayList<>();

    // --- Erze ---
    public static final Block URANIUM_ORE = register("uranium_ore",
            Block::new, AbstractBlock.Settings.copy(Blocks.IRON_ORE), true);
    public static final Block DEEPSLATE_URANIUM_ORE = register("deepslate_uranium_ore",
            Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE), true);

    // --- Reaktoren (Typ, Kapazitaet, FE/Tick, Abgabe, Brenndauer, maxHitze, Hitze/Tick) ---
    public static final Block NUCLEAR_REACTOR =
            registerReactor("nuclear_reactor", 100_000, 40, 512, 1600, 1_200, 6);
    public static final Block ADVANCED_NUCLEAR_REACTOR =
            registerReactor("advanced_nuclear_reactor", 400_000, 120, 2048, 2000, 2_000, 14);
    public static final Block ELITE_NUCLEAR_REACTOR =
            registerReactor("elite_nuclear_reactor", 1_600_000, 360, 8192, 2400, 3_200, 28);
    public static final Block BREEDER_REACTOR =
            registerReactor("breeder_reactor", 800_000, 240, 4096, 2200, 2_400, 18);
    public static final Block THORIUM_REACTOR =
            registerReactor("thorium_reactor", 600_000, 180, 3072, 2600, 2_200, 16);
    public static final Block FUSION_REACTOR =
            registerReactor("fusion_reactor", 4_000_000, 1000, 32768, 1200, 4_000, 44);

    // --- Bausteine ---
    public static final Block REACTOR_CORE = registerDecor("reactor_core",
            metal().luminance(s -> 7));
    public static final Block CONTROL_ROD_BLOCK = registerDecor("control_rod_block", metal());
    public static final Block COOLING_PIPE = registerDecor("cooling_pipe",
            AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK));
    public static final Block LEAD_BLOCK = registerDecor("lead_block", metal());
    public static final Block WASTE_CONTAINER = registerDecor("waste_container", metal());
    public static final Block ENRICHED_URANIUM_BLOCK = registerDecor("enriched_uranium_block",
            metal().luminance(s -> 5));

    private static AbstractBlock.Settings metal() {
        return AbstractBlock.Settings.copy(Blocks.IRON_BLOCK);
    }

    private static Block registerReactor(String name, int capacity, int genPerTick,
                                         int maxExtract, int burnTicks, int maxHeat, int heatPerTick) {
        Block registered = register(name,
                settings -> new NuclearReactorBlock(settings, capacity, genPerTick, maxExtract,
                        burnTicks, maxHeat, heatPerTick),
                metal().luminance(s -> s.get(NuclearReactorBlock.LIT) ? 13 : 0), true);
        REACTORS.add(registered);
        return registered;
    }

    private static Block registerDecor(String name, AbstractBlock.Settings settings) {
        Block registered = register(name, Block::new, settings, true);
        DECOR.add(registered);
        return registered;
    }

    private static Block register(String name, Function<AbstractBlock.Settings, Block> factory,
                                  AbstractBlock.Settings settings, boolean withItem) {
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AkwMod.MOD_ID, name));
        Block registered = Registry.register(Registries.BLOCK, blockKey,
                factory.apply(settings.registryKey(blockKey)));
        if (withItem) {
            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(AkwMod.MOD_ID, name));
            Registry.register(Registries.ITEM, itemKey,
                    new BlockItem(registered, new Item.Settings().registryKey(itemKey)));
        }
        return registered;
    }

    /** Erzwingt das Laden der Klasse und damit die Feld-Registrierung. */
    public static void registerAll() {
        AkwMod.LOGGER.info("[Atomkraftwerk] Bloecke registriert ({} Reaktoren, {} Bausteine).",
                REACTORS.size(), DECOR.size());
    }
}
