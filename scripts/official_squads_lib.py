#!/usr/bin/env python3
"""Load FIFA WC 2026 official squads and build seed-ready squad tuples."""
from __future__ import annotations

import csv
import json
from pathlib import Path

from player_data_lib import ROOT, SQUADS_CSV, SQUADS_CSV_COLUMNS

OFFICIAL_SQUADS_JSON = ROOT / "data" / "official_squads_2026.json"
FIFA_TEAMS_JSON = ROOT / "data" / "fifa_teams_2026.json"

# team_id -> (country, group, code, flag, primary, secondary)
TEAM_META: dict[str, tuple[str, str, str, str, str, str]] = {
    "mexico": ("Mexico", "Group A", "MEX", "🇲🇽", "#006847", "#FFFFFF"),
    "south_africa": ("South Africa", "Group A", "RSA", "🇿🇦", "#007A4D", "#FFB612"),
    "south_korea": ("South Korea", "Group A", "KOR", "🇰🇷", "#CD2E3A", "#0047A0"),
    "czechia": ("Czechia", "Group A", "CZE", "🇨🇿", "#11457E", "#D7141A"),
    "canada": ("Canada", "Group B", "CAN", "🇨🇦", "#FF0000", "#FFFFFF"),
    "switzerland": ("Switzerland", "Group B", "SUI", "🇨🇭", "#FF0000", "#FFFFFF"),
    "qatar": ("Qatar", "Group B", "QAT", "🇶🇦", "#8D1B3D", "#FFFFFF"),
    "bosnia_herzegovina": ("Bosnia and Herzegovina", "Group B", "BIH", "🇧🇦", "#002395", "#FECB00"),
    "brazil": ("Brazil", "Group C", "BRA", "🇧🇷", "#009C3B", "#FFDF00"),
    "morocco": ("Morocco", "Group C", "MAR", "🇲🇦", "#C1272D", "#006233"),
    "scotland": ("Scotland", "Group C", "SCO", "🏴󠁧󠁢󠁳󠁣󠁴󠁿", "#005EB8", "#FFFFFF"),
    "haiti": ("Haiti", "Group C", "HAI", "🇭🇹", "#00209F", "#D21034"),
    "united_states": ("United States", "Group D", "USA", "🇺🇸", "#3C3B6E", "#B22234"),
    "paraguay": ("Paraguay", "Group D", "PAR", "🇵🇾", "#D52B1E", "#0038A8"),
    "australia": ("Australia", "Group D", "AUS", "🇦🇺", "#00008B", "#FFCD00"),
    "turkiye": ("Türkiye", "Group D", "TUR", "🇹🇷", "#E30A17", "#FFFFFF"),
    "germany": ("Germany", "Group E", "GER", "🇩🇪", "#000000", "#DD0000"),
    "ecuador": ("Ecuador", "Group E", "ECU", "🇪🇨", "#FFD100", "#0033A0"),
    "ivory_coast": ("Ivory Coast", "Group E", "CIV", "🇨🇮", "#F77F00", "#009E60"),
    "curacao": ("Curaçao", "Group E", "CUW", "🇨🇼", "#002B7F", "#F9E814"),
    "netherlands": ("Netherlands", "Group F", "NED", "🇳🇱", "#FF6600", "#21468B"),
    "japan": ("Japan", "Group F", "JPN", "🇯🇵", "#BC002D", "#FFFFFF"),
    "tunisia": ("Tunisia", "Group F", "TUN", "🇹🇳", "#E70013", "#FFFFFF"),
    "sweden": ("Sweden", "Group F", "SWE", "🇸🇪", "#006AA7", "#FECC00"),
    "belgium": ("Belgium", "Group G", "BEL", "🇧🇪", "#000000", "#FAE042"),
    "iran": ("Iran", "Group G", "IRN", "🇮🇷", "#239F40", "#FFFFFF"),
    "egypt": ("Egypt", "Group G", "EGY", "🇪🇬", "#CE1126", "#FFFFFF"),
    "new_zealand": ("New Zealand", "Group G", "NZL", "🇳🇿", "#00247D", "#FFFFFF"),
    "spain": ("Spain", "Group H", "ESP", "🇪🇸", "#AA151B", "#F1BF00"),
    "uruguay": ("Uruguay", "Group H", "URU", "🇺🇾", "#0038A8", "#FFFFFF"),
    "saudi_arabia": ("Saudi Arabia", "Group H", "KSA", "🇸🇦", "#006C35", "#FFFFFF"),
    "cape_verde": ("Cape Verde", "Group H", "CPV", "🇨🇻", "#003893", "#FFFFFF"),
    "france": ("France", "Group I", "FRA", "🇫🇷", "#0055A4", "#EF4135"),
    "senegal": ("Senegal", "Group I", "SEN", "🇸🇳", "#00853F", "#FDEF42"),
    "norway": ("Norway", "Group I", "NOR", "🇳🇴", "#BA0C2F", "#00205B"),
    "iraq": ("Iraq", "Group I", "IRQ", "🇮🇶", "#CE1126", "#FFFFFF"),
    "argentina": ("Argentina", "Group J", "ARG", "🇦🇷", "#74ACDF", "#FFFFFF"),
    "austria": ("Austria", "Group J", "AUT", "🇦🇹", "#ED2939", "#FFFFFF"),
    "algeria": ("Algeria", "Group J", "ALG", "🇩🇿", "#006233", "#FFFFFF"),
    "jordan": ("Jordan", "Group J", "JOR", "🇯🇴", "#007A3D", "#FFFFFF"),
    "portugal": ("Portugal", "Group K", "POR", "🇵🇹", "#006600", "#FF0000"),
    "colombia": ("Colombia", "Group K", "COL", "🇨🇴", "#FCD116", "#003893"),
    "uzbekistan": ("Uzbekistan", "Group K", "UZB", "🇺🇿", "#1EB53A", "#FFFFFF"),
    "dr_congo": ("DR Congo", "Group K", "COD", "🇨🇩", "#007FFF", "#F7D618"),
    "england": ("England", "Group L", "ENG", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "#FFFFFF", "#CE1141"),
    "croatia": ("Croatia", "Group L", "CRO", "🇭🇷", "#FF0000", "#FFFFFF"),
    "panama": ("Panama", "Group L", "PAN", "🇵🇦", "#005293", "#DC143C"),
    "ghana": ("Ghana", "Group L", "GHA", "🇬🇭", "#006B3F", "#FCD116"),
}

