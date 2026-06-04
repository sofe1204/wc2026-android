#!/usr/bin/env python3
"""Shared helpers for squad CSV and player ratings CSV pipelines."""
from __future__ import annotations

import csv
import json
import re
import unicodedata
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = ROOT / "data"
SQUADS_CSV = DATA_DIR / "squads.csv"
RATINGS_CSV = DATA_DIR / "player_ratings.csv"

RATINGS_CSV_COLUMNS = [
    "player_id",
    "club_name",
    "club_league",
    "club_logo_url",
    "overall",
    "pace",
    "shooting",
    "passing",
    "dribbling",
    "defending",
    "physical",
    "diving",
    "handling",
    "kicking",
    "reflexes",
    "speed",
    "positioning",
]

SQUADS_CSV_COLUMNS = [
    "team_id",
    "shirt_number",
    "player_name",
    "position",
    "rarity",
]

POSITIONS = {"Goalkeeper", "Defender", "Midfielder", "Forward"}
RARITIES = {"common", "rare", "epic", "legendary"}


def slugify(name: str) -> str:
    s = unicodedata.normalize("NFKD", name)
    s = s.encode("ascii", "ignore").decode("ascii")
    s = s.lower()
    s = re.sub(r"[^a-z0-9]+", "_", s)
    return s.strip("_")


def player_id_for(team_id: str, player_name: str) -> str:
    return f"{team_id}_{slugify(player_name)}"


def empty_ratings() -> dict[str, Any]:
    return {
        "overall": 0,
        "pace": 0,
        "shooting": 0,
        "passing": 0,
        "dribbling": 0,
        "defending": 0,
        "physical": 0,
        "diving": 0,
        "handling": 0,
        "kicking": 0,
        "reflexes": 0,
        "speed": 0,
        "positioning": 0,
    }


def ratings_from_row(row: dict[str, str]) -> dict[str, int]:
    out = empty_ratings()
    for key in out:
        raw = row.get(key, "").strip()
        if raw:
            out[key] = int(raw)
    return out


def ratings_complete(position: str, ratings: dict[str, int]) -> bool:
    if ratings.get("overall", 0) <= 0:
        return False
    if position == "Goalkeeper":
        keys = ("diving", "handling", "kicking", "reflexes", "speed", "positioning")
    else:
        keys = ("pace", "shooting", "passing", "dribbling", "defending", "physical")
    return all(ratings.get(k, 0) > 0 for k in keys)


def _clamp_stat(value: int) -> int:
    return max(1, min(99, value))


def fallback_ratings(position: str, rarity: str, shirt: int) -> dict[str, int]:
    """Deterministic placeholder stats when SoFIFA has no match (UI still shows graphs)."""
    base = {"legendary": 88, "epic": 82, "rare": 76, "common": 71}.get(rarity.lower(), 72)
    deltas = [(shirt * 3 + i * 7) % 9 - 4 for i in range(6)]
    out = empty_ratings()
    out["overall"] = base
    if position == "Goalkeeper":
        keys = ("diving", "handling", "kicking", "reflexes", "speed", "positioning")
        for i, key in enumerate(keys):
            out[key] = _clamp_stat(base + deltas[i])
    else:
        keys = ("pace", "shooting", "passing", "dribbling", "defending", "physical")
        for i, key in enumerate(keys):
            out[key] = _clamp_stat(base + deltas[i])
    return out


def ratings_to_csv_row(
    player_id: str,
    ratings: dict[str, int],
    club_name: str = "",
    club_league: str = "",
    club_logo_url: str = "",
) -> dict[str, str]:
    row: dict[str, str] = {
        "player_id": player_id,
        "club_name": club_name,
        "club_league": club_league,
        "club_logo_url": club_logo_url,
    }
    for key in empty_ratings():
        row[key] = str(ratings.get(key, 0))
    return row


def load_squads_csv(path: Path = SQUADS_CSV) -> dict[str, list[tuple[int, str, str, str]]] | None:
    if not path.exists():
        return None
    squads: dict[str, list[tuple[int, str, str, str]]] = {}
    with path.open(encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            team_id = row["team_id"].strip()
            shirt = int(row["shirt_number"])
            name = row["player_name"].strip()
            position = row["position"].strip()
            rarity = row["rarity"].strip().lower()
            if position not in POSITIONS:
                raise ValueError(f"Invalid position {position!r} for {team_id} #{shirt}")
            if rarity not in RARITIES:
                raise ValueError(f"Invalid rarity {rarity!r} for {team_id} #{shirt}")
            squads.setdefault(team_id, []).append((shirt, name, position, rarity))
    for team_id, squad in squads.items():
        if len(squad) not in (25, 26):
            raise ValueError(
                f"squads.csv: team {team_id} has {len(squad)} players, expected 25–26"
            )
    return squads


def load_ratings_csv(path: Path = RATINGS_CSV) -> dict[str, dict[str, str]]:
    if not path.exists():
        return {}
    by_id: dict[str, dict[str, str]] = {}
    with path.open(encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        fieldnames = reader.fieldnames or []
        missing = set(RATINGS_CSV_COLUMNS) - set(fieldnames)
        if missing:
            raise ValueError(f"player_ratings.csv missing columns: {sorted(missing)}")
        for row in reader:
            pid = row["player_id"].strip()
            if not pid:
                continue
            if pid in by_id:
                raise ValueError(f"Duplicate player_id in ratings CSV: {pid}")
            by_id[pid] = {k: row.get(k, "").strip() for k in RATINGS_CSV_COLUMNS}
    return by_id


def write_csv(path: Path, columns: list[str], rows: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=columns, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
