package com.modularmc.registrate.internal.lifecycle;

import com.modularmc.registrate.AbstractRegistrate;
import com.modularmc.registrate.internal.event.OneTimeEventReceiver;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Centralizes the per-instance NeoForge lifecycle wiring for a Registrate
 * owner.
 *
 * <p>
 * This keeps lifecycle listener registration easy to discover without
 * collapsing multiple mod instances into a shared global runtime.
 */
public final class RegistrateLifecycle {

    public interface Hooks {

        @Nullable
        IEventBus modEventBus();

        void setModEventBus(IEventBus bus);

        boolean doDatagen();

        void onRegister(RegisterEvent event);

        void onRegisterLate(RegisterEvent event);

        void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event);

        void onData(GatherDataEvent event);
    }

    private final AbstractRegistrate<?> owner;
    private final Hooks hooks;

    public RegistrateLifecycle(AbstractRegistrate<?> owner, Hooks hooks) {
        this.owner = owner;
        this.hooks = hooks;
    }

    public void register(IEventBus bus) {
        if (hooks.modEventBus() == null) {
            hooks.setModEventBus(bus);
        }

        Consumer<RegisterEvent> onRegister = hooks::onRegister;
        Consumer<RegisterEvent> onRegisterLate = hooks::onRegisterLate;
        bus.addListener(onRegister);
        bus.addListener(EventPriority.LOWEST, onRegisterLate);
        bus.addListener(hooks::onBuildCreativeModeTabContents);

        // Register events fire multiple times, so clean them up on common setup.
        OneTimeEventReceiver.addModListener(owner, FMLCommonSetupEvent.class, $ -> {
            OneTimeEventReceiver.unregister(owner, onRegister, RegisterEvent.class);
            OneTimeEventReceiver.unregister(owner, onRegisterLate, RegisterEvent.class);
        });

        if (hooks.doDatagen()) {
            OneTimeEventReceiver.addModListener(owner, GatherDataEvent.Client.class, hooks::onData);
        }
    }
}
