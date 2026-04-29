# 数据提供者 / Data Providers

RegistrateLib 内置了自动数据生成系统。当你通过构建器注册对象时，它会自动收集数据生成任务，并在数据生成运行时统一输出。

RegistrateLib has a built-in automatic data generation system. When you register objects through builders, it automatically collects data generation tasks and outputs them uniformly during datagen runs.

---

## 提供者类型 / Provider Types

### 服务端提供者 / Server Providers

| 类型 / Type          | 类 / Class                                             | 描述 / Description                       |
|--------------------|-------------------------------------------------------|----------------------------------------|
| `DYNAMIC`          | `RegistrateDatapackProvider`                          | 动态数据包注册表 / Dynamic datapack registries |
| `DATA_MAP`         | `RegistrateDataMapProvider`                           | 数据映射 / Data maps                       |
| `RECIPE_RUNNER`    | `RegistrateRecipeRunner`                              | 配方（入口点）/ Recipes (entry point)         |
| `ADVANCEMENT`      | `RegistrateAdvancementProvider`                       | 进度 / Advancements                      |
| `LOOT`             | `RegistrateLootTableProvider`                         | 战利品表 / Loot tables                     |
| `BLOCK_TAGS`       | `RegistrateTagsProvider.IntrinsicImpl<Block>`         | 方块标签 / Block tags                      |
| `ITEM_TAGS`        | `RegistrateItemTagsProvider`                          | 物品标签 / Item tags                       |
| `FLUID_TAGS`       | `RegistrateTagsProvider.IntrinsicImpl<Fluid>`         | 流体标签 / Fluid tags                      |
| `ENTITY_TAGS`      | `RegistrateTagsProvider.IntrinsicImpl<EntityType<?>>` | 实体标签 / Entity type tags                |
| `ENCHANTMENT_TAGS` | `RegistrateTagsProvider.Impl<Enchantment>`            | 附魔标签 / Enchantment tags                |
| `GENERIC_SERVER`   | `RegistrateGenericProvider`                           | 通用服务端提供者 / Generic server provider     |

### 客户端提供者 / Client Providers

| 类型 / Type        | 类 / Class                   | 描述 / Description                   |
|------------------|-----------------------------|------------------------------------|
| `MODEL`          | `RegistrateModelProvider`   | 模型和方块状态 / Models & blockstates     |
| `LANG`           | `RegistrateLangProvider`    | 语言文件（en_us + en_ud）/ Lang files    |
| `GENERIC_CLIENT` | `RegistrateGenericProvider` | 通用客户端提供者 / Generic client provider |

---

## 子生成器类型 / Sub-generator Types

这些是更细粒度的生成器键，通常用于构建器的 `setData()` 方法。  
These are finer-grained generator keys, typically used in builder `.setData()`.

| 生成器 / Generator | 所属提供者 / Parent Provider | 用途 / Purpose                                   |
|-----------------|-------------------------|------------------------------------------------|
| `RECIPE`        | `RECIPE_RUNNER`         | 单个对象的配方生成 / Per-object recipe generation       |
| `BLOCKSTATE`    | `MODEL`                 | 单个对象的方块状态生成 / Per-object blockstate generation |
| `ITEM_MODEL`    | `MODEL`                 | 单个对象的物品模型生成 / Per-object item model generation |

---

## 构建器中集成数据生成 / Data Generation in Builders

大多数构建器提供了直接的方法来配置数据生成：  
Most builders provide direct methods to configure data generation:

```java
// 方块 — 方块状态、模型、战利品、配方、语言
// Block — blockstate, model, loot, recipe, lang
REGISTRATE.block("my_block", MyBlock::new)
    .blockstate(ctx -> { /* ... */ })          // 方块状态 / blockstate
    .loot((prov, block) -> { /* ... */ })       // 战利品表 / loot
    .recipe((ctx, prov) -> { /* ... */ })       // 配方 / recipe
    .lang("My Block")                           // 语言 / lang
    .defaultBlockstate()                        // 使用默认值 / use defaults
    .defaultLoot()
    .register();
```

---

## RegistrateLangProvider —— 语言提供者 / Lang Provider

