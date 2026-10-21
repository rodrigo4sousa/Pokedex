# 4+1 views (Kruchten)

UML views of the Pokédex Android app. Names match the C4 diagrams in `../c4/`: the same name is the same element.
Rendered SVGs are in [`../out/`](../out/).

## Logical view: `logical.puml`

**For:** developers changing or adding features.
**Decision documented:** MVVM with one ViewModel per screen and a single `PokemonRepository`, with no use-case layer. The repository takes PokeAPI DTOs and Room entities and returns domain models only (`Pokemon`, `PokemonSummary`, `CaughtPokemon`, `EvolutionStage`), so neither PokeAPI's JSON shape nor the Room entity reaches the ViewModels. Classes are grouped by the layer they work in.

## Process view: `process-open-pokemon-detail.puml`, `process-caught-state-sync.puml`

**For:** developers working on loading, threading, error handling or anything that shows caught state.
**Decisions documented:**
- **Threading:** everything starts from `viewModelScope` on the main thread. Network calls are suspend Retrofit calls on OkHttp threads. Room work runs on Room's executor. The code sets no explicit dispatchers.
- **Detail screen:** the extra sections (caught state, evolution chain, type matchups) load in parallel once the Pokémon itself has loaded. They fail silently.
- **Caught state:** Room is the single source of truth. Every ViewModel observes Room `LiveData`, so a catch or release on one screen shows up on the others.

## Development view: `development.puml`

**For:** developers finding their way in the code or planning refactors.
**Decision documented:** a single app module, `:app`, organised by layer (`data`, `domain`, `presentation`), plus a test-only module, `:testing`, for fixtures and test code shared by `src/test` and `src/androidTest`. Navigation lives in `presentation.navigation` and the shared composables (grid, search bar, sort, filter) in `presentation.components`. Packages are drawn as they are in the code. `presentation` depends only on `data` (the repository) and `domain.model`; `data.remote` (Retrofit and DTOs) and `data.local` (Room) stay inside the data layer, and `data.mapper` turns their types into `domain.model`.

## Physical view: `physical.puml`

**For:** whoever installs, supports or releases the app.
**Decision documented:** the app has no backend; everything runs on the device. The only remote dependencies are two public HTTPS hosts (PokeAPI, and GitHub raw for sprites and cries), and the only persistent data is `pokedex.db` inside the app sandbox. The view covers only what exists today. Planned Play Store publication and CI are in `../c4/04-deployment-planned.puml`.

## Scenarios (+1): `scenarios-*.puml`

**For:** everyone: product, developers, reviewers.
**Decision documented:** which use cases drive the architecture, and how the other four views support them. They map to the user stories in `docs/specification.md`.

| Use case | User stories | Sequence | System test (`app/src/androidTest/.../system/`) |
|---|---|---|---|
| Overview | | `scenarios-use-cases.puml` | |
| Browse and filter Pokémon | US03 | `scenarios-browse-and-filter.puml` | `BrowseAndFilterSystemTest` |
| Catch / release a Pokémon | US01, US02 | `scenarios-catch-release.puml` | `CatchReleaseSystemTest` |
| View Pokémon detail | US04 | `scenarios-view-detail.puml` | `ViewDetailSystemTest` |
| Review My Pokédex | US05 | `scenarios-my-pokedex.puml` | `MyPokedexSystemTest` |

Each system test class is named after its scenario and checks what the user sees, through the Compose UI, against the recorded fixtures.
