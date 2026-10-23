# Building from Source

Requirements: JDK 25 and access to the Paper and Skript Maven repositories.

```
./gradlew build
```
The jar is written to `build/libs/SkriptReloaded-<version>.jar`. The GitHub Action in `.github/workflows/build.yml`
does the same on every push and uploads the jar as an artifact.

Run the Skript test scripts with `./gradlew quickTest` (see `build.gradle` for all test tasks). Test scripts live in
`src/test/skript/tests`.
