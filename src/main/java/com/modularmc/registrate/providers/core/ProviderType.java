package com.modularmc.registrate.providers.core;

import com.modularmc.registrate.AbstractRegistrate;
import com.modularmc.registrate.providers.RegistrateAdvancementProvider;
import com.modularmc.registrate.providers.RegistrateDataMapProvider;
import com.modularmc.registrate.providers.RegistrateDatapackProvider;
import com.modularmc.registrate.providers.RegistrateGenericProvider;
import com.modularmc.registrate.providers.RegistrateItemTagsProvider;
import com.modularmc.registrate.providers.RegistrateLangProvider;
import com.modularmc.registrate.providers.RegistrateTagsProvider;
import com.modularmc.registrate.providers.generators.*;
import com.modularmc.registrate.providers.loot.RegistrateLootTableProvider;
import com.modularmc.registrate.util.nullness.NonNullSupplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents a type of data that can be generated, and specifies a factory for the provider.
 * <p>
 * Used as a key for data generator callbacks.
 * <p>
 * This file also defines the built-in provider types, but third-party types can be created with
 * {@link #registerProvider(String, ProviderType)}.
 *
 * @param <T> The type of the provider
 */
@FunctionalInterface
public interface ProviderType<T extends RegistrateProvider> extends GeneratorType<T> {

    // SERVER DATA
    ProviderType<RegistrateDatapackProvider> DYNAMIC = registerServerData("dynamic", RegistrateDatapackProvider::new);
    ProviderType<RegistrateDataMapProvider> DATA_MAP = registerServerData("data_map", RegistrateDataMapProvider::new);
    ProviderType<RegistrateRecipeRunner> RECIPE_RUNNER = registerServerData("recipe_runner", RegistrateRecipeRunner::new);
    ProviderType<RegistrateAdvancementProvider> ADVANCEMENT = registerServerData("advancement", RegistrateAdvancementProvider::new);
    ProviderType<RegistrateLootTableProvider> LOOT = registerServerData("loot", RegistrateLootTableProvider::new);
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<Block>> BLOCK_TAGS = registerIntrinsicTag("tags/block", "blocks", Registries.BLOCK, BuiltInRegistries.BLOCK);
    ProviderType<RegistrateTagsProvider.Impl<Enchantment>> ENCHANTMENT_TAGS = registerDynamicTag("tags/enchantment", "enchantments", Registries.ENCHANTMENT);
    ProviderType<RegistrateItemTagsProvider> ITEM_TAGS = registerTag("tags/item", Registries.ITEM, c -> new RegistrateItemTagsProvider(c.parent(), c.type(), "items", c.output(), c.provider(), c.require(BLOCK_TAGS).contentsGetter()));
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<Fluid>> FLUID_TAGS = registerIntrinsicTag("tags/fluid", "fluids", Registries.FLUID, BuiltInRegistries.FLUID);
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<EntityType<?>>> ENTITY_TAGS = registerIntrinsicTag("tags/entity", "entity_types", Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE);
    ProviderType<RegistrateGenericProvider> GENERIC_SERVER = registerProvider("registrate_generic_server_provider", c -> new RegistrateGenericProvider(c.parent(), c.output(), c.provider(), LogicalSide.SERVER, c.type()));

    // CLIENT DATA
    ProviderType<RegistrateModelProvider> MODEL = registerClientProvider("model", () -> c -> new RegistrateModelProvider(c.parent(), c.output()));
    ProviderType<RegistrateLangProvider> LANG = registerClientProvider("lang", () -> c -> new RegistrateLangProvider(c.parent(), c.output()));
    ProviderType<RegistrateGenericProvider> GENERIC_CLIENT = registerClientProvider("registrate_generic_client_provider", () -> c -> new RegistrateGenericProvider(c.parent(), c.output(), c.provider(), LogicalSide.CLIENT, c.type()));

    GeneratorType<RegistrateRecipeProvider> RECIPE = RECIPE_RUNNER.createGenerator("recipe");
    GeneratorType<RegistrateBlockModelGenerator> BLOCKSTATE = MODEL.createGenerator("blockstate");
    GeneratorType<RegistrateItemModelGenerator> ITEM_MODEL = MODEL.createGenerator("item_model");

    record Context<T extends RegistrateProvider>(ProviderType<T> type, AbstractRegistrate<?> parent,
                                                 Map<ProviderType<?>, RegistrateProvider> existing,
                                                 PackOutput output,
                                                 CompletableFuture<HolderLookup.Provider> provider) {

        public <R extends RegistrateProvider> R require(ProviderType<R> other) {
            return other.cast(Objects.requireNonNull(
                    existing().get(other),
                    () -> "Missing provider dependency for " + RegistrateDataProvider.getTypeName(other)));
        }
    }

    T create(Context<T> context);

    @SuppressWarnings("unchecked")
    default T cast(RegistrateProvider provider) {
        return (T) provider;
    }

    default <R> GeneratorType<R> createGenerator(String type) {
        return new GeneratorType<>() {

            public String toString() {
                return type;
            }
        };
    }

    interface SimpleServerDataFactory<T extends RegistrateProvider> extends ProviderType<T> {

        T create(AbstractRegistrate<?> parent, PackOutput output, CompletableFuture<HolderLookup.Provider> provider);

        @Override
        default T create(Context<T> context) {
            return create(context.parent(), context.output(), context.provider());
        }

        default ProviderType<T> asProvider() {
            return this;
        }
    }

    static <T extends RegistrateProvider> ProviderType<T> registerServerData(String name, SimpleServerDataFactory<T> factory) {
        return registerProvider(name, factory.asProvider());
    }

    static <T extends RegistrateProvider> ProviderType<T> registerProvider(String name, ProviderType<T> type) {
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    static <T extends RegistrateProvider> ProviderType<T> registerClientProvider(String name, NonNullSupplier<ProviderType<T>> supplier) {
        ProviderType<T> type = DatagenModLoader.isRunningDataGen() ? supplier.get() : context -> {
            throw new IllegalStateException("Client datagen provider '" + name + "' is unavailable outside datagen");
        };
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    @SuppressWarnings("unchecked")
    static <T, R extends RegistrateTagsProvider<T>> ProviderType<R> registerTag(String name, ResourceKey<? extends Registry<T>> key, ProviderType<R> type) {
        if (RegistrateDataProvider.TAG_TYPES.containsKey(key)) {
            return (ProviderType<R>) RegistrateDataProvider.TAG_TYPES.get(key);
        }
        RegistrateDataProvider.TAG_TYPES.put(key, type);
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    static <T> ProviderType<RegistrateTagsProvider.IntrinsicImpl<T>> registerIntrinsicTag(String providerName, String typeName, ResourceKey<? extends Registry<T>> registryKey, Registry<T> registry) {
        return registerTag(providerName, registryKey, c -> new RegistrateTagsProvider.IntrinsicImpl<>(c.parent(), c.type(), typeName, c.output(), registryKey, c.provider(), keyExtractor(registry, typeName)));
    }

    static <T> ProviderType<RegistrateTagsProvider.Impl<T>> registerDynamicTag(String providerName, String typeName, ResourceKey<Registry<T>> registry) {
        return registerTag(providerName, registry, c -> new RegistrateTagsProvider.Impl<>(c.parent(), c.type(), typeName, c.output(), registry, c.provider()));
    }

    static <T extends RegistrateProvider> T create(ProviderType<T> type, AbstractRegistrate<?> parent, Map<ProviderType<?>, RegistrateProvider> existing, PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        return type.create(new Context<>(type, parent, existing, output, provider));
    }

    private static <T> Function<T, ResourceKey<T>> keyExtractor(Registry<T> registry, String typeName) {
        return value -> registry.getResourceKey(value)
                .orElseThrow(() -> new IllegalStateException("Cannot generate tags for unregistered " + typeName + " entry: " + value));
    }
}
