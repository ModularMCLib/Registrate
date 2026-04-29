# 贡献指南 / Contributing

## 开发基线 / Development Baseline

- Java: `25`
- 构建工具 / Build tool: Gradle wrapper
- 推荐 IDE / Recommended IDE: IntelliJ IDEA（使用生成的运行配置 / with the generated run configurations）
- 换行符 / Line endings: `LF`（通过 `.gitattributes` 和 `.editorconfig` / via `.gitattributes` and `.editorconfig`）

## 本地工作流程 / Local Workflow

1. 使用 Java 25 运行时同步 Gradle 项目 / Sync the Gradle project with a Java 25 runtime
2. 在你所接触的行为所属的包层内进行修改 / Make changes inside the package layer that owns the behavior you are touching
3. 使用 `./gradlew build` 或 `gradlew.bat build` 验证 / Validate with `./gradlew build` or `gradlew.bat build`
4. 使用 IDEA 运行配置进行交互式验证 / Use the IDEA run configurations for interactive verification:
   - `Client` / 客户端
   - `Server` / 服务端
   - `Data Generation` / 数据生成
   - `Game Tests` / 游戏测试
   - `Game Tests (Client)` / 游戏测试（客户端）

## 包边界 / Package Boundaries

- 流式注册 API 放在 `com.modularmc.registrate.builders`
  Put fluent registration APIs in `com.modularmc.registrate.builders`
- 数据生成连接放在 `com.modularmc.registrate.providers`
  Put data generation wiring in `com.modularmc.registrate.providers`
- 通用运行时支持代码放在 `com.modularmc.registrate.util`
  Put generic runtime support code in `com.modularmc.registrate.util`
- 类型化注册包装器保持在 `com.modularmc.registrate.util.entry`
  Keep typed registry wrappers in `com.modularmc.registrate.util.entry`
- 当概念属于 builder 或 provider 时，避免向 `util` 添加新的公开 API 表面
  Avoid adding new public API surface to `util` when the concept belongs in a builder or provider

## 维护说明 / Maintenance Notes

- `RegistrateLib` 是本分支的库模组入口点，同时持有共享元数据/常量
  `RegistrateLib` is the library mod entrypoint for the fork and also holds shared metadata/constants
- `src/test/java/com/modularmc/registrate/test/meta` 包含用于生成的桥接方法的维护辅助工具
  `src/test/java/com/modularmc/registrate/test/meta` contains maintenance helpers for generated bridge methods
- 当上游 Registrate 变更时，先更新实现类，然后对照 `docs/architecture.md` 重新验证包所有权
  When upstream Registrate changes, update implementation classes first and then re-verify package ownership against `docs/architecture.md`
