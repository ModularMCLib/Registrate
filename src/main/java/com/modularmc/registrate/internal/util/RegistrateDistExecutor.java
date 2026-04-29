package com.modularmc.registrate.internal.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

public final class RegistrateDistExecutor {

    private RegistrateDistExecutor() {}

    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (dist == FMLEnvironment.getDist()) {
            toRun.get().run();
        }
    }
}
