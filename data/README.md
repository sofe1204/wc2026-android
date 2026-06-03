# Player data (squads + ratings)

Canonical squad lists and per-player attribute numbers are maintained as CSV, then merged into seed JSON.

## Files

| File | Purpose |
|------|---------|
| `squads.csv` | 48 teams × 15 players — overrides inline squads in `generate_seed_data.py` when present |
| `player_ratings.csv` | Club + overall + six outfield or six goalkeeper attributes per `player_id` |

## Pipeline

**Python** (preferred when available):

```bash
python scripts/generate_seed_data.py          # uses squads.csv when present
python scripts/export_player_data_templates.py # refresh CSV from seed
# edit data/player_ratings.csv
python scripts/enrich_player_ratings.py
python scripts/validate_player_ratings.py --strict
python scripts/sync_project.py
```

**Node** (Windows / no Python):

```bash
# Auto-fill from SoFIFA open-data CSV + merge into seed:
node scripts/populate_player_ratings.mjs --enrich

# Or manual / template workflow:
node scripts/upgrade_players_seed.mjs   # add ratings fields + export CSV templates
# edit data/player_ratings.csv
node scripts/enrich_player_ratings.mjs
node scripts/enrich_player_ratings.mjs --strict
```

Optional manual patches for players missing from SoFIFA: `data/ratings_overrides.json` (keyed by `player_id`).

After changing `squads.csv`, run `generate_seed_data.py` (Python) so `player_id` values stay aligned with `{team_id}_{slugified_name}`.

## `squads.csv` columns

`team_id`, `shirt_number`, `player_name`, `position`, `rarity`

- `position`: Goalkeeper | Defender | Midfielder | Forward  
- `rarity`: common | rare | epic | legendary  

## `player_ratings.csv` columns

`player_id`, `club_name`, `club_league`, `overall`, `pace`, `shooting`, `passing`, `dribbling`, `defending`, `physical`, `diving`, `handling`, `kicking`, `reflexes`, `speed`, `positioning`

- **Goalkeepers:** fill `diving` … `positioning`; outfield columns may be `0`.  
- **Outfield:** fill `pace` … `physical`; goalkeeper columns may be `0`.  
- `player_id` must match seed (`{team_id}_{slugified_name}`).

`ratingsComplete` in JSON is set automatically when overall and the correct six attributes for that position are all &gt; 0.
