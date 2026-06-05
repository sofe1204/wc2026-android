# Player data (squads + ratings)

Canonical squad lists and per-player attribute numbers are maintained as CSV, then merged into seed JSON.

## Files

| File | Purpose |
|------|---------|
| `official_squads_2026.json` | Canonical FIFA WC 2026 squads (from Wikipedia / FIFA lists) |
| `fifa_teams_2026.json` | Official group draw (groups A–L) |
| `squads.csv` | Generated from official squads when you run `generate_seed_data.py` |
| `player_ratings.csv` | Club + overall + six outfield or six goalkeeper attributes per `player_id` |
| `official_squads_2026.json` | **Club names** from [Wikipedia FIFA squads](https://en.wikipedia.org/wiki/2026_FIFA_World_Cup_squads) (tournament source) |

## Pipeline

**Python** (preferred when available):

```bash
python scripts/generate_seed_data.py          # uses squads.csv when present
python scripts/export_player_data_templates.py # refresh CSV from seed
# edit data/player_ratings.csv
python scripts/enrich_player_ratings.py   # merges CSV; uses rarity-based fallback for SoFIFA gaps
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

### Club names (current clubs)

**`club_name`** comes from Wikipedia FIFA 2026 squad pages (not stale SoFIFA player rows). League + logo are matched from SoFIFA by club name.

```bash
npm run import:official-squads   # refresh squads + clubs from Wikipedia
npm run sync:clubs               # update CSV + players_seed.json
npm run go-live:publish          # if signed-in users use Firestore
```

## `squads.csv` columns

`team_id`, `shirt_number`, `player_name`, `position`, `rarity`

- `position`: Goalkeeper | Defender | Midfielder | Forward  
- `rarity`: common | rare | epic | legendary  

## `player_ratings.csv` columns

`player_id`, `club_name`, `club_league`, `club_logo_url`, `overall`, `pace`, `shooting`, `passing`, `dribbling`, `defending`, `physical`, `diving`, `handling`, `kicking`, `reflexes`, `speed`, `positioning`

- **Goalkeepers:** fill `diving` … `positioning`; outfield columns may be `0`.  
- **Outfield:** fill `pace` … `physical`; goalkeeper columns may be `0`.  
- `player_id` must match seed (`{team_id}_{slugified_name}`).

`ratingsComplete` in JSON is set automatically when overall and the correct six attributes for that position are all &gt; 0.

## Sticker images (fal.ai / Imagen 4)

**Full go-live steps:** [`docs/GO_LIVE.md`](../docs/GO_LIVE.md)

```bash
npm run go-live:check              # preflight
npm run go-live:images:pilot       # 5 test images
npm run go-live:images             # all players
npm run go-live:images:emblems     # 48 crests
npm run go-live:publish            # Firestore for signed-in users
```

Default model: **Imagen 4** (`fal-ai/imagen4/preview`), 3:4, ~$0.04/image (~$60 budget for full album).

Player prompts use only **player name** + **country**. Refresh without wiping ratings: `npm run update:sticker-prompts`.

Progress: `data/sticker_images_progress.json` (gitignored).