LEGENDARY_HINTS = {
    "lionel messi", "kylian mbappé", "kylian mbappe", "cristiano ronaldo", "neymar",
    "vinícius júnior", "vinicius junior", "harry kane", "jude bellingham", "lamine yamal",
    "erling haaland", "alphonso davies", "jonathan david", "raúl jiménez", "raul jimenez",
    "guillermo ochoa", "david alaba", "marko arnautović", "marko arnautovic",
    "marcel sabitzer", "luka modrić", "luka modric", "kevin de bruyne", "rodri",
    "florian wirtz", "jamal musiala", "kylian mbappé",
}


def build_teams_list() -> list[tuple[str, str, str, str, str, str, str]]:
    if not FIFA_TEAMS_JSON.exists():
        return [(tid, *TEAM_META[tid]) for tid in TEAM_META]
    data = json.loads(FIFA_TEAMS_JSON.read_text(encoding="utf-8"))
    teams: list[tuple[str, str, str, str, str, str, str]] = []
    for letter in "ABCDEFGHIJKL":
        for team_id in data["groups"][letter]:
            teams.append((team_id, *TEAM_META[team_id]))
    return teams


def assign_rarity(name: str, position: str, shirt: int) -> str:
    lower = name.lower()
    if lower in LEGENDARY_HINTS:
        return "legendary"
    if position == "Forward" and shirt in (9, 10, 11):
        return "epic"
    if position == "Midfielder" and shirt in (8, 10):
        return "epic"
    if position == "Defender" and shirt in (3, 4, 5):
        return "rare"
    if position == "Goalkeeper" and shirt in (1, 12, 13):
        return "rare"
    if position == "Forward":
        return "rare"
    return "common"


def load_official_squads() -> dict[str, list[tuple[int, str, str, str]]]:
    if not OFFICIAL_SQUADS_JSON.exists():
        raise FileNotFoundError(
            f"Missing {OFFICIAL_SQUADS_JSON.relative_to(ROOT)}. "
            "Run: python3 scripts/import_official_wc2026_squads.py"
        )
    data = json.loads(OFFICIAL_SQUADS_JSON.read_text(encoding="utf-8"))
    squads: dict[str, list[tuple[int, str, str, str]]] = {}
    for team_id, players in data["teams"].items():
        if team_id not in TEAM_META:
            raise KeyError(f"official_squads: unknown team_id {team_id!r}")
        n = len(players)
        if n not in (25, 26):
            raise ValueError(f"official_squads: {team_id} has {n} players (expected 25–26)")
        rows = []
        for p in sorted(players, key=lambda x: x["shirt"]):
            shirt = int(p["shirt"])
            name = str(p["name"]).strip()
            position = str(p["position"]).strip()
            rows.append((shirt, name, position, assign_rarity(name, position, shirt)))
        squads[team_id] = rows
    expected = set(TEAM_META)
    if set(squads) != expected:
        raise ValueError(
            f"official_squads team mismatch missing={expected - set(squads)} "
            f"extra={set(squads) - expected}"
        )
    return squads


def write_squads_csv(squads: dict[str, list[tuple[int, str, str, str]]]) -> None:
    SQUADS_CSV.parent.mkdir(parents=True, exist_ok=True)
    with SQUADS_CSV.open("w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(SQUADS_CSV_COLUMNS)
        for team_id in sorted(squads):
            for shirt, name, position, rarity in squads[team_id]:
                writer.writerow([team_id, shirt, name, position, rarity])
    total = sum(len(v) for v in squads.values())
    print(f"Wrote {SQUADS_CSV.relative_to(ROOT)} ({total} rows)")
