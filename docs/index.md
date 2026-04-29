# RegistrateLib 维基 / Wiki

## 什么是 RegistrateLib？ / What is RegistrateLib?

RegistrateLib 是一个强大的流式注册 API 封装，用于在 NeoForge 模组中创建和注册对象。它提供了一套流畅的链式调用 API，让你能够以声明式的方式组织模组内容，并自动处理数据生成、本地化和标签管理。

RegistrateLib is a powerful fluent registration API wrapper for creating and registering objects in NeoForge mods. It provides a smooth chain-call API that lets you organize mod content declaratively, with automatic data generation, localization, and tag management.

## 功能特性 / Features

- **流畅的流式 API** — 链式调用构建器，以声明式方式定义对象及其行为 / Chain-call builders for declarative object definition
- **自动数据生成** — 自动为方块、物品、流体等生成模型、方块状态、战利品表和配方 / Automatic model, blockstate, loot table, and recipe generation
- **智能本地化** — 自动为注册对象生成语言条目 / Automatic lang entry generation for registered objects
- **标签管理** — 链式 API 中直接支持方块、物品、流体和实体标签 / Built-in tag support for blocks, items, fluids, and entities
- **自定义注册表** — 支持创建自定义注册表和数据包注册表 / Custom registry and datapack registry creation
- **创造模式标签页** — 轻松将物品分配到创造模式标签页 / Easy creative mode tab assignment

## 快速开始 / Quick Start

```java
// 创建 Registrate 实例 / Create a Registrate instance
public static final Registrate REGISTRATE = Registrate.create(MOD_ID);

// 注册一个简单的方块 / Register a simple block
public static final BlockEntry<Block> MY_BLOCK = REGISTRATE.block("my_block", Block::new)
        .simpleItem()
        .register();

// 注册一个物品 / Register an item
public static final ItemEntry<Item> MY_ITEM = REGISTRATE.item("my_item", Item::new)
        .register();
```

## 文档目录 / Table of Contents

1. [快速入门](getting-started.md) — 安装、设置和你的第一个注册 / Installation, setup, and your first registration
2. [构建器 API](builders.md) — 所有构建器的详细 API 参考 / Detailed builder API reference
3. [注册条目](entries.md) — 注册返回的强类型句柄 / Strongly-typed registry entry handles
4. [数据提供者](providers.md) — 数据生成系统详解 / Data generation system in detail
5. [高级用法](advanced.md) — 自定义注册表、生命周期钩子等 / Custom registries, lifecycle hooks, and more

## 架构概述 / Architecture Overview

详细信息请查阅 [architecture.md](architecture.md)。  
See [architecture.md](architecture.md) for package responsibilities and extension points.
