package ch.danielt.akw.datagen;

import ch.danielt.akw.block.NuclearReactorBlock;
import ch.danielt.akw.registry.ModBlocks;
import ch.danielt.akw.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {
        // Einfache Würfel-Blöcke
        gen.registerSimpleCubeAll(ModBlocks.URANIUM_ORE);
        gen.registerSimpleCubeAll(ModBlocks.DEEPSLATE_URANIUM_ORE);
        gen.registerSimpleCubeAll(ModBlocks.REACTOR_CORE);
        gen.registerSimpleCubeAll(ModBlocks.CONTROL_ROD_BLOCK);
        gen.registerSimpleCubeAll(ModBlocks.COOLING_PIPE);
        gen.registerSimpleCubeAll(ModBlocks.LEAD_BLOCK);
        gen.registerSimpleCubeAll(ModBlocks.WASTE_CONTAINER);
        gen.registerSimpleCubeAll(ModBlocks.ENRICHED_URANIUM_BLOCK);

        // Reaktoren: orientable (FACING) + LIT-Zustand
        for (Block block : ModBlocks.REACTORS) {
            registerReactor(gen, block);
        }
    }

    private void registerReactor(BlockStateModelGenerator gen, Block block) {
        String id = Registries.BLOCK.getId(block).getPath();

        // Modell aus/an
        Identifier offModel = Models.ORIENTABLE.upload(block,
                new TextureMap()
                        .put(TextureKey.TOP,   Identifier.of("akw", "block/" + id + "_top"))
                        .put(TextureKey.FRONT, Identifier.of("akw", "block/" + id + "_front"))
                        .put(TextureKey.SIDE,  Identifier.of("akw", "block/" + id + "_side")),
                gen.modelCollector);

        Identifier onModel = Models.ORIENTABLE.upload(block, "_on",
                new TextureMap()
                        .put(TextureKey.TOP,   Identifier.of("akw", "block/" + id + "_top"))
                        .put(TextureKey.FRONT, Identifier.of("akw", "block/" + id + "_front_on"))
                        .put(TextureKey.SIDE,  Identifier.of("akw", "block/" + id + "_side")),
                gen.modelCollector);

        // Blockstate: 4 Richtungen × 2 LIT-Zustände
        gen.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(block,
                        BlockStateVariantMap.create(NuclearReactorBlock.FACING, NuclearReactorBlock.LIT)
                                .register(Direction.NORTH, false,
                                        BlockStateVariant.create().put(VariantSettings.MODEL, offModel))
                                .register(Direction.EAST,  false,
                                        BlockStateVariant.create().put(VariantSettings.MODEL, offModel)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                                .register(Direction.SOUTH, false,
                                        BlockStateVariant.create().put(VariantSettings.MODEL, offModel)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                                .register(Direction.WEST,  false,
                                        BlockStateVariant.create().put(VariantSettings.MODEL, offModel)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R270))
                                .register(Direction.NORTH, true,
                                        BlockStateVariant.create().put(VariantSettings.MODEL, onModel))
                                .register(Direction.EAST,  true,
                                        BlockStateVariant.create().put(VariantSettings.MODEL, onModel)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                                .register(Direction.SOUTH, true,
                                        BlockStateVariant.create().put(VariantSettings.MODEL, onModel)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                                .register(Direction.WEST,  true,
                                        BlockStateVariant.create().put(VariantSettings.MODEL, onModel)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R270))
                ));

        // Item-Modell erbt vom Block-Modell (aus-Zustand)
        gen.registerParentedItemModel(block, offModel);
    }

    @Override
    public void generateItemModels(ItemModelGenerator gen) {
        gen.register(ModItems.RAW_URANIUM,   Models.GENERATED);
        gen.register(ModItems.URANIUM_INGOT, Models.GENERATED);
        gen.register(ModItems.FUEL_ROD,      Models.GENERATED);
    }
}
