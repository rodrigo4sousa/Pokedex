"""Records the PokeAPI responses the tests use into testing/src/main/resources/fixtures/.

Usage: python testing/scripts/record_fixtures.py

Only the standard library is used. Responses are saved as PokeAPI sends them, except the full
Pokemon list, which is trimmed to the Pokemon the tests expect.
"""
import json
import pathlib
import urllib.request

BASE_URL = "https://pokeapi.co/api/v2/"
OUT = pathlib.Path(__file__).resolve().parent.parent / "src" / "main" / "resources" / "fixtures"

POKEMON = ["bulbasaur", "eevee", "tauros"]
EVOLUTION_CHAINS = [1, 63, 67]
TYPES = ["grass", "poison", "normal"]
# The trimmed list: Bulbasaur's line, Tauros, and Eevee with its first three evolutions.
LIST_NAMES = ["bulbasaur", "ivysaur", "venusaur", "tauros", "eevee", "vaporeon", "jolteon", "flareon"]


def fetch(path):
    request = urllib.request.Request(BASE_URL + path, headers={"User-Agent": "pokedex-fixture-recorder"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)


def save(name, data):
    target = OUT / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"wrote {target.relative_to(OUT.parent)}")


def main():
    page = fetch("pokemon?limit=100000&offset=0")
    by_name = {entry["name"]: entry for entry in page["results"]}
    save("pokemon-list.json", {
        "count": len(LIST_NAMES),
        "next": None,
        "previous": None,
        "results": [by_name[name] for name in LIST_NAMES],
    })
    save("pokemon-species-count.json", fetch("pokemon-species?limit=1"))
    for name in POKEMON:
        save(f"pokemon/{name}.json", fetch(f"pokemon/{name}"))
        save(f"pokemon-species/{name}.json", fetch(f"pokemon-species/{name}"))
    for chain_id in EVOLUTION_CHAINS:
        save(f"evolution-chain/{chain_id}.json", fetch(f"evolution-chain/{chain_id}/"))
    for type_name in TYPES:
        save(f"type/{type_name}.json", fetch(f"type/{type_name}"))


if __name__ == "__main__":
    main()
