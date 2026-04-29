package com.modularmc.registrate;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(RegistrateLib.MOD_ID)
public class RegistrateLib {

    public static final String MOD_ID = "registrate";
    public static final String MOD_NAME = "RegistrateLib";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Random RANDOM = new Random();

    public RegistrateLib(IEventBus bus) {

    }
}
