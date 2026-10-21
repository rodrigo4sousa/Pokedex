# Inventory

What the code contains, read from the build files, the manifest, the DI modules and the source. The diagrams are drawn from this file.
Last updated: 2026-10-21.

## Stack

| Area | What the code uses |
|---|---|
| Language | Kotlin 2.4.20, JVM target 11, `minSdk` 31, `targetSdk`/`compileSdk` 37 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3, Navigation Compose, single Activity |
| Architecture | MVVM: ViewModels expose `LiveData` (`MutableLiveData`, `MediatorLiveData`, `switchMap`), screens read it with `observeAsState`. No use-case layer. |
| DI | Hilt 2.60 (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `DatabaseModule`) |
| Network | Retrofit 3 + Gson (PokeAPI); Coil 3 + OkHttp (images); `MediaPlayer` streams cries (`.ogg`) |
| Persistence | Room 2.8.4: `PokedexDatabase` v1 (`pokedex.db`), single table `caught_pokemon`; in-memory type membership cache in `PokemonRepository` |
| Async | Coroutines (`viewModelScope`, `async`/`awaitAll`); Room reads return `LiveData`. No Flow, no WorkManager. |
| Build | AGP 9.4.1, KSP (Hilt and Room code generation) |

## Gradle modules and packages

App module `:app`, package `com.example.pokedex` (test-only module `:testing` described below):

| Package | Contents |
|---|---|
| (root) | `PokedexApp` (Application, `@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`) |
| `data` | `PokemonRepository` (returns domain models only) |
| `data/remote` | `PokemonApi` (Retrofit interface), `NetworkModule` (Hilt: OkHttp, Retrofit, `PokemonApi`) |
| `data/remote/dto` | PokeAPI response types: `PokemonDto` (+ `AbilitySlotDto`, `TypeSlotDto`, `StatDto`, `SpritesDto`, `CriesDto`), `PokemonPageDto`, `PokemonSpeciesDto`, `EvolutionChainDto` (+ `ChainLinkDto`, `EvolutionDetailDto`), `TypeDto` (+ `TypePokemonDto`, `DamageRelationsDto`), `CountDto`, `NamedResourceDto`, `ApiResourceDto` |
| `data/mapper` | `RemoteMappers` (DTO → domain, `idFromUrl`, `spriteUrlFor`, damage multipliers), `LocalMappers` (entity ↔ domain) |
| `data/local` | `CaughtPokemonEntity`, `CaughtPokemonDao`, `PokedexDatabase`, `DatabaseModule` (Hilt) |
| `domain/model` | `PokemonSummary`, `CaughtPokemon`, `Pokemon` (+ `Ability`, `Stat`, `Sprites`), `EvolutionStage` (+ `EvolutionCondition`, `Gender`) |
| `presentation/navigation` | `Navigation` (NavHost and bottom bar), `Screen` (routes) |
| `presentation/home` | `HomeScreen`, `HomeViewModel` |
| `presentation/pokedex` | `PokedexScreen`, `PokedexViewModel` |
| `presentation/pokemonDetail` | `PokemonDetailScreen`, `PokemonDetailViewModel` |
| `presentation/components` | `PokemonGrid`, `PokemonSearchBar`, `SortChips`, `FilterSidePanel`, `CatchStarButton`, `TypeColors` |
| `ui/theme` | Compose theme |

### Test-only module `:testing`

Plain Kotlin/JVM, used as `testImplementation` and `androidTestImplementation` by `:app`. No app classes.

| Package / folder | Contents |
|---|---|
| `com.example.pokedex.testing` | `Fixtures` (loads recorded JSON through the classloader), `PokeApiDispatcher` (MockWebServer dispatcher: path → fixture, 404 for unknown paths, 500 on demand) |
| `src/main/resources/fixtures/` | Recorded PokeAPI responses: trimmed list, Bulbasaur / Eevee / Tauros (`pokemon`, `pokemon-species`), evolution chains 1, 63, 67, types grass / poison / normal, species count |
| `scripts/record_fixtures.py` | Re-records the fixtures |

