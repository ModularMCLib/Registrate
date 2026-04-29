package com.modularmc.registrate;

import com.modularmc.registrate.internal.util.RegistrateLogger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class Registrate extends AbstractRegistrate<Registrate> {

    private static final Logger LOGGER = RegistrateLogger.core();

    /**
     * Create a new {@link Registrate} and register event listeners for registration and data generation. Used in lieu
     * of adding side-effects to constructor, so that alternate initialization
     * strategies can be done in subclasses.
     *
     * @param modid
     *              The mod ID for which objects will be registered
     * @return The {@link Registrate} instance
     */
    public static Registrate create(String modid) {
        var ret = new Registrate(modid);

        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modid)
                .map(ModContainer::getEventBus);

        modEventBus.ifPresentOrElse(ret::registerEventListeners, () -> LOGGER.fatal(
                RegistrateLogger.modMessage(
                        "Failed to locate the mod event bus during Registrate.create; registration listeners were not attached"),
                modid));

        return ret;
    }

    protected Registrate(String modid) {
        super(modid);
    }
}
