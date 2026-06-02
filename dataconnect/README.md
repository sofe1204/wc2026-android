# Firebase SQL Connect — World Cup 2026

This folder connects the app to your Cloud SQL (PostgreSQL) instance provisioned in Firebase Console.

## Console configuration (already set)

| Setting | Value |
|---------|--------|
| Firebase project | `wc-2026-3110f` |
| Location | `us-east4` (Northern Virginia) |
| Service ID | `wc-2026-3110f-service` |
| Cloud SQL instance | `wc-2026-3110f-instance` |
| Database | `wc-2026-3110f-database` |

Config file: [`dataconnect.yaml`](dataconnect.yaml)

## After provisioning completes (~20 min)

```bash
cd "/Users/sofe/Desktop/Workspace/WC app"
firebase login
firebase use wc-2026-3110f

# Preview SQL migrations from GraphQL schema
firebase dataconnect:sql:diff

# Apply schema to Cloud SQL
firebase dataconnect:sql:migrate

# Grant IAM roles on the instance (if prompted)
firebase dataconnect:sql:setup

# Deploy service + connectors + generate Kotlin SDK
firebase deploy --only dataconnect
```

## Generated Kotlin Android SDK

Generate type-safe Kotlin (not JavaScript):

```bash
firebase dataconnect:sdk:generate
```

Output directory (configured in `connector/default/connector.yaml`):

`android/app/src/main/java/com/techmomentum/wc2026/dataconnect/generated/`

This **replaces** the placeholder `DefaultConnector.kt` stub.

Then in [`android/app/build.gradle.kts`](../android/app/build.gradle.kts):

```kotlin
buildConfigField("boolean", "USE_SQL_CONNECT", "true")
```

Kotlin usage in app:

```kotlin
val teams = DefaultConnector.instance.listTeams.execute().data.teams
```

See [`SqlConnectCatalogDataSource.kt`](../android/app/src/main/java/com/techmomentum/wc2026/data/dataconnect/SqlConnectCatalogDataSource.kt).

## Architecture note

| Data | Recommended store |
|------|-------------------|
| Teams, players, stickers (catalog) | SQL Connect / Cloud SQL |
| User profile, ownership, rewards | Cloud Functions + SQL or Firestore (anti-cheat) |
| Pack open, daily, slot rewards | **Cloud Functions only** (do not trust client) |

Firestore rules and Functions in this repo remain valid during migration.

## Schema layout

- [`schema/catalog.gql`](schema/catalog.gql) — `teams`, `players`, `stickers`
- [`schema/users.gql`](schema/users.gql) — `users`, `user_stickers`, `pack_history`, `slot_history`
- [`connector/default/queries.gql`](connector/default/queries.gql) — public catalog + user reads
- [`connector/default/mutations.gql`](connector/default/mutations.gql) — `EnsureUserProfile`

## Seed catalog into PostgreSQL

Until a SQL seed script exists, use existing tools:

1. Deploy SQL Connect schema (`sql:migrate`)
2. Run Cloud Function seed (`seedTeams`, `seedPlayers`, `seedStickers`) **or**
3. Import from `android/app/src/main/assets/seed/*.json` via a future migration script

TODO: Add `scripts/seed_sql_connect.py` to bulk-insert from JSON after schema deploy.
