# 高级用法 / Advanced Usage

---

## 自定义注册表 / Custom Registries

RegistrateLib 支持创建内置注册表和数据包注册表。  
RegistrateLib supports creating both built-in and datapack registries.

### 内置注册表 / Built-in Registry

```java
// 创建内置注册表 / Create a built-in registry
public static final ResourceKey<Registry<MyCustomEntry>> CUSTOM_REGISTRY =
    REGISTRATE.makeRegistry("custom", RegistryBuilder::new);

// 使用该注册表注册对象 / Register objects with this registry
public static final RegistryEntry<MyCustomEntry, MyCustomEntry> MY_CUSTOM =
    REGISTRATE.object("testcustom")
        .simple(CUSTOM_REGISTRY, MyCustomEntry::new);
```

### 数据包注册表 / Datapack Registry

```java
// 创建数据包注册表（仅服务端同步）/ Datapack registry (server-side sync only)
public static final ResourceKey<Registry<MyEntry>> MY_REGISTRY =
    REGISTRATE.makeDatapackRegistry("my_registry", MyEntry.CODEC);

// 创建数据包注册表（带网络同步）/ Datapack registry (with network sync)
public static final ResourceKey<Registry<MyEntry>> MY_REGISTRY =
    REGISTRATE.makeDatapackRegistry("my_registry", MyEntry.CODEC, MyEntry.NETWORK_CODEC);
```

---

## 生命周期钩子 / Lifecycle Hooks

### onRegister —— 注册回调

在对象被注册到游戏注册表时触发。  
Fires when the object is registered into the game registry.

```java
REGISTRATE.item("my_item", Item::new)
    .onRegister(item -> {
        // 注册时执行操作 / Do something on registration
        LOGGER.info("Item registered: {}", item);
    })
    .register();
```

### onRegisterAfter —— 依赖注册回调

在指定注册表完成注册后触发。  
Fires after a specified registry finishes registration.

```java
REGISTRATE.block("my_block", MyBlock::new)
    .onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, block -> {
        // 方块实体注册后执行操作 / Do after block entities are registered
    })
    .register();
```

### 全局注册回调 / Global Registration Callbacks

```java
// 特定条目的注册回调 / Per-entry registration callback
REGISTRATE.addRegisterCallback("my_block", Registries.BLOCK, block -> {
    // ...
});

// 整个注册表的注册后回调 / Per-registry after-register callback
REGISTRATE.addRegisterCallback(Registries.BLOCK, () -> {
    // 所有方块注册完成后执行 / All blocks registered
});
```

---

## 数据生成器管理 / Data Generator Management

### 设置/移除对象数据生成器 / Set/Remove Per-Object Data Generators

```java
// 通过构建器设置数据生成器 / Via builder
builder.setData(ProviderType.RECIPE, (ctx, recipeProv) -> { ... });
builder.removeData(ProviderType.RECIPE);

// 通过 Registrate 实例 / Via Registrate instance
REGISTRATE.setDataGenerator(builder, ProviderType.RECIPE, consumer);
REGISTRATE.setDataGenerator("my_block", Registries.BLOCK, ProviderType.RECIPE, consumer);
REGISTRATE.removeDataGenerator(builder, ProviderType.RECIPE);
```

### 添加全局数据生成器 / Add Global Data Generators

```java
REGISTRATE.addDataGenerator(ProviderType.LANG, langProvider -> {
    langProvider.addBlock(MY_BLOCK);
});
```

---

## 创造模式标签页 / Creative Mode Tabs

### 设置默认创造模式标签页 / Set Default Creative Tab

```java
// 方法 1：引用现有标签页 / Reference an existing tab
REGISTRATE.defaultCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);

// 方法 2：创建自定义标签页 / Create a custom tab
public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MY_TAB =
    REGISTRATE.object("my_tab")
        .defaultCreativeTab(tab -> tab.withLabelColor(0xFF00AA00))
        .register();
```

### 将物品添加到标签页 / Add Items to Tabs

```java
REGISTRATE.item("my_item", Item::new)
    .tab(MY_TAB.getKey())                       // 简单添加 / Simple add
    .tab(MY_TAB.getKey(), (ctx, modifier) ->    // 上下文感知添加 / Context-aware
        modifier.accept(ctx))
    .removeTab(CreativeModeTabs.BUILDING_BLOCKS) // 从标签页移除 / Remove from tab
    .register();
```

### 修改现有标签页 / Modify Existing Tabs

```java
REGISTRATE.modifyCreativeModeTab(CreativeModeTabs.INGREDIENTS, modifier -> {
    modifier.accept(MY_ITEM);
});
```

---

## 数据映射 / Data Maps

```java
REGISTRATE.block("my_block", MyBlock::new)
    .dataMap(DataMapTypes.FUEL, 300)  // 设置燃料值 / Set fuel value
    .register();

// 带上下文的工厂 / Context-aware factory
REGISTRATE.block("my_block", MyBlock::new)
    .dataMap(DataMapTypes.FUEL, ctx -> {
        // 根据上下文计算值 / Compute value from context
        return ctx.getEntry().is(Blocks.DIAMOND) ? 3200 : 0;
    })
    .register();
```

---

## 转换器 / Transform

可以使用 `.transform()` 方法将构建器传递给辅助函数。  
Use `.transform()` to pass the builder to helper functions.

