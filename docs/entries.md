# 注册条目 / Registry Entries

当构建器调用 `register()` 后，会返回一个强类型的 `RegistryEntry` 句柄。这些条目提供了类型安全的方式来引用和使用已注册的对象。

When a builder's `register()` is called, it returns a strongly-typed `RegistryEntry` handle. These entries provide a type-safe way to reference and use registered objects.

---

## RegistryEntry —— 基础条目 / Base Entry

所有注册条目的基类。  
Base class for all registry entries.

```java
public class RegistryEntry<R, S extends R> extends DeferredHolder<R, S>
        implements NonNullSupplier<S>
```

### 方法 / Methods

| 方法 / Method               | 描述 / Description                                                 |
|---------------------------|------------------------------------------------------------------|
| `get()`                   | 获取注册对象（继承自 `NonNullSupplier`）/ Get the registered object         |
| `getSibling(ResourceKey)` | 按注册表键获取兄弟条目（如方块获取其 BlockItem）/ Get sibling entry by registry key |
| `getSibling(Registry)`    | 按注册表实例获取兄弟条目 / Get sibling entry by registry instance            |
| `getSiblingOptional(...)` | 获取可选的兄弟条目 / Get optional sibling entry                           |
| `filter(Predicate)`       | 按谓词过滤条目 / Filter entry by predicate                              |
| `is(Object)`              | 检查条目是否等于给定对象 / Check if entry equals given object                |

### 兄弟条目示例 / Sibling Example

```java
// 从方块获取其 BlockItem 条目
// Get BlockItem entry from a block
ItemEntry<BlockItem> blockItem = ItemEntry.cast(
        MY_BLOCK.getSibling(Registries.ITEM));

// 从方块获取其方块实体条目
// Get BE entry from a block
BlockEntityEntry<MyBE> be = BlockEntityEntry.cast(
        MY_BLOCK.getSibling(Registries.BLOCK_ENTITY_TYPE));
```

---

## ItemProviderEntry —— 可提供物品的条目 / Item-able Entry

`BlockEntry` 和 `ItemEntry` 的基类，实现了 `ItemLike`。  
Base class for `BlockEntry` and `ItemEntry`, implements `ItemLike`.

```java
public class ItemProviderEntry<R extends ItemLike, T extends R>
        extends RegistryEntry<R, T> implements ItemLike
```

### 方法 / Methods

| 方法 / Method         | 描述 / Description                                                        |
|---------------------|-------------------------------------------------------------------------|
| `asStack()`         | 创建包含此物品的 `ItemStack`（数量 1）/ Create an ItemStack (count 1)               |
| `asStack(int)`      | 创建指定数量的 `ItemStack` / Create an ItemStack with specified count          |
| `asStackTemplate()` | 创建 `ItemStackTemplate`（用于配方）/ Create an ItemStackTemplate (for recipes) |
| `isIn(ItemStack)`   | 检查物品栈是否包含此物品 / Check if a stack contains this item                      |
| `is(Item)`          | 检查是否特定物品 / Check if specific item                                       |
| `asItem()`          | 获取 `Item` 对象 / Get the Item object                                      |

---

## BlockEntry —— 方块条目 / Block Entry

```java
public class BlockEntry<T extends Block> extends ItemProviderEntry<Block, T>
```

### 方法 / Methods

| 方法 / Method           | 描述 / Description                                     |
|-----------------------|------------------------------------------------------|
| `getDefaultState()`   | 获取默认方块状态 / Get default block state                   |
| `has(BlockState)`     | 检查方块状态是否属于此方块 / Check if state belongs to this block |
| `cast(RegistryEntry)` | 静态转换方法 / Static cast method                          |

### 示例 / Example

```java
public static final BlockEntry<MyBlock> MY_BLOCK = REGISTRATE
        .block("my_block", MyBlock::new).simpleItem().register();

// 使用 / Usage
BlockState state = MY_BLOCK.getDefaultState();
ItemStack stack = MY_BLOCK.asStack();
boolean isMyBlock = MY_BLOCK.has(someState);
```

---

## ItemEntry —— 物品条目 / Item Entry

```java
public class ItemEntry<T extends Item> extends ItemProviderEntry<Item, T>
```

### 方法 / Methods

| 方法 / Method           | 描述 / Description            |
|-----------------------|-----------------------------|
| `cast(RegistryEntry)` | 静态转换方法 / Static cast method |

### 示例 / Example

```java
public static final ItemEntry<Item> MY_ITEM = REGISTRATE
        .item("my_item", Item::new).register();

// 使用 / Usage
ItemStack stack = MY_ITEM.asStack(64);
boolean isFood = MY_ITEM.is(Items.APPLE);
```

---

## BlockEntityEntry —— 方块实体条目 / Block Entity Entry

```java
public class BlockEntityEntry<T extends BlockEntity>
        extends RegistryEntry<BlockEntityType<?>, BlockEntityType<T>>
```

### 方法 / Methods

