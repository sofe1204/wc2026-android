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
# Full pipeline: real squads → seed JSON → SoFIFA ratings:
npm run data:full

# Or step by step:
npm run generate:seed
npm run populate:ratings
```

```bash
# Ratings only (after squads/seed already correct):
node scripts/populate_player_ratings.mjs --enrich

# Or manual / template workflow:
node scripts/upgrade_players_seed.mjs   # add ratings fields + export CSV templates
# edit data/player_ratings.csv
node scripts/enrich_player_ratings.mjs
node scripts/enrich_player_ratings.mjs --strict
```

### Non-destructive ratings completion

`populate_player_ratings.mjs` defaults to **fill-only**: rows that are already complete in `player_ratings.csv` are copied unchanged. Use `--force` for a full rebuild (destructive). `enrich_player_ratings.mjs` only updates seed players when the CSV row is complete; incomplete CSV rows leave existing seed ratings intact.

```bash
# Baseline before changes
node scripts/verify_ratings_no_regression.mjs --snapshot

# Layer 1: matcher + preserve populate
npm run populate:ratings

# Review gaps
npm run export:unmatched

# Layer 2: SoFIFA overrides for priority IDs → populate again
npm run build:overrides
npm run populate:ratings

# Layer 3: strict last-name backfill for empty CSV rows
npm run fill:ratings-empty
node scripts/enrich_player_ratings.mjs

# Must pass (no lost complete ratings / OVR changes)
node scripts/verify_ratings_no_regression.mjs

# Or run the full chain (after --snapshot):
npm run ratings:complete
```

Optional SoFIFA-derived patches: `data/ratings_overrides.json` (merged into empty fields only during populate).

After changing squads, run `npm run generate:seed` (Node) or `python scripts/generate_seed_data.py` so `player_id` values stay aligned with `{team_id}_{slugified_name}`.

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
