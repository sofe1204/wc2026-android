#!/usr/bin/env python3
"""
Merge data/player_ratings.csv into players_seed.json (Android + Functions).

Usage:
  python scripts/generate_seed_data.py
  python scripts/enrich_player_ratings.py
  python scripts/enrich_player_ratings.py --strict   # fail if any player incomplete
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
_CONFIG = json.loads((ROOT / "project.config.json").read_text(encoding="utf-8"))
PLAYERS_PATHS = [
    ROOT / _CONFIG["seed"]["android"] / "players_seed.json",
    ROOT / _CONFIG["seed"]["functions"] / "players_seed.json",
]

from player_data_lib import (  # noqa: E402
    RATINGS_CSV,
    load_ratings_csv,
    ratings_complete,
    ratings_from_row,
)


def enrich_players(players: list[dict], ratings_by_id: dict[str, dict[str, str]], strict: bool) -> tuple[list[dict], list[str]]:
    errors: list[str] = []
    enriched: list[dict] = []

    for player in players:
        pid = player["playerId"]
        row = ratings_by_id.get(pid)
        position = player["position"]

        if row is None:
            if strict:
                errors.append(f"Missing ratings row for player_id={pid}")
            enriched.append(player)
            continue

        ratings = ratings_from_row(row)
        complete = ratings_complete(position, ratings)
        if strict and not complete:
            errors.append(f"Incomplete ratings for player_id={pid} ({player['playerName']})")

        player = {
            **player,
            "clubName": row.get("club_name", "").strip(),
            "clubLeague": row.get("club_league", "").strip(),
            "ratings": ratings,
            "ratingsComplete": complete,
        }
        enriched.append(player)

    extra_ids = set(ratings_by_id) - {p["playerId"] for p in players}
    for pid in sorted(extra_ids):
        errors.append(f"Ratings CSV has unknown player_id={pid}")

    return enriched, errors


def main() -> int:
    parser = argparse.ArgumentParser(description="Merge player_ratings.csv into players_seed.json")
    parser.add_argument("--strict", action="store_true", help="Require every player to have complete ratings")
    args = parser.parse_args()

    if not RATINGS_CSV.exists():
        print(f"Missing {RATINGS_CSV.relative_to(ROOT)}", file=sys.stderr)
        print("Run: python scripts/export_player_data_templates.py", file=sys.stderr)
        return 1

    ratings_by_id = load_ratings_csv()
    if not ratings_by_id:
        print(f"No rows in {RATINGS_CSV.relative_to(ROOT)}", file=sys.stderr)
        return 1

    exit_code = 0
    for path in PLAYERS_PATHS:
        if not path.exists():
            print(f"Missing {path.relative_to(ROOT)} — run generate_seed_data.py first", file=sys.stderr)
            return 1
        players = json.loads(path.read_text(encoding="utf-8"))
        enriched, errors = enrich_players(players, ratings_by_id, args.strict)
        path.write_text(json.dumps(enriched, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        complete_count = sum(1 for p in enriched if p.get("ratingsComplete"))
        print(f"{path.relative_to(ROOT)}: {complete_count}/{len(enriched)} players with complete ratings")
        for err in errors:
            print(f"  ! {err}", file=sys.stderr)
            exit_code = 1

    return exit_code


if __name__ == "__main__":
    raise SystemExit(main())
