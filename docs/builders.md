# 构建器 API / Builder API

RegistrateLib 使用**流式构建器模式**（Fluent Builder Pattern）来声明和管理对象注册。每个构建器都提供链式方法调用来配置对象的不同方面，最后通过 `register()` 完成注册。

RegistrateLib uses the **Fluent Builder Pattern** to declare and manage object registration. Each builder provides chainable methods to configure different aspects of the object, finalised with `register()`.

---

## 目录 / Table of Contents

1. [AbstractBuilder —— 所有构建器的基类](#abstractbuilder)
2. [BlockBuilder —— 方块构建器](#blockbuilder)
3. [ItemBuilder —— 物品构建器](#itembuilder)
4. [BlockEntityBuilder —— 方块实体构建器](#blockentitybuilder)
5. [EntityBuilder —— 实体构建器](#entitybuilder)
6. [FluidBuilder —— 流体构建器](#fluidbuilder)
7. [MenuBuilder —— 菜单构建器](#menubuilder)

---

## AbstractBuilder

所有构建器的抽象基类，提供通用的构建器方法。  
Abstract base class for all builders, providing common builder methods.

```java
public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>>
```

### 通用方法 / Common Methods

| 方法 / Method                                | 描述 / Description                                                                              |
|--------------------------------------------|-----------------------------------------------------------------------------------------------|
| `register()`                               | 完成注册，返回对应的 `RegistryEntry` / Finalise registration, returns the corresponding `RegistryEntry` |
| `tag(ProviderType, TagKey...)`             | 为对象添加标签（通用版）/ Add tags to the object (generic)                                                |
| `removeTag(ProviderType, TagKey...)`       | 移除对象的标签 / Remove tags from the object                                                         |
| `lang(NonNullFunction<T, String>, String)` | 设置自定义语言键 / Set a custom lang key                                                              |
| `asOptional()`                             | 将此构建器标记为可选的 / Mark this builder as optional                                                   |
| `getResourceKey()`                         | 获取此构建器创建的资源的 `ResourceKey` / Get the `ResourceKey` for the resource                           |

### Builder 接口方法 / Builder Interface Methods

以下方法在 `Builder<R, T, P, S>` 接口中定义，对所有构建器可用：  
These methods are defined in the `Builder<R, T, P, S>` interface and available on all builders:

| 方法 / Method                                     | 描述 / Description                                                       |
|-------------------------------------------------|------------------------------------------------------------------------|
| `setData(GeneratorType, NonNullBiConsumer)`     | 为此对象设置数据生成器 / Set a data generator for this object                     |
| `removeData(GeneratorType)`                     | 移除数据生成器 / Remove a data generator                                      |
| `addMiscData(GeneratorType, NonNullConsumer)`   | 添加全局数据生成器 / Add a global data generator                                |
| `dataMap(DataMapType, ...)`                     | 为此对象添加数据映射 / Add a data map value                                      |
| `onRegister(NonNullConsumer)`                   | 注册完成时的回调 / Callback when registration completes                        |
| `onRegisterAfter(ResourceKey, NonNullConsumer)` | 在指定注册表注册完成后的回调 / Callback after another registry finishes registration |
| `transform(NonNullFunction)`                    | 对构建器应用变换函数 / Apply a transform function to the builder                 |
| `build()`                                       | 返回父构建器（用于嵌套构建器链）/ Return to parent builder                             |

---

## BlockBuilder

用于注册方块。  
Used for registering blocks.

```java
public class BlockBuilder<T extends Block, P>
        extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>>
```

### 创建 / Creation

```java
// 由 Registrate 的 block() 方法创建 / Created via Registrate's block() method
registrate.block("my_block", MyBlock::new)
```

### 方法 / Methods

| 方法 / Method                                                          | 描述 / Description                                                                                         |
|----------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `properties(NonNullUnaryOperator<BlockBehaviour.Properties>)`        | 修改方块属性 / Modify block properties                                                                         |
| `initialProperties(NonNullSupplier<? extends Block>)`                | 从现有方块复制初始属性 / Copy initial properties from an existing block                                             |
| `simpleItem()`                                                       | 自动创建一个默认的 BlockItem / Automatically create a default BlockItem                                           |
| `item()`                                                             | 返回 `ItemBuilder<BlockItem, BlockBuilder<T,P>>`，用于自定义 BlockItem / Return ItemBuilder for custom BlockItem |
| `item(NonNullBiFunction)`                                            | 使用自定义工厂创建 BlockItem / Create BlockItem with a custom factory                                             |
| `simpleBlockEntity(BlockEntityFactory)`                              | 快速创建没有额外配置的方块实体 / Quickly create a block entity without extra configuration                              |
| `blockEntity(BlockEntityFactory)`                                    | 返回 `BlockEntityBuilder` 用于完全配置方块实体 / Return BlockEntityBuilder for full BE configuration                 |
| `color(NonNullSupplier<Supplier<List<BlockTintSource>>>)`            | 设置方块着色器 / Set block tint sources                                                                         |
| `defaultBlockstate()`                                                | 使用默认方块状态生成 / Use default blockstate generation                                                           |
| `blockstate(NonNullSupplier<NonNullBiConsumer>)`                     | 自定义方块状态生成 / Custom blockstate generation                                                                 |
| `defaultLang()`                                                      | 自动生成语言条目 / Auto-generate lang entry                                                                      |
| `lang(String)`                                                       | 自定义语言名称 / Custom lang name                                                                               |
| `defaultLoot()`                                                      | 使用默认战利品表生成 / Use default loot table generation                                                           |
| `loot(NonNullBiConsumer)`                                            | 自定义战利品表生成 / Custom loot table generation                                                                 |
| `recipe(NonNullBiConsumer)`                                          | 添加配方生成 / Add recipe generation                                                                           |
| `clientExtension(NonNullSupplier<Supplier<IClientBlockExtensions>>)` | 添加客户端扩展 / Add client block extensions                                                                    |
| `tag(TagKey<Block>...)`                                              | 添加方块标签 / Add block tags                                                                                  |
| `register()`                                                         | 返回 `BlockEntry<T>` / Returns `BlockEntry<T>`                                                             |

### 示例 / Example

```java
public static final BlockEntry<MyStairsBlock> MY_STAIRS = REGISTRATE
        .block("my_stairs", MyStairsBlock::new)
        .defaultItem()  // 或 .item() 进行自定义 / or .item() for custom
        .tag(BlockTags.STAIRS)
        .blockstate(ctx -> ctx.getProvider()
                .stairsBlock(ctx.getEntry(), ctx.getProvider().modLoc(ctx.getName())))
        .lang("Special Stairs")
        .register();
```

---

## ItemBuilder

用于注册物品。  
Used for registering items.

```java
public class ItemBuilder<T extends Item, P>
        extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>>
```

### 方法 / Methods

| 方法 / Method                                                         | 描述 / Description                           |
|---------------------------------------------------------------------|--------------------------------------------|
| `properties(NonNullUnaryOperator<Item.Properties>)`                 | 修改物品属性 / Modify item properties            |
| `initialProperties(NonNullSupplier<Item.Properties>)`               | 使用初始属性 / Use initial properties            |
| `tab(ResourceKey<CreativeModeTab>, ...)`                            | 添加到创造模式标签页 / Add to creative mode tab      |
| `removeTab(ResourceKey<CreativeModeTab>)`                           | 从标签页移除 / Remove from tab                   |
| `defaultModel()`                                                    | 使用默认物品模型 / Use default item model          |
| `model(NonNullSupplier<NonNullBiConsumer>)`                         | 自定义物品模型 / Custom item model                |
| `defaultLang()`                                                     | 自动生成语言条目 / Auto-generate lang entry        |
| `lang(String)`                                                      | 自定义语言名称 / Custom lang name                 |
| `recipe(NonNullBiConsumer)`                                         | 添加配方生成 / Add recipe                        |
| `burnTime(int)`                                                     | 设置燃料燃烧时间（tick）/ Set fuel burn time (ticks) |
| `compostable(float)`                                                | 设置堆肥机概率 / Set composter chance             |
| `clientExtension(NonNullSupplier<Supplier<IClientItemExtensions>>)` | 添加客户端扩展 / Add client item extensions       |
| `tag(TagKey<Item>...)`                                              | 添加物品标签 / Add item tags                     |
| `register()`                                                        | 返回 `ItemEntry<T>` / Returns `ItemEntry<T>` |

### 示例 / Example

```java
public static final ItemEntry<Item> MY_ITEM = REGISTRATE
        .item("my_item", Item::new)
        .properties(p -> p.food(new FoodProperties.Builder()
                .nutrition(4).saturationModifier(0.5f).build()))
        .tag(ItemTags.FOOD)
        .tab(MyMod.CREATIVE_TAB)
        .register();
```

---

## BlockEntityBuilder

用于注册方块实体。  
Used for registering block entities.

```java
public class BlockEntityBuilder<T extends BlockEntity, P>
        extends AbstractBuilder<BlockEntityType<?>, BlockEntityType<T>, P, BlockEntityBuilder<T, P>>
```

### 方法 / Methods

| 方法 / Method                                               | 描述 / Description                                         |
|-----------------------------------------------------------|----------------------------------------------------------|
| `validBlock(NonNullSupplier<? extends Block>)`            | 为此方块实体关联一个有效方块 / Associate a valid block with this BE    |
| `validBlocks(NonNullSupplier<? extends Block>...)`        | 关联多个有效方块 / Associate multiple valid blocks               |
| `renderer(NonNullSupplier<NonNullFunction>)`              | 设置方块实体渲染器 / Set block entity renderer                    |
| `registerCapability(Consumer<RegisterCapabilitiesEvent>)` | 注册能力（Capability）/ Register capabilities                  |
| `register()`                                              | 返回 `BlockEntityEntry<T>` / Returns `BlockEntityEntry<T>` |

### 示例 / Example

```java
public static final BlockEntityEntry<MyBlockEntity> MY_BE = REGISTRATE
        .blockEntity("my_be", MyBlockEntity::new)
        .validBlock(() -> MyMod.MY_BLOCK)
        .renderer(() -> MyBERenderer::new)
        .register();
```

---

## EntityBuilder

用于注册实体。  
Used for registering entities.

```java
public class EntityBuilder<T extends Entity, P>
        extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>>
```

### 方法 / Methods

| 方法 / Method                                                                      | 描述 / Description                               |
|----------------------------------------------------------------------------------|------------------------------------------------|
| `properties(NonNullConsumer<EntityType.Builder<T>>)`                             | 修改实体类型属性 / Modify entity type properties       |
| `renderer(NonNullSupplier<NonNullFunction>)`                                     | 设置实体渲染器 / Set entity renderer                  |
| `attributes(Supplier<AttributeSupplier.Builder>)`                                | 设置实体属性 / Set entity attributes                 |
| `spawnPlacement(SpawnPlacementType, Heightmap.Types, SpawnPredicate, Operation)` | 设置生成放置规则 / Set spawn placement rules           |
| `spawnEgg()`                                                                     | 自动生成刷怪蛋 / Auto-generate spawn egg              |
| `spawnEgg(NonNullConsumer<ItemBuilder>)`                                         | 自定义刷怪蛋 / Custom spawn egg                      |
| `defaultLang()`                                                                  | 自动生成语言条目 / Auto-generate lang entry            |
| `lang(String)`                                                                   | 自定义语言名称 / Custom lang name                     |
| `loot(NonNullBiConsumer)`                                                        | 自定义战利品表 / Custom loot table                    |
| `tag(TagKey<EntityType<?>>...)`                                                  | 添加实体标签 / Add entity type tags                  |
| `register()`                                                                     | 返回 `EntityEntry<T>` / Returns `EntityEntry<T>` |

### 示例 / Example

```java
public static final EntityEntry<MyEntity> MY_ENTITY = REGISTRATE
        .entity("my_entity", MyEntity::new, MobCategory.CREATURE)
        .attributes(MyEntity::createAttributes)
        .renderer(() -> MyEntityRenderer::new)
        .spawnEgg()
        .spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR)
        .loot((prov, type) -> prov.add(type, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(Items.DIAMOND)))))
        .register();
```

---

## FluidBuilder

用于注册流体。  
Used for registering fluids.

```java
public class FluidBuilder<T extends BaseFlowingFluid, P>
        extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>>
```

### 创建 / Creation

```java
// 基本创建（默认 FluidType）/ Basic creation (default FluidType)
registrate.fluid("my_fluid",
        Identifier.of("minecraft:block/water_still"),
        Identifier.of("minecraft:block/water_flow"),
        FluidType::new)
```

### 方法 / Methods

| 方法 / Method                                                     | 描述 / Description                                               |
|-----------------------------------------------------------------|----------------------------------------------------------------|
| `properties(NonNullConsumer<FluidType.Properties>)`             | 修改流体类型属性 / Modify fluid type properties                        |
| `fluidProperties(NonNullConsumer<BaseFlowingFluid.Properties>)` | 修改流体属性 / Modify fluid properties                               |
| `defaultLang()`                                                 | 自动生成语言条目 / Auto-generate lang entry                            |
| `lang(String)`                                                  | 自定义语言名称 / Custom lang name                                     |
| `defaultSource()`                                               | 使用默认流体源 / Use default fluid source                             |
| `source(NonNullFunction)`                                       | 自定义流体源 / Custom fluid source                                   |
| `defaultBlock()`                                                | 自动生成流体方块 / Auto-generate fluid block                           |
| `block()`                                                       | 返回 `BlockBuilder` 配置流体方块 / Return BlockBuilder for fluid block |
| `block(NonNullBiFunction)`                                      | 使用自定义工厂创建流体方块 / Custom fluid block factory                     |
| `noBlock()`                                                     | 不生成流体方块 / Skip fluid block generation                          |
| `defaultBucket()`                                               | 自动生成流体桶 / Auto-generate fluid bucket                           |
| `bucket()`                                                      | 返回 `ItemBuilder` 配置流体桶 / Return ItemBuilder for bucket         |
| `bucket(NonNullBiFunction)`                                     | 自定义桶 / Custom bucket                                           |
| `noBucket()`                                                    | 不生成桶 / Skip bucket generation                                  |
| `model(Identifier, Identifier)`                                 | 设置静态和流动纹理 / Set still and flowing textures                     |
| `model(NonNullSupplier)`                                        | 自定义模型 / Custom model                                           |
| `clientExtension(NonNullSupplier)`                              | 添加客户端流体类型扩展 / Add client fluid type extensions                 |
| `tag(TagKey<Fluid>...)`                                         | 添加流体标签 / Add fluid tags                                        |
| `removeTag(TagKey<Fluid>...)`                                   | 移除流体标签 / Remove fluid tags                                     |
| `register()`                                                    | 返回 `FluidEntry<T>` / Returns `FluidEntry<T>`                   |

### 示例 / Example

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MY_FLUID = REGISTRATE
        .fluid("my_fluid",
                Identifier.of("minecraft:block/water_still"),
                Identifier.of("minecraft:block/water_flow"),
                FluidType::new)
        .properties(p -> p.lightLevel(15).canConvertToSource(true))
        .register();
```

---

## MenuBuilder

用于注册菜单（容器界面）。  
Used for registering menus (container screens).

```java
public class MenuBuilder<T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>, P>
        extends AbstractBuilder<MenuType<?>, MenuType<T>, P, MenuBuilder<T, S, P>>
```

### 方法 / Methods

| 方法 / Method  | 描述 / Description                           |
|--------------|--------------------------------------------|
| `register()` | 返回 `MenuEntry<T>` / Returns `MenuEntry<T>` |

### 工厂接口 / Factory Interfaces

```java
// 标准菜单工厂 / Standard menu factory
MenuFactory<T>       // (type, windowId, inv) -> T

// Forge 扩展菜单工厂（支持缓冲区）/ Forge extended menu factory (with buffer)
ForgeMenuFactory<T>  // (type, windowId, inv, buffer) -> T

// 屏幕工厂 / Screen factory
ScreenFactory<M, S>  // (menu, inv, displayName) -> S
```

### 示例 / Example

```java
public static final MenuEntry<ChestMenu> MY_MENU = REGISTRATE
        .menu("my_menu",
            (type, windowId, inv) -> new ChestMenu(type, windowId, inv, new SimpleContainer(9), 9),
            () -> ContainerScreen::new)
        .register();
```