```java
// 定义可复用的变换函数 / Define reusable transform function
private static <T extends Block, P> BlockBuilder<T, P> applyDiamondDrop(
        BlockBuilder<T, P> builder) {
    return builder.loot((prov, block) -> prov.dropOther(block, Items.DIAMOND));
}

// 在构建器中应用 / Apply in builder
REGISTRATE.block("my_block", MyBlock::new)
    .transform(MyMod::applyDiamondDrop)
    .register();
```

---

## DataIngredient —— 数据驱动的配方便利工具 / Data-Driven Recipe Ingredient

```java
// 从物品条目创建 / From item entries
DataIngredient.items(MY_ITEM);
DataIngredient.items(MY_BLOCK, MY_OTHER_ITEM);

// 从标签创建 / From tags
DataIngredient.tag(ItemTags.PLANKS);

// 结合 Ingredient / Combine with Ingredient
DataIngredient.ingredient(Ingredient.of(...), Items.DIAMOND);

// 获取进度条件 / Get criterion for advancement unlock
DataIngredient.items(MY_ITEM).getCriterion(recipeProvider);
```

---

## CreativeModeTabModifier —— 标签页修改器 / Tab Modifier

```java
// 在标签页修改回调中使用的输出接口
// Output interface used in tab modification callbacks
public interface CreativeModeTab.Output {
    void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility);
}

// CreativeModeTabModifier 额外方法 / CreativeModeTabModifier extras
modifier.getFlags();           // 获取功能标志 / Get feature flags
modifier.getParameters();      // 获取显示参数 / Get display parameters
modifier.hasPermissions();     // 是否有权限 / Has permissions
modifier.accept(itemLike);     // 添加物品 / Add item (default visibility)
modifier.accept(itemLike, TabVisibility);  // 添加物品（指定可见性）/ Add item with visibility
```

---

## 注册监听器管理 / Registration Listener Management

`OneTimeEventReceiver` 提供了一次性事件监听器机制。  
Provides one-shot event listener mechanism.

```java
// 添加一次性 mod 总线监听器 / Add one-shot mod bus listener
OneTimeEventReceiver.addModListener(registrate, RegisterEvent.class, event -> {
    // 此监听器在第一次触发后自动取消注册
    // This listener auto-unregisters after first fire
});
```

---

## 客户端扩展 / Client Extensions

### 方块客户端扩展 / Block Client Extensions

```java
REGISTRATE.block("my_block", MyBlock::new)
    .clientExtension(() -> () -> new IClientBlockExtensions() {
        // 自定义方块客户端行为 / Custom block client behavior
    })
    .register();
```

### 物品客户端扩展 / Item Client Extensions

```java
REGISTRATE.item("my_item", Item::new)
    .clientExtension(() -> () -> new IClientItemExtensions() {
        // 自定义物品客户端行为 / Custom item client behavior
    })
    .register();
```

### 流体客户端扩展 / Fluid Client Extensions

```java
REGISTRATE.fluid("my_fluid", still, flow, FluidType::new)
    .clientExtension(() -> () -> new IClientFluidTypeExtensions() {
        // 自定义流体客户端行为 / Custom fluid client behavior
    })
    .register();
```

---

## 颜色着色 / Color Tinting

```java
// 方块着色 / Block tinting
REGISTRATE.block("my_block", MyBlock::new)
    .color(() -> () -> List.of(
        BlockTintSources.constant(0xFFFF0000)  // 红色着色 / Red tint
    ))
    .register();

// 物品模型着色（通过 ItemBuilder）/ Item model tinting (via ItemBuilder)
REGISTRATE.item("my_item", Item::new)
    .defaultModel(new ItemTintSource("layer0", Constant.create(0xFFFF0000)))
    .register();
```

---

## 延迟注册 / Deferred Registration

对于在模组初始化后可能需要延迟解析的对象，可以使用 `NonNullSupplier.lazy()` 创建延迟初始化。  
For objects that need deferred resolution after mod initialisation, use `NonNullSupplier.lazy()`.

```java
public static final NonNullSupplier<Registrate> REGISTRATE =
    NonNullSupplier.lazy(() -> Registrate.create(MOD_ID));
```

---

## 跳过错误 / Skipping Errors

```java
// 在开发环境下跳过注册错误 / Skip registration errors in dev
REGISTRATE.skipErrors(true);
```

---

## 从现有对象获取条目 / Getting Entries from Existing Objects

```java
// 从已注册的 Block 获取 BlockEntry
// Get BlockEntry from a registered block
BlockEntry<Block> entry = BlockEntry.cast(
    registrate.get("existing_block", Registries.BLOCK));

// 检查对象是否已注册
// Check if an object is registered
boolean registered = REGISTRATE.isRegistered(Registries.BLOCK);

// 获取某注册表中的所有条目
// Get all entries in a registry
Collection<RegistryEntry<Block, ? extends Block>> allBlocks =
    REGISTRATE.getAll(Registries.BLOCK);
```

---

## 注册多个名称相同的条目 / Registering Entries with the Same Name

```java
// 使用相同的名称在不同注册表中注册不同对象
// Register different objects with the same name in different registries
REGISTRATE.object("testitem")
    .item(Item::new)
    .register();

REGISTRATE.object("testitem")
    .entity(TestEntity::new, MobCategory.CREATURE)
    .register();
```
