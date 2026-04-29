package com.modularmc.registrate;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Runtime entrypoint for the ModularMCLib variant of Registrate.
 *
 * <p>
 * This library is intended to load as its own NeoForge mod so downstream
 * mods can depend on it as a prerequisite during development. Integration
 * scenarios, sample registrations, and visual validation belong in the test
 * harness under {@code src/test}.
 */
@Mod(RegistrateLib.MOD_ID)
public final class RegistrateLib {

    public static final String MOD_ID = "registrate";

    public RegistrateLib(IEventBus modEventBus) {}
}