自动生成 `en_us` 和 `en_ud`（上下颠倒）语言文件。  
Automatically generates `en_us` and `en_ud` (upside-down) lang files.

```java
// 通过 Registrate 添加语言条目 / Add lang entries via Registrate
REGISTRATE.addRawLang("key", "value");                    // 原始语言键 / raw lang key
REGISTRATE.addLang("tooltip", blockId, "My Tooltip");     // 工具提示 / tooltip
REGISTRATE.addLang("item", itemId, "suffix", "Name");     // 带后缀的物品名称 / item name with suffix

// 从构建器自动生成 / Auto-generated from builders
// 调用 .defaultLang() 或 .lang("name") 即可 / call .defaultLang() or .lang("name")
```

### 手动使用 / Manual Usage

```java
// 直接访问语言提供者 / Access the lang provider directly
REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
    provider.addBlock(MY_BLOCK);
    provider.addItem(MY_ITEM);
    provider.addBlockWithTooltip(MY_BLOCK, "block.mymod.my_block", "A wonderful block");
});
```

---

## RegistrateTagsProvider —— 标签提供者 / Tag Provider

标签管理可以通过构建器链式调用或在数据生成器中手动完成。  
Tag management can be done via builder chaining or manually in data generators.

### 通过构建器 / Via Builders

```java
REGISTRATE.block("my_block", Block::new)
    .tag(BlockTags.STAIRS, BlockTags.DRAGON_IMMUNE)
    .tag(BlockTags.WITHER_IMMUNE)
    .register();

REGISTRATE.item("my_item", Item::new)
    .tag(ItemTags.BEDS)
    .register();
```

### 手动使用 / Manual Usage

```java
REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, provider -> {
    provider.tag(ItemTags.STAIRS)
            .add(MY_BLOCK.getKey());
});
```

### 复制方块标签到物品标签 / Copy Block Tags to Item Tags

```java
// ItemTagsProvider 支持从方块标签复制到物品标签
// ItemTagsProvider supports copying block tags to item tags
REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, provider -> {
    provider.copy(BlockTags.STAIRS, ItemTags.STAIRS);
});
```

---

## RegistrateRecipeProvider —— 配方提供者 / Recipe Provider

在构建器中通过 `.recipe()` 添加配方：  
Add recipes in builders via `.recipe()`:

```java
REGISTRATE.block("my_block", MyBlock::new)
    .recipe((ctx, prov) -> {
        prov.shaped(RecipeCategory.MISC, ctx.getEntry())
            .pattern("DDD").pattern("DED").pattern("DDD")
            .define('D', Items.DIAMOND)
            .define('E', Items.EGG)
            .unlockedBy("has_egg", prov.has(Items.EGG))
            .save(prov);

        prov.food(DataIngredient.items(ctx), RecipeCategory.MISC,
                  CookingBookCategory.MISC, () -> Blocks.DIAMOND_BLOCK, 1f);
    })
    .register();
```

### 便捷方法 / Convenience Methods

| 方法 / Method                                                        | 描述 / Description                              |
|--------------------------------------------------------------------|-----------------------------------------------|
| `smelting(DataIngredient, RecipeCategory, NonNullSupplier, float)` | 烧炼配方 / Smelting recipe                        |
| `blasting(...)`                                                    | 高炉配方 / Blasting recipe                        |
| `smoking(...)`                                                     | 烟熏配方 / Smoking recipe                         |
| `campfire(...)`                                                    | 营火配方 / Campfire recipe                        |
| `stonecutting(...)`                                                | 切石机配方 / Stonecutting recipe                   |
| `smeltingAndBlasting(...)`                                         | 同时生成烧炼和高炉配方 / Both smelting & blasting        |
| `food(...)`                                                        | 食物配方（烧炼）/ Food cooking recipe                 |
| `storage(...)`                                                     | 存储压缩配方（3x3 或 2x2）/ Storage compression recipe |
| `singleItem(...)`                                                  | 单物品配方 / Single item recipe                    |
| `planks(...)`                                                      | 木板配方 / Planks recipe                          |
| `stairs(...)`                                                      | 楼梯配方 / Stairs recipe                          |
| `slab(...)`                                                        | 台阶配方 / Slab recipe                            |
| `fence(...)`                                                       | 栅栏配方 / Fence recipe                           |
| `fenceGate(...)`                                                   | 栅栏门配方 / Fence gate recipe                     |
| `wall(...)`                                                        | 墙配方 / Wall recipe                             |
| `door(...)`                                                        | 门配方 / Door recipe                             |
| `trapDoor(...)`                                                    | 活板门配方 / Trapdoor recipe                       |

