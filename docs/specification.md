# Specification

User stories and their acceptance criteria. Each criterion is checked by a system test in [`app/src/androidTest/.../system/`](../app/src/androidTest/java/com/example/pokedex/system/), which drives the app through the Compose UI. The tests run against recorded PokeAPI responses, so the Pokémon named below are the ones in those fixtures.

## US01 - Catch a Pokémon

As a user, I want to add a Pokémon to my Pokédex, so that I keep it up to date.

Tests: [`CatchReleaseSystemTest`](../app/src/androidTest/java/com/example/pokedex/system/CatchReleaseSystemTest.kt)

- Tapping the star on a card on Home adds the Pokémon to My Pokédex, and the counter shows "Caught 1 / 1025". (`catchingFromHomeAddsItToMyPokedex`)
- Tapping "Catch" on the detail page turns the button into "Release" and adds the Pokémon to My Pokédex. (`catchingFromTheDetailPageAddsItToMyPokedex`)
- A caught Pokémon stays caught after the activity is recreated: its star on Home still shows it as caught, and the counter still shows "Caught 1 / 1025". (`caughtPokemonIsStillShownAfterTheActivityIsRecreated`)

## US02 - Release a Pokémon

As a user, I want to remove a Pokémon from my Pokédex, so that I keep it up to date.

Tests: [`CatchReleaseSystemTest`](../app/src/androidTest/java/com/example/pokedex/system/CatchReleaseSystemTest.kt)

- Tapping the star of a caught Pokémon in My Pokédex removes it: the screen shows "No Pokémon caught yet." and the counter shows "Caught 0 / 1025". (`releasingFromMyPokedexRemovesIt`)

## US03 - Browse all Pokémon

As a user, I want to list all the Pokémon, so that I can find the one I want.

Tests: [`BrowseAndFilterSystemTest`](../app/src/androidTest/java/com/example/pokedex/system/BrowseAndFilterSystemTest.kt)

- Home lists every Pokémon the server returns. (`listShowsEveryPokemonFromTheServer`)
- Searching for "saur" shows only the Pokémon whose name contains it. (`searchByNameShowsOnlyMatchingPokemon`)
- Selecting the Grass type in the filter panel shows only Grass Pokémon. (`filterByTypeShowsOnlyThatType`)
- When the list request fails, Home shows "No Pokémon to show", the same message as an empty list. There is no separate error screen. (`serverErrorShowsNoPokemonMessage`)

Not covered by a system test: sorting.

## US04 - Inspect a Pokémon

As a user, I want to inspect a Pokémon, so that I better understand its attributes.

Tests: [`ViewDetailSystemTest`](../app/src/androidTest/java/com/example/pokedex/system/ViewDetailSystemTest.kt)

- The detail page shows the Pokédex number, the types and the base stats, the type matchups (Bulbasaur: "Fire 2×", "Psychic 2×", "Grass ¼×") and the evolution chain with its conditions (Ivysaur and Venusaur, with "Lv. 16" and "Lv. 32"). (`bulbasaurShowsStatsTypeMatchupsAndEvolutionChain`)
- A branching evolution chain shows every branch: Eevee shows all eight of its evolutions. (`eeveeShowsEveryBranchOfItsEvolution`)
- A Pokémon that doesn't evolve shows its matchups (Tauros: "Fighting 2×", "Ghost 0×") and no Evolution section. (`taurosHasNoEvolutionSection`)

Not covered by a system test: the sprite toggles, cry playback, and tapping an evolution to open it.

## US05 - Review My Pokédex

As a user, I want to review my Pokédex, so that I can see which Pokémon I have already caught.

Tests: [`MyPokedexSystemTest`](../app/src/androidTest/java/com/example/pokedex/system/MyPokedexSystemTest.kt)

- With nothing caught, My Pokédex shows "Caught 0 / 1025" and "No Pokémon caught yet." (`showsAnEmptyMessageWhenNothingIsCaught`)
- It shows only the caught Pokémon, with the count ("Caught 2 / 1025" after catching Bulbasaur and Tauros). (`showsOnlyCaughtPokemonWithTheCount`)
- Searching filters the caught Pokémon by name. (`searchFindsCaughtPokemonByName`)

Not covered by a system test: sorting and the type filter on this screen.

## US06 - Guess the daily Pokémon

As a user, I want to guess a hidden Pokémon every day, so that I test what I know and keep a streak going.

Tests: [`GuessPokemonSystemTest`](../app/src/androidTest/java/com/example/pokedex/system/GuessPokemonSystemTest.kt) (the test fixes the date, so the day's Pokémon is Bulbasaur)

- The Guess tab shows the day's Pokémon as a silhouette with three hearts. Until the round ends, nothing on screen names it, not even the silhouette's description ("Hidden Pokémon"). (`guessingRightRevealsThePokemonAndStartsAStreak`)
- Typing the name in any case and with spaces or hyphens ("BULBA-saur") and tapping Guess reveals the Pokémon: "You got it!", its name, "Streak: 1" and "Come back tomorrow for a new Pokémon." "Open details" opens its detail page. (`guessingRightRevealsThePokemonAndStartsAStreak`)
- A wrong Pokémon costs a heart and is listed under the field ("2 of 3 hearts left", "Ivysaur"). A name that isn't a Pokémon shows "“Agumon” isn't a Pokémon." and costs nothing. After the third wrong guess the screen shows "Out of hearts!", the Pokémon's name, "Streak: 0" and "Come back tomorrow for a new Pokémon." (`threeWrongGuessesRevealThePokemonAndResetTheStreak`)

Not covered by a system test: the streak over several days, a missed day, and the round surviving a restart (covered by `GuessViewModelTest`); the silhouette's colours and the reveal animation.
