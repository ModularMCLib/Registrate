# 快速入门 / Getting Started

## 安装 / Installation

### 1. 添加依赖 / Add Dependency

在 `build.gradle` 中添加：  
Add to your `build.gradle`:

```groovy
dependencies {
    implementation "com.modularmc.registrate:registratelib:${registratelib_version}"
}
```

### 2. 声明依赖 / Declare Dependency

在 `neoforge.mods.toml` 中添加：  
Add to your `neoforge.mods.toml`:

```toml
[[dependencies.yourmodid]]
modId="registratelib"
type="required"
versionRange="[2.0.0,)"
ordering="AFTER"
side="BOTH"
```

## 创建 Registrate 实例 / Creating a Registrate Instance

在你的主模组类中创建一个 `Registrate` 对象：  
Create a `Registrate` object in your main mod class:

```java
@Mod(MyMod.MOD_ID)
public class MyMod {
    public static final String MOD_ID = "mymod";

    // 简单方式（注意：需确保不在构造之前访问）
    // Simple approach (ensure it's not accessed before construction)
    public static final Registrate REGISTRATE = Registrate.create(MOD_ID);

    public MyMod(IEventBus modEventBus) {
        // 在此处注册事件监听器
        // Register event listeners here
    }
}
```

### 延迟初始化 / Lazy Initialization

如果需要延迟初始化（避免在类加载阶段过早创建）：  
If you need lazy initialization to avoid creating too early during class loading:

```java
public static final NonNullSupplier<Registrate> REGISTRATE =
        NonNullSupplier.lazy(() -> Registrate.create(MOD_ID));
```

## 注册你的第一个对象 / Registering Your First Object

### 方块 / Block

```java
public static final BlockEntry<Block> MY_BLOCK = REGISTRATE
        .block("my_block", Block::new)
        .simpleItem()           // 自动创建 BlockItem / Automatically create a BlockItem
        .defaultBlockstate()     // 使用默认方块状态 / Use default blockstate
        .defaultLang()           // 自动生成语言条目 / Auto-generate lang entry
        .defaultLoot()           // 自动生成战利品表 / Auto-generate loot table
        .register();             // 完成注册 / Finalize registration
```

### 物品 / Item

```java
public static final ItemEntry<Item> MY_ITEM = REGISTRATE
        .item("my_item", Item::new)
        .defaultModel()          // 使用默认模型 / Use default model
        .defaultLang()           // 自动生成语言条目 / Auto-generate lang entry
        .register();
```

### 方块实体 / Block Entity

```java
public static final BlockEntityEntry<MyBlockEntity> MY_BE = REGISTRATE
        .blockEntity("my_block_entity", MyBlockEntity::new)
        .validBlock(() -> MyMod.MY_BLOCK)  // 关联有效方块 / Associate valid block
        .register();
```

## 下一步 / Next Steps

- 探索[构建器 API](builders.md) 了解所有可用的构建器和方法
  Explore the [builder API](builders.md) for all available builders and methods
- 查看[注册条目](entries.md)了解如何使用注册后的句柄
  Check [registry entries](entries.md) for how to use the registered handles
- 阅读[数据提供者](providers.md)了解自动数据生成系统
  Read about [data providers](providers.md) for the automatic data generation system
