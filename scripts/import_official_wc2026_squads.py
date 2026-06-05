#!/usr/bin/env python3
"""
Re-fetch FIFA World Cup 2026 squads from Wikipedia and write data/official_squads_2026.json.

Source: https://en.wikipedia.org/wiki/2026_FIFA_World_Cup_squads
After running, regenerate seed: python3 scripts/generate_seed_data.py
"""
from __future__ import annotations

import json
import re
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "data" / "official_squads_2026.json"
RAW_CACHE = ROOT / "data" / "wikipedia_squads_raw.json"

NAME_TO_ID = {
    "Mexico": "mexico",
    "South Africa": "south_africa",
    "Korea Republic": "south_korea",
    "South Korea": "south_korea",
    "Czech Republic": "czechia",
    "Czechia": "czechia",
    "Canada": "canada",
    "Bosnia and Herzegovina": "bosnia_herzegovina",
    "Qatar": "qatar",
    "Switzerland": "switzerland",
    "Brazil": "brazil",
    "Morocco": "morocco",
    "Haiti": "haiti",
    "Scotland": "scotland",
    "United States": "united_states",
    "Paraguay": "paraguay",
    "Australia": "australia",
    "Türkiye": "turkiye",
    "Turkey": "turkiye",
    "Germany": "germany",
    "Ecuador": "ecuador",
    "Ivory Coast": "ivory_coast",
    "Côte d'Ivoire": "ivory_coast",
    "Curaçao": "curacao",
    "Netherlands": "netherlands",
    "Japan": "japan",
    "Tunisia": "tunisia",
    "Sweden": "sweden",
    "Belgium": "belgium",
    "Iran": "iran",
    "IR Iran": "iran",
    "Egypt": "egypt",
    "New Zealand": "new_zealand",
    "Spain": "spain",
    "Cape Verde": "cape_verde",
    "Cabo Verde": "cape_verde",
    "Saudi Arabia": "saudi_arabia",
    "Uruguay": "uruguay",
    "France": "france",
    "Senegal": "senegal",
    "Norway": "norway",
    "Iraq": "iraq",
    "Argentina": "argentina",
    "Algeria": "algeria",
    "Austria": "austria",
    "Jordan": "jordan",
    "Portugal": "portugal",
    "Colombia": "colombia",
    "Uzbekistan": "uzbekistan",
    "DR Congo": "dr_congo",
    "Congo DR": "dr_congo",
    "England": "england",
    "Croatia": "croatia",
    "Panama": "panama",
    "Ghana": "ghana",
}

POS_MAP = {"GK": "Goalkeeper", "DF": "Defender", "MF": "Midfielder", "FW": "Forward"}
SKIP_SECTIONS = {"Notes", "References", "External links", "Summary", "Player representation"}


def clean_name(raw: str) -> str:
    s = re.sub(r"\{\{[^}]+\}\}", "", raw)
    s = re.sub(r"<[^>]+>", "", s)
    s = re.sub(r"\[\[([^|\]]+)\|([^\]]+)\]\]", r"\2", s)
    s = re.sub(r"\[\[([^\]|]+?)(?:\s*\([^)]*\))?\]\]", r"\1", s)
    s = re.sub(r"\[\[([^\]|]+?)(?:\s*\([^)]*\))?", r"\1", s)
    s = re.sub(r"'''", "", s).strip()
    return s


def parse_pos(cell: str) -> str:
    m = re.search(r"\b(GK|DF|MF|FW)\b", cell)
    return POS_MAP.get(m.group(1) if m else "MF", "Midfielder")


def parse_nat_fs_fields(line: str) -> dict[str, str] | None:
    if "nat fs" not in line or "player" not in line:
        return None
    fields: dict[str, str] = {}
    for m in re.finditer(r"\|([^|=]+)=([^|{}]+)", line):
        fields[m.group(1).strip().lower()] = m.group(2).strip()
    if "no" not in fields or "name" not in fields:
        return None
    return fields


def player_entry_from_fields(fields: dict[str, str], *, club: str = "", club_nat: str = "") -> dict:
    shirt = int(fields["no"])
    pos = parse_pos(fields.get("pos", "MF"))
    name = clean_name(fields["name"])
    entry: dict = {"shirt": shirt, "name": name, "position": pos}
    wiki_club = clean_name(fields.get("club", club))
    wiki_nat = fields.get("clubnat", club_nat).strip()
    if wiki_club:
        entry["club"] = wiki_club
    if wiki_nat:
        entry["club_nat"] = wiki_nat
    return entry


def parse_player_templates(wt: str) -> dict[str, list[dict]]:
    squads: dict[str, list[dict]] = {}
    current: str | None = None
    for line in wt.splitlines():
        m = re.match(r"^===([^=]+)===$", line.strip())
        if m:
            title = m.group(1).strip()
            current = NAME_TO_ID.get(title) if title not in SKIP_SECTIONS else None
            if current:
                squads[current] = []
            continue
        if not current:
            continue
        fields = parse_nat_fs_fields(line)
        if fields:
            name = clean_name(fields["name"])
            if name:
                squads[current].append(player_entry_from_fields(fields))
            continue
        if line.strip().startswith("|") and not line.strip().startswith("|-") and "Player" not in line:
            cells = [c.strip() for c in line.split("|")[1:-1]]
            if len(cells) >= 3 and cells[0].isdigit():
                shirt = int(cells[0])
                pos = parse_pos(cells[1])
                name = clean_name(cells[2])
                club = clean_name(cells[6]) if len(cells) >= 7 else ""
                if name and not name.startswith("{{"):
                    entry = {"shirt": shirt, "name": name, "position": pos}
                    if club:
                        entry["club"] = club
                    squads[current].append(entry)
    return squads


def fetch_wikitext() -> str:
    url = (
        "https://en.wikipedia.org/w/api.php?"
        "action=parse&page=2026_FIFA_World_Cup_squads&prop=wikitext&format=json"
    )
    req = urllib.request.Request(url, headers={"User-Agent": "WC2026App/1.0 (seed import)"})
    with urllib.request.urlopen(req, timeout=60) as resp:
        data = json.load(resp)
    RAW_CACHE.write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")
    return data["parse"]["wikitext"]["*"]


def main() -> None:
    wt = fetch_wikitext()
    squads = parse_player_templates(wt)
    from official_squads_lib import TEAM_META

    missing = set(TEAM_META) - set(squads)
    if missing:
        raise SystemExit(f"Missing teams after parse: {sorted(missing)}")
    bad = {k: len(v) for k, v in squads.items() if len(v) not in (25, 26)}
    if bad:
        print("WARN: teams not 25–26 players:", bad)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(
        json.dumps({"source": "wikipedia", "teams": squads}, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    print(f"Wrote {OUT.relative_to(ROOT)} ({sum(len(v) for v in squads.values())} players)")


if __name__ == "__main__":
    main()
