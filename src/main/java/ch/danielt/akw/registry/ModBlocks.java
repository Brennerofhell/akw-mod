package ch.danielt.akw.registry;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.block.DamagedReactorCoreBlock;
import ch.danielt.akw.block.EnergyBatteryBlock;
import ch.danielt.akw.block.EnergyCableBlock;
import ch.danielt.akw.block.MultiblockReactorControllerBlock;
import ch.danielt.akw.block.NuclearReactorBlock;
import ch.danielt.akw.block.ReactorBuilderControllerBlock;
import ch.danielt.akw.block.ReactorCasingBlock;
import ch.danielt.akw.block.ReactorEnergyPortBlock;
import ch.danielt.akw.block.ReactorItemPortBlock;
import ch.danielt.akw.block.WasteContainerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AkwMod.MOD_ID);
    private static final DeferredRegister.Items  ITEMS  = DeferredRegister.createItems(AkwMod.MOD_ID);

    /** Alle Reaktor-Typen (fuer BlockEntity-Typ und Kreativ-Tab). */
    public static final List<DeferredBlock<? extends Block>> REACTORS = new ArrayList<>();
    /** Alle einfachen Reaktor-Bausteine. */
    public static final List<DeferredBlock<? extends Block>> DECOR    = new ArrayList<>();

    // --- Erze ---
    public static final DeferredBlock<Block> URANIUM_ORE =
            registerWithItem("uranium_ore", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE =
            registerWithItem("deepslate_uranium_ore", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

    // --- Reaktoren ---
    public static final DeferredBlock<NuclearReactorBlock> NUCLEAR_REACTOR =
            registerReactor("nuclear_reactor", 100_000, 40, 512, 1600, 1_200, 6);
    public static final DeferredBlock<NuclearReactorBlock> ADVANCED_NUCLEAR_REACTOR =
            registerReactor("advanced_nuclear_reactor", 400_000, 120, 2048, 2000, 2_000, 14);
    public static final DeferredBlock<NuclearReactorBlock> ELITE_NUCLEAR_REACTOR =
            registerReactor("elite_nuclear_reactor", 1_600_000, 360, 8192, 2400, 3_200, 28);
    public static final DeferredBlock<NuclearReactorBlock> BREEDER_REACTOR =
            registerReactor("breeder_reactor", 800_000, 240, 4096, 2200, 2_400, 18);
    public static final DeferredBlock<NuclearReactorBlock> THORIUM_REACTOR =
            registerReactor("thorium_reactor", 600_000, 180, 3072, 2600, 2_200, 16);
    public static final DeferredBlock<NuclearReactorBlock> FUSION_REACTOR =
            registerReactor("fusion_reactor", 4_000_000, 1000, 32768, 1200, 4_000, 44);

    // --- Bausteine ---
    public static final DeferredBlock<Block> REACTOR_CORE =
            registerDecor("reactor_core", Block::new, metal().lightLevel(s -> 7));
    public static final DeferredBlock<Block> CONTROL_ROD_BLOCK =
            registerDecor("control_rod_block", Block::new, metal());
    public static final DeferredBlock<Block> COOLING_PIPE =
            registerDecor("cooling_pipe", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final DeferredBlock<Block> LEAD_BLOCK =
            registerDecor("lead_block", Block::new, metal());
    public static final DeferredBlock<WasteContainerBlock> WASTE_CONTAINER =
            registerDecor("waste_container", WasteContainerBlock::new, metal());
    public static final DeferredBlock<Block> ENRICHED_URANIUM_BLOCK =
            registerDecor("enriched_uranium_block", Block::new, metal().lightLevel(s -> 5));

    // --- Multiblock-System ---
    public static final DeferredBlock<ReactorCasingBlock> REACTOR_CASING =
            registerDecor("reactor_casing", ReactorCasingBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(5f, 1200f));
    public static final DeferredBlock<MultiblockReactorControllerBlock> MULTIBLOCK_REACTOR_CONTROLLER =
            registerWithItem("multiblock_reactor_controller", MultiblockReactorControllerBlock::new,
                    metal().lightLevel(s -> s.getValue(MultiblockReactorControllerBlock.LIT) ? 13 : 0));
    public static final DeferredBlock<ReactorBuilderControllerBlock> REACTOR_BUILDER_CONTROLLER =
            registerWithItem("reactor_builder_controller", ReactorBuilderControllerBlock::new,
                    metal().lightLevel(s -> s.getValue(ReactorBuilderControllerBlock.ACTIVE) ? 7 : 0));
    public static final DeferredBlock<ReactorEnergyPortBlock> REACTOR_ENERGY_PORT =
            registerWithItem("reactor_energy_port", ReactorEnergyPortBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(5f, 1200f));
    public static final DeferredBlock<ReactorItemPortBlock> REACTOR_ITEM_PORT =
            registerWithItem("reactor_item_port", ReactorItemPortBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(5f, 1200f));
    public static final DeferredBlock<DamagedReactorCoreBlock> DAMAGED_REACTOR_CORE =
            registerWithItem("damaged_reactor_core", DamagedReactorCoreBlock::new,
                    metal().lightLevel(s -> 3));

    // --- Energie-Infrastruktur ---
    public static final DeferredBlock<EnergyCableBlock> ENERGY_CABLE =
            registerWithItem("energy_cable", EnergyCableBlock::new, metal());
    public static final DeferredBlock<EnergyBatteryBlock> ENERGY_BATTERY =
            registerWithItem("energy_battery", EnergyBatteryBlock::new, metal());

    // --- Hilfsmethoden ---

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK);
    }

    private static DeferredBlock<NuclearReactorBlock> registerReactor(
            String name, int capacity, int genPerTick, int maxExtract,
            int burnTicks, int maxHeat, int heatPerTick) {
        DeferredBlock<NuclearReactorBlock> block = registerWithItem(name,
                props -> new NuclearReactorBlock(props, capacity, genPerTick, maxExtract,
                        burnTicks, maxHeat, heatPerTick),
                metal().lightLevel(s -> s.getValue(NuclearReactorBlock.LIT) ? 13 : 0));
        REACTORS.add(block);
        return block;
    }

    private static <B extends Block> DeferredBlock<B> registerDecor(
            String name, Function<BlockBehaviour.Properties, B> factory, BlockBehaviour.Properties props) {
        DeferredBlock<B> block = registerWithItem(name, factory, props);
        DECOR.add(block);
        return block;
    }

    private static <B extends Block> DeferredBlock<B> registerWithItem(
            String name, Function<BlockBehaviour.Properties, B> factory, BlockBehaviour.Properties props) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, props);
        ITEMS.registerSimpleBlockItem(name, block);
        return block;
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        AkwMod.LOGGER.info("[Atomkraftwerk] Bloecke registriert ({} Reaktoren, {} Bausteine).",
                REACTORS.size(), DECOR.size());
    }
}
