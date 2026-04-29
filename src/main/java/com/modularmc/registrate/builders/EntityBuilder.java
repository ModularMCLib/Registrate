package com.modularmc.registrate.builders;

import com.modularmc.registrate.AbstractRegistrate;
import com.modularmc.registrate.builders.base.AbstractBuilder;
import com.modularmc.registrate.builders.base.BuilderCallback;
import com.modularmc.registrate.internal.event.OneTimeEventReceiver;
import com.modularmc.registrate.internal.util.RegistrateDistExecutor;
import com.modularmc.registrate.providers.DataGenContext;
import com.modularmc.registrate.providers.RegistrateLangProvider;
import com.modularmc.registrate.providers.core.ProviderType;
import com.modularmc.registrate.providers.loot.RegistrateEntityLootTables;
import com.modularmc.registrate.providers.loot.RegistrateLootTableProvider.LootType;
import com.modularmc.registrate.util.entry.EntityEntry;
import com.modularmc.registrate.util.entry.RegistryEntry;
import com.modularmc.registrate.util.nullness.NonNullBiConsumer;
import com.modularmc.registrate.util.nullness.NonNullConsumer;
import com.modularmc.registrate.util.nullness.NonNullFunction;
import com.modularmc.registrate.util.nullness.NonNullSupplier;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.SpawnPlacements.SpawnPredicate;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A builder for entities, allows for customization of the {@link EntityType.Builder}, easy creation of spawn egg items,
 * and configuration of data associated with entities (loot tables, etc.).
 *
 * @param <T>
 *            The type of entity being built
 * @param <P>
 *            Parent object type
 */
public class EntityBuilder<T extends Entity, P> extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>> {

