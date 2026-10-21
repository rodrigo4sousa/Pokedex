# Pokédex architecture

Architecture of the Pokédex Android app, as PlantUML diagrams. Each diagram link opens the rendered SVG; the `.puml` link is its source.

## How these docs are organized

The docs use two models because they answer different questions. **C4** zooms in step by step (the system, its containers, their components), and **Kruchten's 4+1** looks at the same app from one concern at a time (classes, runtime behaviour, code organization, deployment, use cases).

- [`inventory.md`](inventory.md) lists what the code contains (stack, packages, data sources, flows) and the decisions behind the design. The diagrams are drawn from it.
- Every diagram follows the same rules:
  - **One abstraction level per diagram:** one C4 level or one 4+1 view.
  - **No invented elements:** every element and relationship exists in the code or the build configuration.
  - **Planned items are kept apart from existing ones:** they appear only in `04-deployment-planned`, drawn dashed.
  - **Dead code is left out.**
  - **About 10 elements at most.** The component level has 21, so it is split into three parts: the UI layer, the data layer and the guess game.
  - **One shared style:** every diagram includes [`style.puml`](style.puml).
- The same name means the same element in every diagram.

## Index

### C4 ([`c4/`](c4/))

| Diagram | Source | Shows |
|---|---|---|
| [1. Context](out/01-context.svg) | [`01-context.puml`](c4/01-context.puml) | User, Pokédex App, PokeAPI, PokeAPI Sprites (GitHub raw), PokeAPI Cries (GitHub raw) |
| [2. Containers](out/02-containers.svg) | [`02-containers.puml`](c4/02-containers.puml) | Android App and Local Database (Room / SQLite), plus the three external systems |
| [3. Components: UI layer](out/03-components-android-app-ui.svg) | [`03-components-android-app-ui.puml`](c4/03-components-android-app-ui.puml) | MainActivity, Navigation, the 4 screens, the 4 ViewModels, PokemonRepository |
| [3. Components: data layer](out/03-components-android-app-data.svg) | [`03-components-android-app-data.puml`](c4/03-components-android-app-data.puml) | ViewModels, PokemonRepository, Mappers, PokemonApi, NetworkModule, CaughtPokemonDao, PokedexDatabase, DatabaseModule |
| [3. Components: guess game](out/03-components-android-app-guess.svg) | [`03-components-android-app-guess.puml`](c4/03-components-android-app-guess.puml) | GuessScreen, GuessViewModel, GuessGame, ClockModule, PokemonRepository, GuessGameRepository, GuessGameDao, PokedexDatabase, DatabaseModule |
| [Dynamic: browse and filter](out/dynamic-browse-and-filter.svg) | [`dynamic-browse-and-filter.puml`](c4/dynamic-browse-and-filter.puml) | Load the full list, then filter by type |
| [Dynamic: catch / release](out/dynamic-catch-release-pokemon.svg) | [`dynamic-catch-release-pokemon.puml`](c4/dynamic-catch-release-pokemon.puml) | Star → Room insert/delete → LiveData back to the screen |
| [Dynamic: open detail](out/dynamic-open-pokemon-detail.svg) | [`dynamic-open-pokemon-detail.puml`](c4/dynamic-open-pokemon-detail.puml) | Load a Pokémon, then caught state, evolution chain and types in parallel |
| [Deployment](out/04-deployment.svg) | [`04-deployment.puml`](c4/04-deployment.puml) | What exists today: Android device with the app and `pokedex.db`, `pokeapi.co`, `raw.githubusercontent.com` |
| [Deployment: planned](out/04-deployment-planned.svg) | [`04-deployment-planned.puml`](c4/04-deployment-planned.puml) | **Planned** (dashed): CI service → Google Play Store → device |

### 4+1 ([`4plus1/`](4plus1/))

Who each view is for, and which decision it documents, is in [`4plus1/README.md`](4plus1/README.md).

| View | Diagrams |
|---|---|
| Logical | [Pokédex catalog](out/logical-catalog.svg) ([`.puml`](4plus1/logical-catalog.puml)), [guess game](out/logical-guess-game.svg) ([`.puml`](4plus1/logical-guess-game.puml)) |
| Process | [open Pokémon detail](out/process-open-pokemon-detail.svg) ([`.puml`](4plus1/process-open-pokemon-detail.puml)), [caught state sync](out/process-caught-state-sync.svg) ([`.puml`](4plus1/process-caught-state-sync.puml)) |
| Development | [package diagram](out/development.svg) ([`.puml`](4plus1/development.puml)) |
| Physical | [deployment diagram](out/physical.svg) ([`.puml`](4plus1/physical.puml)) |
| Scenarios (+1) | [use cases](out/scenarios-use-cases.svg) ([`.puml`](4plus1/scenarios-use-cases.puml)), [browse and filter](out/scenarios-browse-and-filter.svg) ([`.puml`](4plus1/scenarios-browse-and-filter.puml)), [catch / release](out/scenarios-catch-release.svg) ([`.puml`](4plus1/scenarios-catch-release.puml)), [view detail](out/scenarios-view-detail.svg) ([`.puml`](4plus1/scenarios-view-detail.puml)), [My Pokédex](out/scenarios-my-pokedex.svg) ([`.puml`](4plus1/scenarios-my-pokedex.puml)), [guess the daily Pokémon](out/scenarios-guess-pokemon.svg) ([`.puml`](4plus1/scenarios-guess-pokemon.puml)) |

### Guide ([`guide/`](guide/))

Explanatory diagrams, not a C4 level or a 4+1 view. They use the same names and style as the others.

| Diagram | Source | Shows |
|---|---|---|
| [MVVM](out/mvvm.svg) | [`mvvm.puml`](guide/mvvm.puml) | View, ViewModel and Model layers, with Retrofit and Room behind the repository |
| [Retrofit request](out/retrofit-request.svg) | [`retrofit-request.puml`](guide/retrofit-request.puml) | One request end to end, from PokemonDetailViewModel through OkHttp and Gson to PokeAPI |
| [Coil image loading](out/coil-image-loading.svg) | [`coil-image-loading.puml`](guide/coil-image-loading.puml) | How a sprite is loaded: memory cache, disk cache, download, decode |

## Known limitations

The diagrams show the code as it is, gaps included.

Android Auto Backup is left out of the diagrams. `data_extraction_rules.xml` (Android 12+) and `backup_rules.xml` (Android 11 and lower, unused with `minSdk` 31) keep only `pokedex.db` and its WAL file, so caught Pokémon survive a phone change.

## Rendering the diagrams

The SVGs in [`out/`](out/) are generated from the `.puml` files; don't edit them by hand. [`render.sh`](render.sh) (Git Bash, macOS, Linux) and [`render.ps1`](render.ps1) (Windows PowerShell) render every diagram with the `plantuml/plantuml` Docker image. The diagrams use Arial and the image only has DejaVu fonts, so the scripts mount Arial from the host into the container. Docker must be running.

```bash
bash docs/architecture/render.sh
powershell -ExecutionPolicy Bypass -File docs\architecture\render.ps1
```

Docker is used only to render these diagrams. The app itself has no Docker setup and no CI yet.
