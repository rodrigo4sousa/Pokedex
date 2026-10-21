# Pokédex

![API](https://img.shields.io/badge/API-31%2B-brightgreen.svg?style=flat)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=flat&logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange.svg?style=flat)

An Android Pokédex: browse every Pokémon from [PokeAPI](https://pokeapi.co/), inspect stats, evolutions and type matchups, and keep a list of the Pokémon you caught on the device.

| Home | Type filter | Detail | Stats & matchups | My Pokédex | Guess | Guess result |
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| ![Home: every Pokémon in a grid](docs/images/screenshot-home.png) | ![Filter side panel with the 18 types](docs/images/screenshot-filters.png) | ![Charizard detail: sprite, types, abilities](docs/images/screenshot-detail.png) | ![Charizard: base stats, type effectiveness, evolution chain](docs/images/screenshot-detail-matchups.png) | ![My Pokédex: caught Pokémon with counter](docs/images/screenshot-my-pokedex.png) | ![Guess: the daily Pokémon as a silhouette, one wrong guess, two hearts left](docs/images/screenshot-guess.png) | ![Guess result: Parasect revealed, streak 1](docs/images/screenshot-guess-result.png) |

## Features

- **Catch a Pokémon (US01):** tap the star on any card or "Catch" on the detail page. Caught Pokémon are saved on the device, and every screen updates at once.
- **Release a Pokémon (US02):** tap the star again, on any screen.
- **Browse all Pokémon (US03):** the full list, forms included, with search by name, sorting by number or name, and a filter by type (selected types combine with OR).
- **Inspect a Pokémon (US04):** sprites (front/back, shiny, female), its cry, height, weight, abilities, base stats, weaknesses and resistances computed from its types, and the evolution chain with its conditions.
- **Review My Pokédex (US05):** the caught Pokémon with a "Caught x / 1025" counter, search, sorting by number, name or date caught, and the same type filter.
- **Guess the daily Pokémon (US06):** one hidden Pokémon a day, shown as a silhouette. Three hearts to name it; a win raises the streak, a loss or a missed day resets it. The round and the streak are saved on the device.

The user stories and their acceptance criteria are in [`docs/specification.md`](docs/specification.md).

## Tech stack

Kotlin 2.4.20 and coroutines, Jetpack Compose with Material 3, Navigation Compose, ViewModel and LiveData, Hilt, Retrofit 3 with Gson, Room, Coil 3; min SDK 31, target SDK 37.

## Architecture

![C4 container diagram: User, Android App, Local Database, PokeAPI, PokeAPI Sprites, PokeAPI Cries](docs/architecture/out/02-containers.svg)

The app has no backend. Pokémon data is public and read-only, so the app reads it straight from PokeAPI, and the sprites and cries straight from PokeAPI's repositories on GitHub. The only data the user creates, the list of caught Pokémon and the guess game's progress, stays on the device in a Room database.

![MVVM layers: View, ViewModel, Model, with Retrofit and Room behind the repository](docs/architecture/out/mvvm.svg)

Each screen has its own ViewModel, which keeps the screen state as `LiveData`, so it survives rotation, and does search, sort and type filtering in memory. All ViewModels go through one `PokemonRepository`, which maps PokeAPI DTOs and Room entities to domain models, so PokeAPI's JSON shape never reaches the UI. The guess game also saves its round and streak through `GuessGameRepository`, and keeps its rules in `GuessGame`, a plain Kotlin class tested on its own. Caught state comes from Room as `LiveData`, so catching a Pokémon on one screen updates the other screens without them talking to each other.

The rest of the architecture (C4 context, components, dynamic flows and deployment, Kruchten's 4+1 views, and how a Retrofit request and a Coil image load work) is in [`docs/architecture/`](docs/architecture/README.md).

## Testing

The tests are split into unit, JVM integration, device integration and system tests. No test calls the real PokeAPI: responses come from recorded fixtures, served by hand-written fakes on the JVM and by MockWebServer on a device, where Hilt also swaps in an in-memory database. The system tests drive the whole app through the Compose UI, one test class per use case in the specification. Layers, counts and commands are in [`docs/testing.md`](docs/testing.md).

## Build and run

1. Clone the repository and open it in an Android Studio version that supports AGP 9.4.
2. Run it on a device or emulator with Android 12 (API 31) or newer:

```bash
./gradlew installDebug
```

The app needs internet access to load data from PokeAPI.

## Credits

Pokémon data comes from [PokeAPI](https://pokeapi.co/), the sprites from [PokeAPI/sprites](https://github.com/PokeAPI/sprites) and the cries from [PokeAPI/cries](https://github.com/PokeAPI/cries). PokeAPI is free and needs no API key; please follow its [fair use policy](https://pokeapi.co/docs/v2#fairuse).

Pokémon and Pokémon character names are trademarks of Nintendo, Game Freak and The Pokémon Company. This is a personal project, not affiliated with them.
