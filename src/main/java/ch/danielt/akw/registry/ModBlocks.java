package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block URANIUM_ORE = register("uranium_ore",
            new Block(AbstractBlock.Settings.copy(Blocks.IRON_ORE)), true);
    public static final Block DEEPSLATE_URANIUM_ORE = register("deepslate_uranium_ore",
            new Block(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE)), true);

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
        AkwMod.LOGGER.info("[Atomkraftwerk] Bloecke registriert.");
    }
}
