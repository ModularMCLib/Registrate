# Contributing

## Development Baseline

- Java: `25`
- Build tool: Gradle wrapper
- Recommended IDE: IntelliJ IDEA with the generated run configurations
- Line endings: `LF` via `.gitattributes` and `.editorconfig`

## Local Workflow

1. Sync the Gradle project with a Java 25 runtime.
2. Make changes inside the package layer that owns the behavior you are touching.
3. Validate with `./gradlew build` or `gradlew.bat build`.
4. Use the IDEA run configurations for interactive verification:
   - `Client`
   - `Server`
   - `Data Generation`
   - `Game Tests`
   - `Game Tests (Client)`

## Package Boundaries

- Put fluent registration APIs in `com.modularmc.registrate.builders`.
- Put data generation wiring in `com.modularmc.registrate.providers`.
- Put generic runtime support code in `com.modularmc.registrate.util`.
- Keep typed registry wrappers in `com.modularmc.registrate.util.entry`.
- Avoid adding new public API surface to `util` when the concept belongs in a builder or provider.

## Maintenance Notes

- `RegistrateLib` is a fork metadata/constants holder, not a NeoForge mod entrypoint.
- `src/test/java/com/modularmc/registrate/test/meta` contains maintenance helpers for generated bridge methods.
- When upstream Registrate changes, update implementation classes first and then re-verify package ownership against `docs/architecture.md`.
