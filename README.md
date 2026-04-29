# RegistrateLib [![License](https://img.shields.io/github/license/ModularMCLib/RegistrateLib?cacheSeconds=36000)](https://www.tldrlegal.com/l/mpl-2.0) ![Minecraft Version](https://img.shields.io/badge/minecraft-26.1.1-blue)

<!-- [![Build Status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.tterrag.com%2Fjob%2FRegistrate%2Fjob%2F1.21%2F)](https://ci.tterrag.com/job/Registrate/job/1.21) -->
<!-- [![Maven Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.tterrag.com%2Fcom%2Ftterrag%2Fregistrate%2FRegistrate%2Fmaven-metadata.xml)](https://maven.tterrag.com/com/tterrag/registrate/Registrate) -->
<!-- Badges below are placeholders for this mod's own CI / Maven: -->
[![Build Status](#)]()
[![Maven Version](#)]()

一个用于在你的模组中创建和注册对象的强大封装。  
A powerful wrapper for creating and registering objects in your mod.

---

## 分支状态 / Fork Status

本仓库是 `com.modularmc.registrate` 命名空间下的 Registrate 分支，为 ModularMCLib 生态维护。它追踪上游 Registrate 的设计，同时面向现代 NeoForge + Minecraft `26.1` 工具链，并保留可安全嵌入下游项目的包命名空间。
This repository is the `com.modularmc.registrate` fork maintained for the ModularMCLib ecosystem. It tracks the upstream Registrate design while targeting the modern NeoForge + Minecraft `26.1` toolchain and preserving a package namespace that can be safely embedded in downstream projects.

---

## 快速链接 / Quick Links

<!-- - [Javadocs](https://ci.tterrag.com/job/Registrate/job/1.21/javadoc/) -->
- Javadocs（待定 / TBD）
- [维基 / Wiki](https://github.com/ModularMCLib/RegistrateLib/wiki)
- [贡献指南 / Contributing](CONTRIBUTING.md)

---

## 为什么使用 Registrate? / Why Registrate?

- 你可以按照自己的方式组织模组内容，无需将每个对象的定义分散在各处
  Allows you to organize your mod content however you like, rather than having pieces of each object defined in scattered places
- 简洁的流式 API / Simple fluent API
- 可扩展，支持构建和注册自定义对象及数据
  Open to extension, build and register custom objects and data
- 自动数据生成，提供合理的默认值
  Automatic data generation with sane defaults
- 既可作为独立的 NeoForge 库模组使用，也可作为下游模组的声明依赖使用
  Usable as a standalone NeoForge library mod and as a declared dependency for downstream mods

---

## 代码分类 / Code Classification

- `com.modularmc.registrate` — 公开 API 入口、最小化的库引导、注册协调中心 / public API roots, minimal library bootstrap, and the central registration coordinator
- `com.modularmc.registrate.builders` — 下游模组用于注册内容的流式 DSL / the fluent DSL used by downstream mods to register content
- `com.modularmc.registrate.providers` — 数据生成编排与提供者生命周期管理 / data generation orchestration and provider lifecycle management
- `com.modularmc.registrate.providers.generators` — 方块状态、模型和配方生成器适配器 / blockstate, model, and recipe generator adapters
- `com.modularmc.registrate.providers.loot` — 战利品表提供者封装 / loot-table focused provider wrappers
- `com.modularmc.registrate.util` — 公开的共享辅助工具 / public shared helpers that remain part of the reusable library surface
- `com.modularmc.registrate.util.entry` — 构建器返回的强类型注册句柄 / strongly-typed registry handles returned by builders
- `com.modularmc.registrate.util.nullness` — 空安全的函数式辅助与包级默认值 / null-safe functional helpers and package defaults
- `com.modularmc.registrate.internal` — 核心运行时的非 API 实现细节 / non-API implementation details extracted from the core runtime
- `com.modularmc.registrate.internal.event` — 用于构建器和注册生命周期钩子的一次性事件连接 / one-shot event wiring used by builders and registration lifecycle hooks
- `com.modularmc.registrate.internal.lifecycle` — 实例级生命周期桥接 / per-instance lifecycle bridges
- `com.modularmc.registrate.internal.util` — 分发限制执行与内部日志辅助 / dist-gated execution and internal logging helpers
- `com.modularmc.registrate.test.mod` — 集成测试示例模组 / the integration-style sample mod
- `com.modularmc.registrate.test.gametests` — 可执行的运行时验证场景 / executable validation scenarios for runtime behavior
- `com.modularmc.registrate.test.meta` — 维护工具，使生成的桥接方法与上游 API 保持一致 / maintenance utilities that keep generated bridge methods aligned with upstream APIs

---

## 使用方法 / How to Use

首先，创建一个 `Registrate` 对象，该对象将在整个项目中使用。  
First, create a `Registrate` object which will be used across your entire project.

```java
public static final Registrate REGISTRATE = Registrate.create(MOD_ID);
```

不一定要使用常量字段，可以在注册设置完成后传递或丢弃。  
Using a constant field is not necessary, it can be passed around and thrown away after registration is setup.

如果在 `@Mod` 类中声明为 `static`，必须延迟创建 `Registrate` 对象，避免在加载过程中过早创建。可以这样轻松实现：  
If declared static in your `@Mod` class, you must create the `Registrate` object lazily so it is not created too early during loading. This can be done easily like so:

```java
public static final NonNullSupplier<Registrate> REGISTRATE = NonNullSupplier.lazy(() -> Registrate.create(MOD_ID));
```

接下来，开始添加对象。  
Next, begin adding objects.

如果你有一个方块类：  
If you have a block class such as:

```java
public class MyBlock extends Block {

    public MyBlock(Block.Properties properties) {
        super(properties);
    }

    ...
}
```

可以这样注册：  
then register it like so:

```java
public static final RegistryEntry<MyBlock> MY_BLOCK = REGISTRATE.block("my_block", MyBlock::new).register();
```

Registrate 会创建一个方块，并生成默认的简单方块状态、模型、战利品表和语言条目。所有这些方面都可以轻松配置为自定义数据。例如：  
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

这个自定义版本会创建 BlockItem（含默认模型和语言条目）、将方块添加到标签、配置楼梯属性方块状态，并添加自定义本地化。  
This customized version will create a BlockItem (with its own default model and lang entry), add the block to a tag, configure the blockstate for stair properties, and add a custom localization.

<!-- 有关不同 API 和方法的概述，请查阅 [Javadocs](https://ci.tterrag.com/job/Registrate/job/1.21/javadoc/)。 -->
有关不同 API 和方法的概述，请查阅 Javadocs（待定）。更高级的用法请阅读 [维基](https://github.com/ModularMCLib/RegistrateLib/wiki)（编写中）。  
<!-- To get an overview of the different APIs and methods, check out the [Javadocs](https://ci.tterrag.com/job/Registrate/job/1.21/javadoc/). -->
To get an overview of the different APIs and methods, check out the Javadocs (TBD). For more advanced usage, read the [wiki](https://github.com/ModularMCLib/RegistrateLib/wiki) (WIP).

---

## 项目设置 / Project Setup

将库添加到 Gradle 依赖：  
Add the library to your Gradle dependencies:

```groovy
dependencies {
    implementation "com.modularmc.registrate:registratelib:${registratelib_version}"
}
```

然后在 `neoforge.mods.toml` 中将 `registratelib` 声明为必需依赖：  
Then declare `registratelib` as a required dependency in your `neoforge.mods.toml`:

```toml
[[dependencies.yourmodid]]
modId="registratelib"
type="required"
versionRange="[2.0.0,)"
ordering="AFTER"
side="BOTH"
```

---

## 开发指南 / Development

- 使用 `Java 25` 进行本地构建和 IDE 同步 / Use `Java 25` for local builds and IDE sync
- 优先使用捆绑的 IDEA 运行配置：`Client`、`Server`、`Data Generation`、`Game Tests`、`Game Tests (Client)`
  Prefer the bundled IDEA run configurations
- 流式注册 API 放在 `builders`，数据生成编排放在 `providers`，公开辅助工具放在 `util`，实现细节放在 `internal`
  Keep fluent registration APIs in `builders`, data generation orchestration in `providers`, public reusable helpers in `util`, and implementation plumbing in `internal`
- 查看 [`docs/architecture.md`](docs/architecture.md) 了解包职责和扩展点
  See [`docs/architecture.md`](docs/architecture.md) for package responsibilities and extension points

---

## 贡献 / Contributing

请阅读 [`CONTRIBUTING.md`](CONTRIBUTING.md) 了解如何为此项目做出贡献。  
Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) for details on how to contribute to this project.
