#!/usr/bin/env python3
"""
Export data/squads.csv and data/player_ratings.csv from current seed (or generate inline squads).

Run after changing squads in code, or to refresh rating templates when players change.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
_CONFIG = json.loads((ROOT / "project.config.json").read_text(encoding="utf-8"))
PLAYERS_PATH = ROOT / _CONFIG["seed"]["android"] / "players_seed.json"

from player_data_lib import (  # noqa: E402
    RATINGS_CSV,
    RATINGS_CSV_COLUMNS,
    SQUADS_CSV,
    SQUADS_CSV_COLUMNS,
    write_csv,
)


def export_from_players(players: list[dict]) -> None:
    squad_rows = []
    rating_rows = []
    for p in sorted(players, key=lambda x: (x["teamId"], x["shirtNumber"])):
        squad_rows.append({
            "team_id": p["teamId"],
            "shirt_number": p["shirtNumber"],
            "player_name": p["playerName"],
            "position": p["position"],
            "rarity": p["rarity"],
        })
        ratings = p.get("ratings") or {}
        rating_rows.append({
            "player_id": p["playerId"],
            "club_name": p.get("clubName", ""),
            "club_league": p.get("clubLeague", ""),
            "club_logo_url": p.get("clubLogoUrl", ""),
            "overall": ratings.get("overall", ""),
            "pace": ratings.get("pace", ""),
            "shooting": ratings.get("shooting", ""),
            "passing": ratings.get("passing", ""),
            "dribbling": ratings.get("dribbling", ""),
            "defending": ratings.get("defending", ""),
            "physical": ratings.get("physical", ""),
            "diving": ratings.get("diving", ""),
            "handling": ratings.get("handling", ""),
            "kicking": ratings.get("kicking", ""),
            "reflexes": ratings.get("reflexes", ""),
            "speed": ratings.get("speed", ""),
            "positioning": ratings.get("positioning", ""),
        })

    write_csv(SQUADS_CSV, SQUADS_CSV_COLUMNS, squad_rows)
    write_csv(RATINGS_CSV, RATINGS_CSV_COLUMNS, rating_rows)
    print(f"Wrote {SQUADS_CSV.relative_to(ROOT)} ({len(squad_rows)} rows)")
    print(f"Wrote {RATINGS_CSV.relative_to(ROOT)} ({len(rating_rows)} rows)")


def main() -> int:
    if not PLAYERS_PATH.exists():
        print("players_seed.json missing — run: python scripts/generate_seed_data.py", file=sys.stderr)
        return 1
    players = json.loads(PLAYERS_PATH.read_text(encoding="utf-8"))
    export_from_players(players)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