---

## RegistrateLootTableProvider —— 战利品表提供者 / Loot Table Provider

### 通过构建器 / Via Builders

```java
REGISTRATE.block("my_block", MyBlock::new)
    .loot((prov, block) -> {
        prov.dropOther(block, Items.DIAMOND);
    })
    .register();

REGISTRATE.entity("my_entity", MyEntity::new, MobCategory.CREATURE)
    .loot((prov, type) -> {
        prov.add(type, LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .add(LootItem.lootTableItem(Items.DIAMOND)
                    .apply(SetItemCountFunction.setCount(
                        UniformGenerator.between(1, 3))))));
    })
    .register();
```

### 可用方法 / Available Methods

从 `RegistrateBlockLootTables`（继承自 `BlockLootSubProvider`）：  
From `RegistrateBlockLootTables` (extends `BlockLootSubProvider`):

| 方法 / Method                           | 描述 / Description                       |
|---------------------------------------|----------------------------------------|
| `dropSelf(Block)`                     | 掉落自身 / Drop itself                     |
| `dropOther(Block, ItemLike)`          | 掉落其他物品 / Drop other item               |
| `createSingleItemTable(ItemLike)`     | 单物品掉落表 / Single item drop table        |
| `createSilkTouchDispatchTable(Block)` | 精准采集掉落表 / Silk touch drop table        |
| `createSlabItemTable(Block)`          | 台阶掉落表（保证掉落 2 个）/ Slab drop table       |
| `createDoorTable(Block)`              | 门掉落表 / Door drop table                 |
| `createOreDrop(Block, ItemLike)`      | 矿石掉落 + 精准采集 / Ore drop with silk touch |

---

## RegistrateAdvancementProvider —— 进度提供者 / Advancement Provider

```java
REGISTRATE.addDataGenerator(ProviderType.ADVANCEMENT, adv -> {
    Advancement.Builder.advancement()
        .addCriterion("has_egg",
            InventoryChangeTrigger.TriggerInstance.hasItems(Items.EGG))
        .display(Items.EGG,
            adv.title(MOD_ID, "root", "Test Advancement"),
            adv.desc(MOD_ID, "root", "Get an egg."),
            Identifier.withDefaultNamespace(
                "textures/gui/advancements/backgrounds/stone.png"),
            AdvancementType.TASK, true, true, false)
        .save(adv, MOD_ID + ":root");
});
```

---

## RegistrateGenericProvider —— 通用提供者 / Generic Provider

用于自定义数据生成任务（服务端和客户端）。  
For custom data generation tasks (server and client).

### 服务端示例：自定义维度 / Server Example: Custom Dimension

```java
REGISTRATE.addDataGenerator(ProviderType.GENERIC_SERVER,
    provider -> provider.add(data -> {
        return new DatapackBuiltinEntriesProvider(
            data.output(), data.registries(),
            new RegistrySetBuilder()
                .add(Registries.DIMENSION_TYPE, context -> { /* ... */ })
                .add(Registries.LEVEL_STEM, context -> { /* ... */ }),
            Set.of(MOD_ID));
    }));
```

---

## 注册自定义提供者 / Registering Custom Providers

你可以通过 `ProviderType.registerServerData()` 和 `ProviderType.registerClientProvider()` 注册自己的提供者类型。

You can register your own provider types via `ProviderType.registerServerData()` and `ProviderType.registerClientProvider()`.

```java
// 注册自定义服务端数据提供者
// Register a custom server data provider
public static final ProviderType<MyCustomProvider> MY_PROVIDER =
    ProviderType.registerServerData("my_provider", MyCustomProvider::new);
```
