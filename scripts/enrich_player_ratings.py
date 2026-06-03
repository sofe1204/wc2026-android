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
import csv
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
    RATINGS_CSV_COLUMNS,
    empty_ratings,
    fallback_ratings,
    load_ratings_csv,
    ratings_complete,
    ratings_from_row,
    ratings_to_csv_row,
)


def enrich_players(
    players: list[dict],
    ratings_by_id: dict[str, dict[str, str]],
    strict: bool,
    use_fallback: bool,
) -> tuple[list[dict], list[str], int]:
    errors: list[str] = []
    enriched: list[dict] = []
    fallback_count = 0

    for player in players:
        pid = player["playerId"]
        row = ratings_by_id.get(pid)
        position = player["position"]
        rarity = str(player.get("rarity", "common"))
        shirt = int(player.get("shirtNumber", 0))

        club_name = row.get("club_name", "").strip() if row else ""
        club_league = row.get("club_league", "").strip() if row else ""
        ratings = ratings_from_row(row) if row else empty_ratings()
        complete = ratings_complete(position, ratings) if row else False

        if not complete and use_fallback and not strict:
            ratings = fallback_ratings(position, rarity, shirt)
            complete = True
            fallback_count += 1
            ratings_by_id[pid] = ratings_to_csv_row(pid, ratings, club_name, club_league)
        elif row is None and strict:
            errors.append(f"Missing ratings row for player_id={pid}")
            enriched.append(player)
            continue
        elif strict and not complete:
            errors.append(f"Incomplete ratings for player_id={pid} ({player['playerName']})")
            enriched.append(player)
            continue

        player = {
            **player,
            "clubName": club_name,
            "clubLeague": club_league,
            "ratings": ratings,
            "ratingsComplete": complete,
        }
        enriched.append(player)

    extra_ids = set(ratings_by_id) - {p["playerId"] for p in players}
    for pid in sorted(extra_ids):
        errors.append(f"Ratings CSV has unknown player_id={pid}")

    return enriched, errors, fallback_count


def main() -> int:
    parser = argparse.ArgumentParser(description="Merge player_ratings.csv into players_seed.json")
    parser.add_argument("--strict", action="store_true", help="Require every player to have complete ratings")
    parser.add_argument(
        "--no-fallback",
        action="store_true",
        help="Do not synthesize stats for players missing SoFIFA data",
    )
    args = parser.parse_args()
    use_fallback = not args.no_fallback and not args.strict

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
        enriched, errors, fallback_count = enrich_players(
            players, ratings_by_id, args.strict, use_fallback
        )
        path.write_text(json.dumps(enriched, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        complete_count = sum(1 for p in enriched if p.get("ratingsComplete"))
        msg = f"{path.relative_to(ROOT)}: {complete_count}/{len(enriched)} players with complete ratings"
        if fallback_count:
            msg += f" ({fallback_count} fallback)"
        print(msg)
        for err in errors:
            print(f"  ! {err}", file=sys.stderr)
            exit_code = 1

    if use_fallback and ratings_by_id:
        with RATINGS_CSV.open("w", encoding="utf-8", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=RATINGS_CSV_COLUMNS)
            writer.writeheader()
            for pid in sorted(ratings_by_id):
                writer.writerow(ratings_by_id[pid])
        print(f"Updated {RATINGS_CSV.relative_to(ROOT)} ({len(ratings_by_id)} rows)")

    return exit_code


if __name__ == "__main__":
    raise SystemExit(main())