| 方法 / Method                          | 描述 / Description                                    |
|--------------------------------------|-----------------------------------------------------|
| `create(BlockPos, BlockState)`       | 在指定位置创建方块实体实例 / Create a BE instance at a position  |
| `is(BlockEntity)`                    | 检查方块实体是否属于此类型 / Check if BE is of this type         |
| `get(BlockGetter, BlockPos)`         | 从世界中获取可选的方块实体实例 / Get optional BE from world        |
| `getNullable(BlockGetter, BlockPos)` | 从世界中获取可能为 null 的方块实体实例 / Get nullable BE from world |
| `cast(RegistryEntry)`                | 静态转换方法 / Static cast method                         |

### 示例 / Example

```java
// 获取世界中的方块实体
// Get BE in world
Optional<MyBE> be = MY_BE.get(level, pos);

// 创建新实例
// Create new instance
MyBE newBe = MY_BE.create(pos, state);
```

---

## EntityEntry —— 实体条目 / Entity Entry

```java
public class EntityEntry<T extends Entity>
        extends RegistryEntry<EntityType<?>, EntityType<T>>
```

### 方法 / Methods

| 方法 / Method                        | 描述 / Description                              |
|------------------------------------|-----------------------------------------------|
| `create(Level, EntitySpawnReason)` | 创建实体实例 / Create an entity instance            |
| `is(Entity)`                       | 检查实体是否属于此类型 / Check if entity is of this type |
| `cast(RegistryEntry)`              | 静态转换方法 / Static cast method                   |

### 示例 / Example

```java
// 生成实体
// Spawn entity
MyEntity entity = MY_ENTITY.create(level, EntitySpawnReason.SPAWN_EGG);
entity.setPos(x, y, z);
level.addFreshEntity(entity);
```

---

## FluidEntry —— 流体条目 / Fluid Entry

```java
public class FluidEntry<T extends BaseFlowingFluid>
        extends RegistryEntry<Fluid, T>
```

### 方法 / Methods

| 方法 / Method   | 描述 / Description                                                           |
|---------------|----------------------------------------------------------------------------|
| `is(Object)`  | 使用 `Fluid.isSame()` 检查流体是否相同 / Check fluid equality using `Fluid.isSame()` |
| `getSource()` | 获取此流体的源变体 / Get the source variant of this fluid                           |
| `getType()`   | 获取 `FluidType` / Get the `FluidType`                                       |
| `getBlock()`  | 获取流体方块（如存在） / Get the fluid block (if present)                             |
| `getBucket()` | 获取流体桶物品（如存在） / Get the bucket item (if present)                            |

### 示例 / Example

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MY_FLUID = REGISTRATE
        .fluid("my_fluid", still, flow, FluidType::new).register();

// 使用 / Usage
Fluid source = MY_FLUID.getSource();
Optional<Block> block = MY_FLUID.getBlock();
Optional<Item> bucket = MY_FLUID.getBucket();
boolean isSame = MY_FLUID.is(otherFluid);
```

---

## MenuEntry —— 菜单条目 / Menu Entry

```java
public class MenuEntry<T extends AbstractContainerMenu>
        extends RegistryEntry<MenuType<?>, MenuType<T>>
```

### 方法 / Methods

| 方法 / Method                                                        | 描述 / Description                                     |
|--------------------------------------------------------------------|------------------------------------------------------|
| `create(int, Inventory)`                                           | 创建菜单实例 / Create a menu instance                      |
| `asProvider()`                                                     | 获取 `MenuConstructor` / Get a `MenuConstructor`       |
| `open(ServerPlayer, Component)`                                    | 为玩家打开菜单 / Open the menu for a player                 |
| `open(ServerPlayer, Component, Consumer<RegistryFriendlyByteBuf>)` | 打开菜单并传递额外数据 / Open menu with extra data              |
| `open(ServerPlayer, Component, MenuConstructor)`                   | 使用自定义提供者打开菜单 / Open with custom provider             |
| `open(ServerPlayer, Component, MenuConstructor, Consumer)`         | 使用自定义提供者和数据打开菜单 / Open with custom provider and data |

### 示例 / Example

```java
// 在服务器端为玩家打开菜单
// Open menu for a player on the server side
MY_MENU.open(player, Component.literal("My Menu"));

// 带额外数据
// With extra data
MY_MENU.open(player, Component.literal("My Menu"), buf -> {
    buf.writeUtf("extra data");
});
```

---

## LazyRegistryEntry —— 延迟解析条目 / Lazy Entry

用于需要延迟解析注册条目的场景，避免在类加载阶段提前触发注册。  
Used when registry entry resolution needs to be deferred to avoid triggering registration early during class loading.

```java
public class LazyRegistryEntry<R, T extends R> implements NonNullSupplier<T>
```

### 构造 / Construction

```java
LazyRegistryEntry<>(NonNullSupplier<? extends RegistryEntry<R, T>> supplier)
```

解析一次后缓存结果，并释放 supplier 引用以允许 GC。  
Resolves once, caches the result, and releases the supplier reference for GC.
