package com.modularmc.registrate.providers.core;

import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;

public interface RegistrateProviderDelegate<R, T extends R> extends DataProvider {

    String getName();

    Identifier getId();

    T getEntry();
}
