# RegistrateLib [![Build Status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.tterrag.com%2Fjob%2FRegistrate%2Fjob%2F1.21%2F)](https://ci.tterrag.com/job/Registrate/job/1.21) [![License](https://img.shields.io/github/license/ModularMCLib/RegistrateLib?cacheSeconds=36000)](https://www.tldrlegal.com/l/mpl-2.0) [![Maven Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.tterrag.com%2Fcom%2Ftterrag%2Fregistrate%2FRegistrate%2Fmaven-metadata.xml)](https://maven.tterrag.com/com/tterrag/registrate/Registrate) ![Minecraft Version](https://img.shields.io/badge/minecraft-26.1.1-blue) [![Discord](https://img.shields.io/discord/175740881389879296?label=discord&logo=discord&color=7289da)](https://discord.gg/gZqYcEj)

A powerful wrapper for creating and registering objects in your mod.

## Fork Status

This repository is the `com.modularmc.registrate` fork maintained for the ModularMCLib ecosystem. It tracks the upstream Registrate design while targeting the modern NeoForge + Minecraft `26.1` toolchain and preserving a package namespace that can be safely embedded in downstream projects.

## Modern Development

- Use `Java 25` for local builds and IDE sync.
- Prefer the bundled IDEA run configurations: `Client`, `Server`, `Data Generation`, `Game Tests`, and `Game Tests (Client)`.
- Keep fluent registration APIs in `builders`, data generation orchestration in `providers`, public reusable helpers in `util`, and implementation plumbing in `internal`.
- Treat [`docs/architecture.md`](docs/architecture.md) as the source of truth for package responsibilities and extension points.
- See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the day-to-day development workflow.

## Code Classification

- `com.modularmc.registrate`: public API roots, minimal library bootstrap, and the central registration coordinator.
- `com.modularmc.registrate.builders`: the fluent DSL used by downstream mods to register content.
- `com.modularmc.registrate.providers`: data generation orchestration and provider lifecycle management.
- `com.modularmc.registrate.providers.generators`: blockstate, model, and recipe generator adapters.
- `com.modularmc.registrate.providers.loot`: loot-table focused provider wrappers.
- `com.modularmc.registrate.util`: public shared helpers that remain part of the reusable library surface.
- `com.modularmc.registrate.util.entry`: strongly-typed registry handles returned by builders.
- `com.modularmc.registrate.util.nullness`: null-safe functional helpers and package defaults.
- `com.modularmc.registrate.internal`: non-API implementation details extracted from the core runtime.
- `com.modularmc.registrate.internal.event`: one-shot event wiring used by builders and registration lifecycle hooks.
- `com.modularmc.registrate.internal.lifecycle`: per-instance lifecycle bridges that keep NeoForge event hookups centralized without collapsing multiple mods into one shared runtime.
- `com.modularmc.registrate.internal.util`: dist-gated execution and internal logging helpers.
- `com.modularmc.registrate.test.mod`: the integration-style sample mod where non-library showcase behavior should live.
- `com.modularmc.registrate.test.gametests`: executable validation scenarios for runtime behavior.
- `com.modularmc.registrate.test.meta`: maintenance utilities that keep generated bridge methods aligned with upstream APIs.

## Why Registrate?

- Allows you to organize your mod content however you like, rather than having pieces of each object defined in scattered places
- Simple fluent API
- Open to extension, build and register custom objects and data
- Automatic data generation with sane defaults
- Usable as a standalone NeoForge library mod and as a declared dependency for downstream mods

## How to Use

First, create a `Registrate` object which will be used across your entire project.

```java
public static final Registrate REGISTRATE = Registrate.create(MOD_ID);
```

Using a constant field is not necessary, it can be passed around and thrown away after registration is setup.

If declared static in your `@Mod` class, you must create the `Registrate` object lazily so it is not created too early during loading. This can be done easily like so:

```java
public static final NonNullSupplier<Registrate> REGISTRATE = NonNullSupplier.lazy(() -> Registrate.create(MOD_ID));
```

Next, begin adding objects.

If you have a block class such as

```java
public class MyBlock extends Block {

    public MyBlock(Block.Properties properties) {
        super(properties);
    }
    
    ...
}
```

then register it like so,

```java
public static final RegistryEntry<MyBlock> MY_BLOCK = REGISTRATE.block("my_block", MyBlock::new).register();
```

Registrate will create a block, with a default simple blockstate, model, loot table, and lang entry. However, all of these facets can be configured easily to use whatever custom data you may want. Example:

```java
public static final RegistryEntry<MyStairsBlock> MY_STAIRS = REGISTRATE.block("my_block", MyStairsBlock::new)
            .defaultItem()
            .tag(BlockTags.STAIRS)
            .blockstate(ctx -> ctx.getProvider()
                .stairsBlock(ctx.getEntry(), ctx.getProvider().modLoc(ctx.getName())))
            .lang("Special Stairs")
            .register();
```

This customized version will create a BlockItem (with its own default model and lang entry), add the block to a tag, configure the blockstate for stair properties, and add a custom localization.

To get an overview of the different APIs and methods, check out the [Javadocs](https://ci.tterrag.com/job/Registrate/job/1.21/javadoc/). For more advanced usage, read the [wiki](https://github.com/ModularMCLib/RegistrateLib/wiki) (WIP).

## Project Setup

For this `26.1` fork, the preferred integration model is to depend on Registrate as a normal NeoForge library mod and declare it as a required dependency in your own metadata. Bundling remains possible for tightly controlled distributions, but standalone dependency loading is the default posture for this branch.

Add the library to your Gradle dependencies:

```groovy
dependencies {
    implementation "com.modularmc.registrate:registrate:${registrate_version}"
}
```

Then declare `registratelib` as a required dependency in your `neoforge.mods.toml`:

```toml
[[dependencies.yourmodid]]
modId="registratelib"
type="required"
versionRange="[2.0.0,)"
ordering="AFTER"
side="BOTH"
```

If you intentionally embed the library instead of loading it as a separate mod, keep your packaging and metadata strategy explicit so downstream debugging still has a clear ownership boundary.
