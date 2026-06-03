#!/usr/bin/env python3
"""Validate players_seed.json ratings coverage. Use --strict in CI when CSV is fully populated."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
_CONFIG = json.loads((ROOT / "project.config.json").read_text(encoding="utf-8"))
PLAYERS_PATH = ROOT / _CONFIG["seed"]["android"] / "players_seed.json"

from player_data_lib import ratings_complete  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--strict", action="store_true", help="Fail unless all 720 players have complete ratings")
    args = parser.parse_args()

    if not PLAYERS_PATH.exists():
        print(f"Missing {PLAYERS_PATH}", file=sys.stderr)
        return 1

    players = json.loads(PLAYERS_PATH.read_text(encoding="utf-8"))
    incomplete = []
    for p in players:
        ratings = p.get("ratings") or {}
        if not p.get("ratingsComplete") and not ratings_complete(p["position"], ratings):
            incomplete.append(p["playerId"])

    complete = len(players) - len(incomplete)
    print(f"Ratings complete: {complete}/{len(players)}")

    if args.strict and incomplete:
        print(f"FAIL: {len(incomplete)} players missing complete ratings", file=sys.stderr)
        for pid in incomplete[:20]:
            print(f"  - {pid}", file=sys.stderr)
        if len(incomplete) > 20:
            print(f"  ... and {len(incomplete) - 20} more", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
