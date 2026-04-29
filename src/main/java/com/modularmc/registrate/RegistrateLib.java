package com.modularmc.registrate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * Fork metadata and shared development constants for the ModularMCLib variant
 * of Registrate.
 *
 * <p>
 * This class is intentionally <strong>not</strong> a NeoForge mod entrypoint.
 * Runtime test entrypoints live under the dedicated {@code testmod} sources.
 */
public final class RegistrateLib {

    public static final String MOD_ID = "registrate";
    public static final String MOD_NAME = "RegistrateLib";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Random RANDOM = new Random();

    private RegistrateLib() {}
}
