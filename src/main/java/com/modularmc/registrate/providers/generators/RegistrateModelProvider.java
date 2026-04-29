package com.modularmc.registrate.providers.generators;

import com.modularmc.registrate.AbstractRegistrate;
import com.modularmc.registrate.providers.core.RegistrateProvider;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;

import com.mojang.serialization.JsonOps;

public class RegistrateModelProvider extends ModelProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> parent;

    public RegistrateModelProvider(AbstractRegistrate<?> parent, PackOutput p_388260_) {
        super(p_388260_, parent.getModid());
        this.parent = parent;
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var registrateBlockModels = new RegistrateBlockModelGenerator(parent, blockModels.blockStateOutput, blockModels.itemModelOutput, blockModels.modelOutput);
        registrateBlockModels.run();
        registrateBlockModels.seenBlockstates.forEach((block, dispatcher) -> validateBlockStateEncoding(block, dispatcher));
        new RegistrateItemModelGenerator(parent, itemModels.itemModelOutput, itemModels.modelOutput).run();
    }

    private static void validateBlockStateEncoding(net.minecraft.world.level.block.Block block, BlockStateModelDispatcher dispatcher) {
        try {
            BlockStateModelDispatcher.CODEC.encodeStart(JsonOps.INSTANCE, dispatcher);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to encode blockstate definition for " + block.builtInRegistryHolder().key().identifier(), ex);
        }
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }
}
