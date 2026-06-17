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
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {

    /** Alle Reaktor-Typen (fuer BlockEntity-Typ und Kreativ-Tab). */
    public static final List<Block> REACTORS = new ArrayList<>();
    /** Alle einfachen Reaktor-Bausteine. */
    public static final List<Block> DECOR = new ArrayList<>();

    // --- Erze ---
    public static final Block URANIUM_ORE = register("uranium_ore",
            new Block(AbstractBlock.Settings.copy(Blocks.IRON_ORE)), true);
    public static final Block DEEPSLATE_URANIUM_ORE = register("deepslate_uranium_ore",
            new Block(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE)), true);

    // --- Reaktoren (Typ, Kapazitaet, FE/Tick, Abgabe, Brenndauer) ---
    public static final Block NUCLEAR_REACTOR =
            registerReactor("nuclear_reactor", 100_000, 40, 512, 1600);
    public static final Block ADVANCED_NUCLEAR_REACTOR =
            registerReactor("advanced_nuclear_reactor", 400_000, 120, 2048, 2000);
    public static final Block ELITE_NUCLEAR_REACTOR =
            registerReactor("elite_nuclear_reactor", 1_600_000, 360, 8192, 2400);
    public static final Block BREEDER_REACTOR =
            registerReactor("breeder_reactor", 800_000, 240, 4096, 2200);
    public static final Block THORIUM_REACTOR =
            registerReactor("thorium_reactor", 600_000, 180, 3072, 2600);
    public static final Block FUSION_REACTOR =
            registerReactor("fusion_reactor", 4_000_000, 1000, 32768, 1200);

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
                                         int maxExtract, int burnTicks) {
        NuclearReactorBlock block = new NuclearReactorBlock(
                metal().luminance(s -> s.get(NuclearReactorBlock.LIT) ? 13 : 0),
                capacity, genPerTick, maxExtract, burnTicks);
        Block registered = register(name, block, true);
        REACTORS.add(registered);
        return registered;
    }

    private static Block registerDecor(String name, AbstractBlock.Settings settings) {
        Block registered = register(name, new Block(settings), true);
        DECOR.add(registered);
        return registered;
    }

    private static Block register(String name, Block block, boolean withItem) {
        Identifier id = Identifier.of(AkwMod.MOD_ID, name);
        Block registered = Registry.register(Registries.BLOCK, id, block);
        if (withItem) {
            Registry.register(Registries.ITEM, id, new BlockItem(registered, new Item.Settings()));
        }
        return registered;
    }

    /** Erzwingt das Laden der Klasse und damit die Feld-Registrierung. */
    public static void registerAll() {
        AkwMod.LOGGER.info("[Atomkraftwerk] Bloecke registriert ({} Reaktoren, {} Bausteine).",
                REACTORS.size(), DECOR.size());
    }
}
