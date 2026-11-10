package io.github.rodrigo4sousa.pokedex.data.mapper

import io.github.rodrigo4sousa.pokedex.data.remote.dto.CriesDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.DamageRelationsDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.EvolutionChainDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.EvolutionDetailDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.NamedResourceDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.PokemonDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.TypeDto
import io.github.rodrigo4sousa.pokedex.domain.model.Ability
import io.github.rodrigo4sousa.pokedex.domain.model.EvolutionCondition
import io.github.rodrigo4sousa.pokedex.domain.model.Gender
import io.github.rodrigo4sousa.pokedex.domain.model.PokemonSummary
import io.github.rodrigo4sousa.pokedex.domain.model.Sprites
import io.github.rodrigo4sousa.pokedex.domain.model.Stat
import io.github.rodrigo4sousa.pokedex.testutil.fixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteMappersTest {

    private val spriteBase = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"

    // --- Pure helpers ---

    @Test
    fun `idFromUrl reads the number at the end of a PokeAPI url`() {
        assertEquals(25, idFromUrl("https://pokeapi.co/api/v2/pokemon/25/"))
        assertEquals(133, idFromUrl("https://pokeapi.co/api/v2/pokemon-species/133"))
        assertEquals(10034, idFromUrl("https://pokeapi.co/api/v2/pokemon/10034/"))
    }

    @Test
    fun `spriteUrlFor points to the default front sprite on GitHub raw`() {
        assertEquals("${spriteBase}1.png", spriteUrlFor(1))
        assertEquals("${spriteBase}10034.png", spriteUrlFor(10034))
    }

    @Test
    fun `a list entry becomes a summary with its number and sprite`() {
        val entry = NamedResourceDto(name = "pikachu", url = "https://pokeapi.co/api/v2/pokemon/25/")

        assertEquals(PokemonSummary(id = 25, name = "pikachu", spriteUrl = "${spriteBase}25.png"), entry.toPokemonSummary())
    }

    // --- Pokemon ---

    @Test
    fun `bulbasaur maps every field`() {
        val bulbasaur = fixture<PokemonDto>("pokemon/bulbasaur.json").toDomain()

        assertEquals(1, bulbasaur.id)
        assertEquals("bulbasaur", bulbasaur.name)
        assertEquals("bulbasaur", bulbasaur.speciesName)
        assertEquals(7, bulbasaur.height)
        assertEquals(69, bulbasaur.weight)
        assertEquals(listOf("grass", "poison"), bulbasaur.types)
        assertEquals(listOf(Ability("overgrow", isHidden = false), Ability("chlorophyll", isHidden = true)), bulbasaur.abilities)
        assertEquals(
            listOf(
                Stat("hp", 45), Stat("attack", 49), Stat("defense", 49),
                Stat("special-attack", 65), Stat("special-defense", 65), Stat("speed", 45)
            ),
            bulbasaur.stats
        )
        assertEquals(
            Sprites(
                frontDefault = "${spriteBase}1.png",
                frontShiny = "${spriteBase}shiny/1.png",
                frontFemale = null,
                frontShinyFemale = null,
                backDefault = "${spriteBase}back/1.png",
                backShiny = "${spriteBase}back/shiny/1.png",
                backFemale = null,
                backShinyFemale = null
            ),
            bulbasaur.sprites
        )
        assertEquals("https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/1.ogg", bulbasaur.cryUrl)
    }

    @Test
    fun `the official artwork is read from the other sprites`() {
        val bulbasaur = fixture<PokemonDto>("pokemon/bulbasaur.json")

        assertEquals("${spriteBase}other/official-artwork/1.png", bulbasaur.toDomain().artworkUrl)
        assertNull(bulbasaur.copy(sprites = bulbasaur.sprites.copy(other = null)).toDomain().artworkUrl)
    }

    @Test
    fun `eevee keeps its female sprites`() {
        val sprites = fixture<PokemonDto>("pokemon/eevee.json").toDomain().sprites

        assertEquals("${spriteBase}female/133.png", sprites.frontFemale)
        assertEquals("${spriteBase}shiny/female/133.png", sprites.frontShinyFemale)
        assertEquals("${spriteBase}back/female/133.png", sprites.backFemale)
        assertEquals("${spriteBase}back/shiny/female/133.png", sprites.backShinyFemale)
    }

    @Test
    fun `the cry falls back to the legacy recording, or none`() {
        val dto = fixture<PokemonDto>("pokemon/bulbasaur.json")

        assertEquals("legacy.ogg", dto.copy(cries = CriesDto(latest = null, legacy = "legacy.ogg")).toDomain().cryUrl)
        assertNull(dto.copy(cries = CriesDto(latest = null, legacy = null)).toDomain().cryUrl)
        assertNull(dto.copy(cries = null).toDomain().cryUrl)
    }

    // --- Evolution chains ---

    @Test
    fun `bulbasaur has a linear chain with level conditions`() {
        val chain = fixture<EvolutionChainDto>("evolution-chain/1.json").chain.toDomain()

        assertEquals(PokemonSummary(1, "bulbasaur", "${spriteBase}1.png"), chain.pokemon)
        assertNull(chain.condition)
        val ivysaur = chain.evolvesTo.single()
        assertEquals(PokemonSummary(2, "ivysaur", "${spriteBase}2.png"), ivysaur.pokemon)
        assertEquals(EvolutionCondition(trigger = "level-up", minLevel = 16), ivysaur.condition)
        val venusaur = ivysaur.evolvesTo.single()
        assertEquals(PokemonSummary(3, "venusaur", "${spriteBase}3.png"), venusaur.pokemon)
        assertEquals(EvolutionCondition(trigger = "level-up", minLevel = 32), venusaur.condition)
        assertTrue(venusaur.evolvesTo.isEmpty())
    }

    @Test
    fun `eevee branches into eight evolutions`() {
        val chain = fixture<EvolutionChainDto>("evolution-chain/67.json").chain.toDomain()

        assertEquals("eevee", chain.pokemon.name)
        assertEquals(
            listOf("vaporeon", "jolteon", "flareon", "espeon", "umbreon", "leafeon", "glaceon", "sylveon"),
            chain.evolvesTo.map { it.pokemon.name }
        )
        assertTrue(chain.evolvesTo.all { it.evolvesTo.isEmpty() })
        val byName = chain.evolvesTo.associateBy { it.pokemon.name }
        assertEquals(EvolutionCondition(trigger = "use-item", item = "water-stone"), byName.getValue("vaporeon").condition)
        assertEquals(
            EvolutionCondition(trigger = "level-up", minHappiness = 160, timeOfDay = "day"),
            byName.getValue("espeon").condition
        )
    }

    @Test
    fun `the method marked default wins over the others`() {
        val byName = fixture<EvolutionChainDto>("evolution-chain/67.json").chain.toDomain()
            .evolvesTo.associateBy { it.pokemon.name }

        // Leafeon has six methods; the fourth (Sword/Shield) is the default.
        assertEquals(EvolutionCondition(trigger = "use-item", item = "leaf-stone"), byName.getValue("leafeon").condition)
        // Sylveon's default is the second of two.
        assertEquals(
            EvolutionCondition(trigger = "level-up", knownMoveType = "fairy", minHappiness = 160),
            byName.getValue("sylveon").condition
        )
    }

    @Test
    fun `tauros does not evolve`() {
        val chain = fixture<EvolutionChainDto>("evolution-chain/63.json").chain.toDomain()

        assertEquals(PokemonSummary(128, "tauros", "${spriteBase}128.png"), chain.pokemon)
        assertNull(chain.condition)
        assertTrue(chain.evolvesTo.isEmpty())
    }

    @Test
    fun `without a default the last method is used, and gender is mapped`() {
        val condition = listOf(
            detail(isDefault = false, minLevel = 20),
            detail(isDefault = null, gender = 1)
        ).toCondition()

        assertEquals(EvolutionCondition(trigger = "level-up", gender = Gender.Female), condition)
        assertEquals(Gender.Male, listOf(detail(gender = 2)).toCondition()?.gender)
        assertNull(listOf(detail(gender = 3)).toCondition()?.gender)
        assertNull(emptyList<EvolutionDetailDto>().toCondition())
    }

    // --- Types ---

    @Test
    fun `grass and poison combine into the right multipliers`() {
        val relations = listOf("grass", "poison").map { fixture<TypeDto>("type/$it.json").damage_relations }

        assertEquals(
            mapOf(
                "fire" to 2.0, "ice" to 2.0, "flying" to 2.0, "psychic" to 2.0,
                "water" to 0.5, "electric" to 0.5, "fighting" to 0.5, "fairy" to 0.5, "grass" to 0.25
            ),
            relations.toDamageMultipliers()
        )
    }

    @Test
    fun `neutral matchups are left out and immunities are zero`() {
        val normal = fixture<TypeDto>("type/normal.json").damage_relations

        assertEquals(mapOf("fighting" to 2.0, "ghost" to 0.0), listOf(normal).toDamageMultipliers())
        assertEquals(emptyMap<String, Double>(), emptyList<DamageRelationsDto>().toDamageMultipliers())
    }

    @Test
    fun `a type lists the names of its members`() {
        val grass = fixture<TypeDto>("type/grass.json").memberNames()

        assertTrue("bulbasaur" in grass)
        assertTrue("venusaur" in grass)
        assertTrue("tauros" !in grass)
    }

    private fun detail(isDefault: Boolean? = null, minLevel: Int? = null, gender: Int? = null) = EvolutionDetailDto(
        is_default = isDefault,
        trigger = NamedResourceDto("level-up", "https://pokeapi.co/api/v2/evolution-trigger/1/"),
        min_level = minLevel,
        item = null,
        held_item = null,
        known_move = null,
        known_move_type = null,
        min_happiness = null,
        min_affection = null,
        location = null,
        time_of_day = null,
        gender = gender,
        trade_species = null
    )
}
