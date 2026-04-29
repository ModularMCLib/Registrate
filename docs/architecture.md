# Architecture

## Overview

`RegistrateLib` is a maintained fork of Registrate for the `com.modularmc.registrate` namespace. The project exposes a fluent registration API to downstream mods while keeping the implementation separated into registration, generation, runtime support, and test-maintenance layers.

## Package Map

- `com.modularmc.registrate`
  - Public entrypoints.
  - `AbstractRegistrate` is the orchestration core.
  - `Registrate` is the default public implementation.
  - `RegistrateLib` is a fork metadata/constants holder used by this repository.
- `com.modularmc.registrate.builders`
  - Public-facing fluent registration DSL.
  - Owns object construction rules and builder chaining behavior.
- `com.modularmc.registrate.providers`
  - Data generation lifecycle, provider registration, and provider dispatch.
- `com.modularmc.registrate.providers.generators`
  - Specialized adapters for recipes, models, and blockstates.
- `com.modularmc.registrate.providers.loot`
  - Loot-specific provider wrappers and compatibility helpers.
- `com.modularmc.registrate.util`
  - Shared runtime helpers for one-time events, distribution checks, sequencing, and misc support code.
- `com.modularmc.registrate.util.entry`
  - Strongly-typed handles returned from registrations.
- `com.modularmc.registrate.util.nullness`
  - Functional interfaces and package-level nullness conventions.
- `com.modularmc.registrate.test.mod`
  - Sample mod used as an integration harness.
- `com.modularmc.registrate.test.gametests`
  - Executable runtime checks.
- `com.modularmc.registrate.test.meta`
  - Maintenance tooling for generated bridge methods and protected API snapshots.

## Public API Boundary

Treat the root package plus `builders` and `util.entry` as the main downstream-facing API surface. Provider and utility packages are reusable, but most of them exist to support the API rather than to be primary extension points.

## Placement Guide

- New registration DSL behavior belongs in `builders`.
- New datagen orchestration belongs in `providers`.
- Generator-specific helpers belong in `providers.generators` or `providers.loot`.
- Cross-cutting runtime support belongs in `util` only when it is not builder- or provider-specific.
- Test-only scaffolding belongs under `src/test/java` even if it mirrors production APIs.

## Modernization Principles

- Prefer explicit package ownership over convenience placement.
- Keep public entrypoints small and push complexity into focused layers.
- Document extension points where downstream mods are expected to integrate.
- Preserve upstream compatibility where possible, but keep fork-specific behavior clearly identified.
