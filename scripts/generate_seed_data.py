#!/usr/bin/env python3
"""Generate teams_seed.json, players_seed.json, stickers_seed.json for World Cup 2026."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
_CONFIG = json.loads((ROOT / "project.config.json").read_text(encoding="utf-8"))
OUTPUT = ROOT / _CONFIG["seed"]["android"]
FUNCTIONS_SEED = ROOT / _CONFIG["seed"]["functions"]
PLAYERS_PER_TEAM = _CONFIG["game"]["playersPerTeam"]
TOTAL_TEAMS = _CONFIG["game"]["totalTeams"]
STICKERS_PER_TEAM = _CONFIG["game"]["stickersPerTeam"]
TOTAL_STICKERS = _CONFIG["game"]["totalStickers"]

from official_squads_lib import (  # noqa: E402
    build_teams_list,
    load_official_squads,
    write_squads_csv,
)
from player_data_lib import empty_ratings, player_id_for  # noqa: E402


def anime_prompt(player_name: str, country: str, shirt: int, position: str) -> str:
    return (
        f"Anime-style collectible football sticker portrait of {player_name} as an inspired stylized character, "
        f"not copied from a photo, wearing a generic {country}-inspired football shirt number {shirt}, "
        f"{position} identity, confident expression, stadium lights, vibrant card border, clean line art."
    )


def main():
    squads = load_official_squads()
    write_squads_csv(squads)
    teams = build_teams_list()

    teams_out = []
    players_out = []
    stickers_out = []

    for team_id, country, group, code, flag, primary, secondary in teams:
        squad = squads[team_id]
        if len(squad) not in (25, 26):
            raise ValueError(f"{team_id}: expected 25–26 players, got {len(squad)}")

        teams_out.append({
            "teamId": team_id,
            "countryName": country,
            "group": group,
            "teamCode": code,
            "flagEmoji": flag,
            "customEmblemUrl": "",
            "primaryColor": primary,
            "secondaryColor": secondary,
            "isActive": True,
        })
        stickers_out.append({
            "stickerId": f"{code}-000",
            "stickerNumber": 0,
            "playerId": "",
            "teamId": team_id,
            "countryName": country,
            "group": group,
            "rarity": "epic",
            "imageUrl": "",
            "isActive": True,
        })
        for shirt, pname, pos, rarity in squad:
            player_id = player_id_for(team_id, pname)
            players_out.append({
                "playerId": player_id,
                "teamId": team_id,
                "countryName": country,
                "group": group,
                "shirtNumber": shirt,
                "playerName": pname,
                "position": pos,
                "rarity": rarity,
                "animeStickerPrompt": anime_prompt(pname, country, shirt, pos),
                "imageUrl": "",
                "clubName": "",
                "clubLeague": "",
                "ratings": empty_ratings(),
                "ratingsComplete": False,
                "isActive": True,
            })
            stickers_out.append({
                "stickerId": f"{code}-{shirt:03d}",
                "stickerNumber": shirt,
                "playerId": player_id,
                "teamId": team_id,
                "countryName": country,
                "group": group,
                "rarity": rarity,
                "imageUrl": "",
                "isActive": True,
            })

    assert len(teams_out) == TOTAL_TEAMS
    assert len(players_out) == sum(len(squads[t[0]]) for t in teams)
    assert len(stickers_out) == TOTAL_STICKERS

    OUTPUT.mkdir(parents=True, exist_ok=True)
    FUNCTIONS_SEED.mkdir(parents=True, exist_ok=True)

    for path in [OUTPUT, FUNCTIONS_SEED]:
        (path / "teams_seed.json").write_text(json.dumps(teams_out, indent=2, ensure_ascii=False))
        (path / "players_seed.json").write_text(json.dumps(players_out, indent=2, ensure_ascii=False))
        (path / "stickers_seed.json").write_text(json.dumps(stickers_out, indent=2, ensure_ascii=False))

    print(
        f"Generated {len(teams_out)} teams, {len(players_out)} players, "
        f"{len(stickers_out)} stickers (official FIFA WC 2026 squads)"
    )


if __name__ == "__main__":
    main()
