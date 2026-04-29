package com.modularmc.registrate.internal.datagen;

import net.minecraft.client.ClientBootstrap;

/**
 * Bridges the gap between NeoForge's server-side datagen entrypoints and
 * Minecraft's client-side model codec bootstrap.
 *
 * <p>
 * In 26.1, item model serialization depends on client bootstrap state even when
 * data generation is launched through the server datagen path. Triggering the
 * vanilla bootstrap here keeps Registrate's client-facing generators usable in
 * headless datagen runs.
 */
public final class RegistrateDatagenBootstrap {

    private RegistrateDatagenBootstrap() {}

    public static void bootstrapClientCodecs() {
        ClientBootstrap.bootstrap();
    }
}
