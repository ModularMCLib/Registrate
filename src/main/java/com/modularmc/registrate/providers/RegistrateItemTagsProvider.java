package com.modularmc.registrate.providers;

import com.modularmc.registrate.AbstractRegistrate;
import com.modularmc.registrate.providers.core.ProviderType;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegistrateItemTagsProvider extends RegistrateTagsProvider.IntrinsicImpl<Item> {

    private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
    private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap<>();

    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type, String name, PackOutput output, CompletableFuture<HolderLookup.Provider> registriesLookup, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
        super(owner, type, name, output, Registries.ITEM, registriesLookup, item -> BuiltInRegistries.ITEM.getResourceKey(item)
                .orElseThrow(() -> new IllegalStateException("Cannot generate tags for unregistered item: " + item)));
        this.blockTags = blockTags;
    }

    public void copy(TagKey<Block> sourceTag, TagKey<Item> targetTag) {
        tagsToCopy.put(sourceTag, targetTag);
    }

    @Override
    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return super.createContentsProvider().thenCombineAsync(blockTags, (lookupProvider, blockTagLookup) -> {
            tagsToCopy.forEach((sourceTag, targetTag) -> {
                TagBuilder tagBuilder = getOrCreateRawBuilder(targetTag);
                Optional<TagBuilder> sourceBuilder = blockTagLookup.apply(sourceTag);
                sourceBuilder
                        .orElseThrow(() -> new IllegalStateException("Missing block tag " + sourceTag.location()))
                        .build()
                        .forEach(tagBuilder::add);
            });
            return lookupProvider;
        });
    }
}
