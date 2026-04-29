# 架构 / Architecture

## 概述 / Overview

RegistrateLib 是 Registrate 在 `com.modularmc.registrate` 命名空间下的维护分支，为 ModularMCLib 生态提供流式注册 API，将实现代码分离为注册、生成、运行时支持和测试维护等多个层次。
RegistrateLib is a maintained fork of Registrate for the `com.modularmc.registrate` namespace. It exposes a fluent registration API while keeping the implementation separated into registration, generation, runtime support, and test-maintenance layers.

## 包映射 / Package Map

- `com.modularmc.registrate`
  - 公开入口点 / Public entrypoints
  - `AbstractRegistrate` 是编排核心 / is the orchestration core
  - `Registrate` 是默认的公开实现 / is the default public implementation
  - `RegistrateLib` 是 NeoForge 库模组入口点 / is the NeoForge library entrypoint

- `com.modularmc.registrate.builders`
  - 面向用户的流式注册 DSL / Public-facing fluent registration DSL
  - 拥有对象构造规则和链式调用行为 / Owns object construction rules and builder chaining behavior

- `com.modularmc.registrate.providers`
  - 数据生成生命周期、提供者注册和分发 / Data generation lifecycle, provider registration, and provider dispatch

- `com.modularmc.registrate.providers.generators`
  - 配方、模型和方块状态的专用适配器 / Specialized adapters for recipes, models, and blockstates

- `com.modularmc.registrate.providers.loot`
  - 战利品表提供者封装与兼容辅助 / Loot-specific provider wrappers and compatibility helpers

- `com.modularmc.registrate.util`
  - 面向下游的共享运行时和数据辅助工具 / Shared downstream-facing runtime and data helpers

- `com.modularmc.registrate.util.entry`
  - 注册返回的强类型句柄 / Strongly-typed handles returned from registrations

- `com.modularmc.registrate.util.nullness`
  - 函数式接口与包级空值约定 / Functional interfaces and package-level nullness conventions

- `com.modularmc.registrate.internal`
  - 从核心提取的非公开状态跟踪器和实现辅助类型 / Non-public state trackers and implementation support types

- `com.modularmc.registrate.internal.event`
  - 用于将构建器回调桥接到 NeoForge mod 总线的一次性事件连接 / One-shot mod event wiring used to bridge builder callbacks onto the NeoForge mod bus

- `com.modularmc.registrate.internal.lifecycle`
  - 实例级生命周期桥接，集中处理 `RegisterEvent`、创造模式标签页和数据生成总线连接 / Per-instance lifecycle bridges that centralize event wiring

- `com.modularmc.registrate.internal.util`
  - 分发限制执行与内部调试日志辅助 / Dist-gated execution and internal debug logging helpers

- `com.modularmc.registrate.test.mod`
  - 用作集成测试的示例模组 / Sample mod used as an integration harness

- `com.modularmc.registrate.test.gametests`
  - 可执行的运行时检查 / Executable runtime checks

- `com.modularmc.registrate.test.meta`
  - 用于生成的桥接方法和受保护 API 快照的维护工具 / Maintenance tooling for generated bridge methods and protected API snapshots

## 公开 API 边界 / Public API Boundary

将根包、`builders` 和 `util.entry` 视为面向下游的主要 API 表面。提供者和工具包也是可复用的，但它们主要用于支持 API，而非主要扩展点。
Treat the root package plus `builders` and `util.entry` as the main downstream-facing API surface. Provider and utility packages are reusable, but most of them exist to support the API rather than to be primary extension points.

## 放置指南 / Placement Guide

- 新的注册 DSL 行为放在 `builders`
  New registration DSL behavior belongs in `builders`
- 新的数据生成编排放在 `providers`
  New datagen orchestration belongs in `providers`
- 生成器专属辅助放在 `providers.generators` 或 `providers.loot`
  Generator-specific helpers belong in `providers.generators` or `providers.loot`
- 跨切面的运行时支持，仅当不特定于 builder 或 provider 时才放在 `util`
  Cross-cutting runtime support belongs in `util` only when it is not builder- or provider-specific
- 事件总线连接和其他仅实现辅助放在 `internal`
  Event-bus plumbing and other implementation-only helpers belong under `internal`
- 实例级 NeoForge 生命周期编排放在 `internal.lifecycle`
  Instance-scoped NeoForge lifecycle orchestration belongs in `internal.lifecycle`
- 仅测试用的框架放在 `src/test/java`
  Test-only scaffolding belongs under `src/test/java`

## 现代化原则 / Modernization Principles

- 偏好明确的包所有权而非便利放置
  Prefer explicit package ownership over convenience placement
- 保持公开入口点小巧，将复杂性推入专注的层次
  Keep public entrypoints small and push complexity into focused layers
- 对下游模组预期集成的扩展点进行文档化
  Document extension points where downstream mods are expected to integrate
- 在此开发分支上，优先采用 26.1 原生结构而非保持遗留兼容性
  Favor 26.1-native structure over preserving legacy compatibility on this dev branch
