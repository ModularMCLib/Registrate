package com.modularmc.registrate.internal.util;

import com.modularmc.registrate.RegistrateLib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RegistrateLogger {

    private static final Logger ROOT = RegistrateLib.LOGGER;
    private static final Logger CORE = child("Core");
    private static final Logger DATAGEN = child("Datagen");
    private static final Logger REGISTRATION = child("Registration");

    private RegistrateLogger() {}

    public static Logger root() {
        return ROOT;
    }

    public static Logger core() {
        return CORE;
    }

    public static Logger datagen() {
        return DATAGEN;
    }

    public static Logger registration() {
        return REGISTRATION;
    }

    public static String modMessage(String message) {
        return "[{}] " + message;
    }

    private static Logger child(String name) {
        return LogManager.getLogger(ROOT.getName() + "." + name);
    }
}