Test helpers in `:app`: `src/test/.../testutil` (`MainDispatcherRule`, `getOrAwaitValue`, `FakePokemonApi`, `FakeCaughtPokemonDao`); `src/androidTest` (`HiltTestRunner` with a fake Coil engine, `MockWebServerRule`, `di/TestNetworkModule`, `di/TestDatabaseModule`); `src/debug` (network security config allowing cleartext to localhost).

## AndroidManifest

- Activities: `MainActivity` only (launcher, `exported=true`)
- Services / receivers / providers: none
- Permissions: `INTERNET`
- `allowBackup="true"`; `data_extraction_rules.xml` (Android 12+) and `backup_rules.xml` (Android 11 and lower, unused with `minSdk` 31) keep only `pokedex.db` and `pokedex.db-wal`

## Data sources

| Source | Access | Used for |
|---|---|---|
| PokeAPI (`https://pokeapi.co/api/v2/`) | Retrofit, HTTPS/JSON | `pokemon/{name}`, `pokemon?limit&offset`, `pokemon-species/{name}`, evolution chain (full URL from species), `type/{name}`, `pokemon-species?limit=1` |
| PokeAPI Sprites (GitHub raw, `raw.githubusercontent.com/PokeAPI/sprites/...`) | Coil, HTTPS | List and evolution sprites (URL built by `spriteUrlFor()` in `data.mapper`); detail sprites come as URLs inside the PokeAPI response, on the same repository (confirmed from the fixtures) |
| PokeAPI Cries (GitHub raw, `raw.githubusercontent.com/PokeAPI/cries/...`) | `MediaPlayer`, HTTPS | `.ogg` URLs come inside the PokeAPI `pokemon` response (`cries.latest` / `cries.legacy`); host confirmed from the recorded fixtures |
| Room (`pokedex.db`) | Room DAO | Caught Pokémon: `name`, `pokemonId`, `caughtAt` |

No own backend, no cloud services, no file storage.

## Main flows

1. **Browse full list (Home):** load every Pokémon in one call (`limit=100000`); search and sort (Number / Name) are client-side.
2. **Filter by type (Home and My Pokédex):** `type/{name}` → set of names, cached in `PokemonRepository`; several types combine with OR.
3. **Catch / release (star):** Room insert/delete from Home, My Pokédex or detail; all screens update through `LiveData`.
4. **My Pokédex:** caught list from Room + "Caught x / y" total from `pokemon-species?limit=1`; search, sort (Number / Name / Date caught), type filter.
5. **Pokémon detail:** `pokemon/{name}`, then in parallel species → evolution chain, and `type/{t}` per type → damage multipliers.
6. **Sprites and cry (detail):** toggle front/back, shiny, female sprites (Coil); play cry (`MediaPlayer`).
7. **Navigate the evolution chain:** opens the detail of another species.

## Decisions

- **Sprites and cries are separate external systems** ("PokeAPI Sprites (GitHub raw)" and "PokeAPI Cries (GitHub raw)"), not part of "PokeAPI". They are served from the `PokeAPI/sprites` and `PokeAPI/cries` GitHub repositories on `raw.githubusercontent.com`, not from `pokeapi.co`, as the URLs in the recorded responses show.
- **The repository returns domain models only.** `PokemonRepository` maps PokeAPI DTOs and Room entities to `domain.model` types, so PokeAPI's JSON shape and the Room entity don't reach the ViewModels.
- **Retrofit and Room are provided by Hilt modules** (`NetworkModule`, `DatabaseModule`), so the device tests can replace them with `TestNetworkModule` and `TestDatabaseModule`.
- **C4 and the logical view group elements by layer; the development view groups them by package.** The first answer "what does each part do", the second "where is the code". Where a package and its layer differ, each view shows its own one.
- **Dead code is left out of the diagrams.** They describe what runs.
- **Android Auto Backup is left out of the diagrams.** The app only declares backup rules in XML (keep `pokedex.db` and its WAL file); no app code calls the backup service.
- **Play Store publication and CI appear only in `04-deployment-planned.puml`**, drawn dashed. They don't exist yet, and no CI tool has been chosen.
- **Diagrams are rendered with the `plantuml/plantuml` Docker image**, so the repository needs no PlantUML or Java setup for them.
