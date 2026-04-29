package com.modularmc.registrate.internal;

import com.modularmc.registrate.internal.util.DebugMarkers;
import com.modularmc.registrate.internal.util.RegistrateLogger;
import com.modularmc.registrate.util.entry.RegistryEntry;
import com.modularmc.registrate.util.nullness.NonNullConsumer;
import com.modularmc.registrate.util.nullness.NonNullFunction;
import com.modularmc.registrate.util.nullness.NonNullSupplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class RegistrationTracker {

    private static final Logger LOGGER = RegistrateLogger.registration();

    private static final class Registration<R, T extends R> {

        private final Identifier identifier;
        private final ResourceKey<? extends Registry<R>> registryKey;
        private final NonNullSupplier<? extends T> creator;
        private final RegistryEntry<R, T> delegate;
        private final List<NonNullConsumer<? super T>> callbacks = new ArrayList<>();

        private Registration(
                             Identifier identifier,
                             ResourceKey<? extends Registry<R>> registryKey,
                             NonNullSupplier<? extends T> creator,
                             NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
            this.identifier = identifier;
            this.registryKey = registryKey;
            this.creator = creator.lazy();
            this.delegate = entryFactory.apply(DeferredHolder.create(registryKey, identifier));
        }

        private Identifier getIdentifier() {
            return identifier;
        }

        private RegistryEntry<R, T> getDelegate() {
            return delegate;
        }

        private void register(RegisterEvent event) {
            T entry = creator.get();
            event.register(registryKey, helper -> helper.register(identifier, entry));
            callbacks.forEach(callback -> callback.accept(entry));
            callbacks.clear();
        }

        private void addRegisterCallback(NonNullConsumer<? super T> callback) {
            Preconditions.checkNotNull(callback, "Callback must not be null");
            callbacks.add(callback);
        }
    }

    private final String modId;
    private final Table<ResourceKey<? extends Registry<?>>, String, Registration<?, ?>> registrations = HashBasedTable.create();
    private final Multimap<Pair<String, ResourceKey<? extends Registry<?>>>, NonNullConsumer<?>> pendingEntryCallbacks = HashMultimap.create();
    private final Multimap<ResourceKey<? extends Registry<?>>, Runnable> afterRegisterCallbacks = HashMultimap.create();
    private final Set<ResourceKey<? extends Registry<?>>> completedRegistrations = new HashSet<>();

    public RegistrationTracker(String modId) {
        this.modId = modId;
    }

    public void onRegister(RegisterEvent event, boolean skipErrors, boolean devEnvironment) {
        ResourceKey<? extends Registry<?>> registryKey = event.getRegistryKey();
        if (registryKey == null) {
            LOGGER.debug(
                    DebugMarkers.REGISTER,
                    RegistrateLogger.modMessage("Skipping registry event without a registry key"),
                    modId);
            return;
        }

        if (!pendingEntryCallbacks.isEmpty()) {
            pendingEntryCallbacks.asMap().forEach((entryKey, callbacks) -> LOGGER.warn(
                    RegistrateLogger.modMessage("Found {} unused register callback(s) for entry {} [{}]. Was the entry ever registered?"),
                    modId,
                    callbacks.size(),
                    entryKey.getLeft(),
                    entryKey.getRight().identifier()));
            pendingEntryCallbacks.clear();
            if (devEnvironment) {
                throw new IllegalStateException("Found unused register callbacks, see logs");
            }
        }

        Map<String, Registration<?, ?>> registrationsForType = registrations.row(registryKey);
        if (registrationsForType.isEmpty()) {
            return;
        }

        LOGGER.trace(
                DebugMarkers.REGISTER,
                RegistrateLogger.modMessage("Registering {} captured object(s) in {}"),
                modId,
                registrationsForType.size(),
                registryKey.identifier());
        for (var entry : registrationsForType.entrySet()) {
            Registration<?, ?> registration = entry.getValue();
            try {
                registration.register(event);
                LOGGER.trace(
                        DebugMarkers.REGISTER,
                        RegistrateLogger.modMessage("Registered {} in {}"),
                        modId,
                        registration.getIdentifier(),
                        registryKey.identifier());
            } catch (Exception exception) {
                if (skipErrors) {
                    LOGGER.error(
                            DebugMarkers.REGISTER,
                            RegistrateLogger.modMessage("Unexpected error while registering entry {} in {}"),
                            modId,
                            registration.getIdentifier(),
                            registryKey.identifier(),
                            exception);
                } else {
                    throw new RuntimeException(
                            "[%s] Unexpected error while registering entry %s in %s"
                                    .formatted(modId, registration.getIdentifier(), registryKey.identifier()),
                            exception);
                }
            }
        }
    }

    public void onRegisterLate(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> registryKey = event.getRegistryKey();
        if (registryKey == null) {
            return;
        }

        Collection<Runnable> callbacks = afterRegisterCallbacks.get(registryKey);
        callbacks.forEach(Runnable::run);
        callbacks.clear();
        completedRegistrations.add(registryKey);
    }

    public <R, T extends R> RegistryEntry<R, T> track(
                                                      String name,
                                                      ResourceKey<? extends Registry<R>> registryKey,
                                                      NonNullSupplier<? extends T> creator,
                                                      NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
        Registration<R, T> registration = new Registration<>(
                Identifier.fromNamespaceAndPath(modId, name),
                registryKey,
                creator,
                entryFactory);
        LOGGER.trace(
                DebugMarkers.REGISTER,
                RegistrateLogger.modMessage("Captured registration {} for registry {}"),
                modId,
                registration.getIdentifier(),
                registryKey.identifier());
        pendingEntryCallbacks.removeAll(Pair.of(name, registryKey)).forEach(callback -> {
            @SuppressWarnings("unchecked")
            NonNullConsumer<? super T> unsafeCallback = (NonNullConsumer<? super T>) callback;
            registration.addRegisterCallback(unsafeCallback);
        });
        registrations.put(registryKey, name, registration);
        return registration.getDelegate();
    }

    public <R, T extends R> void addRegisterCallback(
                                                     String name,
                                                     ResourceKey<? extends Registry<R>> registryKey,
                                                     NonNullConsumer<? super T> callback) {
        Registration<R, T> registration = getRegistrationUnchecked(name, registryKey);
        if (registration == null) {
            pendingEntryCallbacks.put(Pair.of(name, registryKey), callback);
            return;
        }
        registration.addRegisterCallback(callback);
    }

    public <R> void addAfterRegisterCallback(ResourceKey<? extends Registry<R>> registryKey, Runnable callback) {
        afterRegisterCallbacks.put((ResourceKey<? extends Registry<?>>) registryKey, callback);
    }

    public <R> boolean isRegistered(ResourceKey<? extends Registry<R>> registryKey) {
        return completedRegistrations.contains(registryKey);
    }

    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<R, T> get(String name, ResourceKey<? extends Registry<R>> registryKey) {
        return (RegistryEntry<R, T>) getRegistration(name, registryKey).getDelegate();
    }

    public <R, T extends R> Optional<RegistryEntry<R, T>> getOptional(
                                                                      String name,
                                                                      ResourceKey<? extends Registry<R>> registryKey) {
        Registration<R, T> registration = getRegistrationUnchecked(name, registryKey);
        return registration == null ? Optional.empty() : Optional.of(registration.getDelegate());
    }

    @SuppressWarnings("unchecked")
    public <R, T extends R> Collection<RegistryEntry<R, T>> getAll(ResourceKey<? extends Registry<R>> registryKey) {
        return registrations.row(registryKey)
                .values()
                .stream()
                .map(registration -> (RegistryEntry<R, T>) registration.getDelegate())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <R, T extends R> Registration<R, T> getRegistration(String name, ResourceKey<? extends Registry<R>> registryKey) {
        Registration<R, T> registration = (Registration<R, T>) registrations.get(registryKey, name);
        if (registration != null) {
            return registration;
        }
        throw new IllegalArgumentException("Unknown registration " + name + " for type " + registryKey.identifier());
    }

    @SuppressWarnings("unchecked")
    private <R, T extends R> Registration<R, T> getRegistrationUnchecked(
                                                                         String name,
                                                                         ResourceKey<? extends Registry<R>> registryKey) {
        return (Registration<R, T>) registrations.get(registryKey, name);
    }
}
