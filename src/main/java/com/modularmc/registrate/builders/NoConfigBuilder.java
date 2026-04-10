package com.modularmc.registrate.builders;

import com.modularmc.registrate.AbstractRegistrate;
import com.modularmc.registrate.util.nullness.NonNullSupplier;
import com.modularmc.registrate.util.nullness.NonnullType;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class NoConfigBuilder<R, T extends R, P> extends AbstractBuilder<R, T, P, NoConfigBuilder<R, T, P>> {

    private final NonNullSupplier<T> factory;

    public NoConfigBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<? extends Registry<R>> registryType, NonNullSupplier<T> factory) {
        super(owner, parent, name, callback, registryType);
        this.factory = factory;
    }

    @Override
    protected @NonnullType T createEntry() {
        return factory.get();
    }
}
