package com.modularmc.registrate;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static final String MOD_ID = "registratelib";
    public static final String MOD_NAME = "Registrate Lib";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public RegistrateLib(IEventBus modEventBus) {}
}
