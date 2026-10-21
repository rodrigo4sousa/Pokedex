# Testing

The tests are split into four layers. No test calls the real PokeAPI or the sprite and cry hosts: network responses come from recorded fixtures.

## Layers

| Layer | Covers | Source set | Runs on | Tests |
|---|---|---|---|---|
| Unit | One class, or a small group, with hand-written fakes at the boundaries (API, DAO): mappers, both repositories, the guess game's rules (`GuessGame`), `LoadError`, the four ViewModels | `app/src/test` | JVM | 85 |
| Integration (JVM) | `PokemonApi` with real Retrofit and Gson, against MockWebServer | `app/src/test` | JVM | 12 |
| Integration (device) | `CaughtPokemonDao`, `PokemonRepository` with real Room and MockWebServer, the v1 → v2 migration | `app/src/androidTest` | Emulator or device | 9 |
| System | The whole app through the Compose UI, one test class per use case in [`specification.md`](specification.md) | `app/src/androidTest` | Emulator or device | 16 |

Not covered: cry playback (`MediaPlayer` is created inside the composable, with no seam to replace it), tapping an evolution (it opens the detail by Pokédex number, which has no fixtures), and how the guess game's silhouette looks (the device tests load images with a fake Coil engine, so they check its description, not its pixels).

## Running them

```bash
./gradlew testDebugUnitTest            # unit and JVM integration tests
./gradlew connectedDebugAndroidTest    # device integration and system tests (needs a running emulator)
```

`connectedDebugAndroidTest` uninstalls the app when it finishes, which deletes its data. Run it on an emulator kept for tests, not on one with Pokémon you caught.

## How the tests are built

- **Fixtures.** Recorded PokeAPI responses live in the test-only `:testing` module (`testing/src/main/resources/fixtures/`), with the MockWebServer dispatcher that serves them. `testing/scripts/record_fixtures.py` records them again. The module holds no app classes, so both `test` and `androidTest` can use it.
- **Fakes, not mocks.** JVM tests use hand-written fakes (`FakePokemonApi`, `FakeCaughtPokemonDao` in `app/src/test/.../testutil`). There is no mocking library.
- **Same Retrofit setup as the app.** Integration tests build Retrofit with `pokeApiRetrofit(baseUrl, client)`, the function `NetworkModule` uses, pointed at MockWebServer. They check the app's real JSON converter.
- **Hilt test modules.** On a device, `TestNetworkModule` points the app at MockWebServer on localhost and `TestDatabaseModule` gives it an in-memory Room database. `HiltTestRunner` also installs a fake Coil engine, so no image is downloaded.
- **Rule order.** Instrumented tests apply MockWebServer, then Hilt, then Compose. System tests use `createEmptyComposeRule()` and start `MainActivity` inside the test, so a test can configure the server (for example `failPath`) before the app's first request.
- **No sleeps.** System tests find nodes by visible text and wait with the `waitUntil…` helpers in `testutil/SystemTestHelpers.kt`. To check that something does *not* appear, a test first waits for the request that would cause it (`waitForRequest`), then asserts.
- **Fixed date.** The guess game reads today from an injected `java.time.Clock`. JVM tests pass `Clock.fixed(...)`; on a device, `TestClockModule` replaces `ClockModule` with 2026-10-27, the day the recorded species list picks Bulbasaur.
- **Room `LiveData`.** Room notifies observers asynchronously after a write, so device tests wait for the expected value with `awaitValue { predicate }` (`androidTest/.../testutil/LiveDataTestUtil.kt`).
- **Test names.** Backtick names (`` `catching adds it to My Pokédex` ``) only in `src/test`. Android supports method names with spaces only from API 30, so `androidTest` uses camelCase.
- **Database changes.** Changing an `@Entity` means: bump the database version, add a `Migration` and a migration test, and commit the new schema JSON from `app/schemas/`.
- **Dependency versions.** Test APKs use the app's dependency versions. If a test library needs a newer transitive version than the app resolves, align it with a `constraints { }` entry in `app/build.gradle.kts` (see kotlinx-serialization-core).