    /**
     * Create a new {@link EntityBuilder} and configure data. Used in lieu of adding side-effects to constructor, so
     * that alternate initialization strategies can be done in subclasses.
     * <p>
     * The entity will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * </ul>
     *
     * @param <T>
     *                       The type of the builder
     * @param <P>
     *                       Parent object type
     * @param owner
     *                       The owning {@link AbstractRegistrate} object
     * @param parent
     *                       The parent object
     * @param name
     *                       Name of the entry being built
     * @param callback
     *                       A callback used to actually register the built entry
     * @param factory
     *                       Factory to create the entity
     * @param classification
     *                       The {@link MobCategory} of the entity
     * @return A new {@link EntityBuilder} with reasonable default data generators.
     */
    public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory,
                                                                   MobCategory classification) {
        return new EntityBuilder<>(owner, parent, name, callback, factory, classification)
                .defaultLang();
    }

    private final NonNullSupplier<EntityType.Builder<T>> builder;

    private NonNullConsumer<EntityType.Builder<T>> builderCallback = $ -> {};

    private @Nullable NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T, ?>>> renderer;

    private boolean attributesConfigured, spawnConfigured;

    protected EntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
        super(owner, parent, name, callback, Registries.ENTITY_TYPE);
        this.builder = () -> EntityType.Builder.of(factory, classification);
    }

    /**
     * Modify the properties of the entity. Modifications are done lazily, but the passed function is composed with the
     * current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *             The action to perform on the properties
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> properties(NonNullConsumer<EntityType.Builder<T>> cons) {
        builderCallback = builderCallback.andThen(cons);
        return this;
    }

    /**
     * Register an {@link EntityRenderer} for this entity.
     * <p>
     *
     * @param renderer
     *                 A (server safe) supplier to an {@link EntityRendererProvider} that will provide this entity's
     *                 renderer
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> renderer(NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T, ?>>> renderer) {
        if (this.renderer == null && FMLEnvironment.getDist().isClient()) { // First call only
            RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerRenderer);
        }
        this.renderer = renderer;
        return this;
    }

    protected void registerRenderer() {
        OneTimeEventReceiver.addModListener(getOwner(), EntityRenderersEvent.RegisterRenderers.class, evt -> {
            var renderer = this.renderer;
            if (renderer != null) {
                try {
                    var provider = renderer.get();
                    evt.registerEntityRenderer(getEntry(), provider::apply);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to register renderer for Entity " + get().getId(), e);
                }
            }
        });
    }

    /**
     * Register a attributes for this entity. The entity must extend {@link LivingEntity}.
     * <p>
     * Cannot be called more than once per builder.
     *
     * @param attributes
     *                   A supplier to the attributes for this entity, usually of the form
     *                   {@code EntityClass::createAttributes}
     * @return this {@link EntityBuilder}
     * @throws IllegalStateException
     *                               When called more than once
     */
    @SuppressWarnings("unchecked")
    public EntityBuilder<T, P> attributes(Supplier<AttributeSupplier.Builder> attributes) {
        if (attributesConfigured) {
            throw new IllegalStateException("Cannot configure attributes more than once");
        }
        attributesConfigured = true;
        OneTimeEventReceiver.addModListener(getOwner(), EntityAttributeCreationEvent.class, e -> e.put((EntityType<LivingEntity>) getEntry(), attributes.get().build()));
        return this;
    }

    /**
     * Register a spawn placement for this entity. The entity must extend {@link Mob} and allow construction with a
     * {@code null} {@link Level}.
     * <p>
     * Cannot be called more than once per builder.
     *
     * @param type
     *                  The type of placement to use
     * @param heightmap
     *                  Which heightmap to use to choose placement locations
     * @param predicate
     *                  A predicate to check spawn locations for validity
     * @return this {@link EntityBuilder}
     * @throws IllegalStateException
     *                               When called more than once
     */
    @SuppressWarnings("unchecked")
    public EntityBuilder<T, P> spawnPlacement(SpawnPlacementType type, Heightmap.Types heightmap, SpawnPredicate<T> predicate, RegisterSpawnPlacementsEvent.Operation operation) {
        if (spawnConfigured) {
            throw new IllegalStateException("Cannot configure spawn placement more than once");
        }
        spawnConfigured = true;
        this.onRegister(t -> {
            OneTimeEventReceiver.addModListener(getOwner(), RegisterSpawnPlacementsEvent.class, e -> {
                e.register(t, type, heightmap, predicate, operation);
            });
        });
        return this;
    }

    /**
     * Create a spawn egg item for this entity and build it immediately.
     *
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> spawnEgg() {
        return spawnEgg($ -> {});
    }

    /**
     * Create a spawn egg item for this entity and expose its builder for further customization.
     *
     * <p>
     * The created egg uses the modern {@link net.minecraft.world.item.Item.Properties#spawnEgg(EntityType)} API and is
     * added to {@link CreativeModeTabs#SPAWN_EGGS} by default.
     *
     * @param consumer
     *                 A callback for customizing the nested egg {@link ItemBuilder}
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> spawnEgg(NonNullConsumer<ItemBuilder<SpawnEggItem, EntityBuilder<T, P>>> consumer) {
        NonNullSupplier<EntityType<T>> entityType = asSupplier();
        ItemBuilder<SpawnEggItem, EntityBuilder<T, P>> eggBuilder = getOwner()
                .item(this, getName() + "_spawn_egg", SpawnEggItem::new)
                .properties(properties -> properties.spawnEgg(entityType.get()))
                .model(() -> (ctx, prov) -> prov.createWithExistingModel(ctx.getEntry(), prov.mcLoc("item/template_spawn_egg")))
                .tab(CreativeModeTabs.SPAWN_EGGS);
        consumer.accept(eggBuilder);
        return eggBuilder.build();
    }

    /**
     * Assign the default translation, as specified by
     * {@link RegistrateLangProvider#getAutomaticName(NonNullSupplier, net.minecraft.resources.ResourceKey)}. This is
     * the default, so it is generally
     * not necessary to call, unless for undoing previous changes.
     *
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> defaultLang() {
        return lang(EntityType::getDescriptionId);
    }

    /**
     * Set the translation for this entity.
     *
     * @param name
     *             A localized English name
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> lang(String name) {
        return lang(EntityType::getDescriptionId, name);
    }

    /**
     * Configure the loot table for this entity. This is different than most data gen callbacks as the callback does not
     * accept a {@link DataGenContext}, but instead a
     * {@link RegistrateEntityLootTables}, for creating specifically entity loot tables.
     *
     * @param cons
     *             The callback which will be invoked during entity loot table creation.
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> loot(NonNullBiConsumer<RegistrateEntityLootTables, EntityType<T>> cons) {
        return setData(ProviderType.LOOT, (ctx, prov) -> prov.addLootAction(LootType.ENTITY, tb -> cons.accept(tb, ctx.getEntry())));
    }

    /**
     * Assign {@link TagKey}{@code s} to this entity. Multiple calls will add additional tags.
     *
     * @param tags
     *             The tags to assign
     * @return this {@link EntityBuilder}
     */
    @SafeVarargs
    public final EntityBuilder<T, P> tag(TagKey<EntityType<?>>... tags) {
        return tag(ProviderType.ENTITY_TAGS, tags);
    }

    @Override
    protected EntityType<T> createEntry() {
        EntityType.Builder<T> builder = this.builder.get();
        builderCallback.accept(builder);
        return builder.build(getResourceKey());
    }

    @Override
    protected RegistryEntry<EntityType<?>, EntityType<T>> createEntryWrapper(DeferredHolder<EntityType<?>, EntityType<T>> delegate) {
        return new EntityEntry<>(getOwner(), delegate);
    }

    @Override
    public EntityEntry<T> register() {
        return (EntityEntry<T>) super.register();
    }
}
