# Generated Kotlin SDK (do not edit by hand)

This folder is populated by the Firebase CLI:

```bash
firebase dataconnect:sdk:generate
# or
firebase deploy --only dataconnect
```

Expected output (Kotlin):

- `DefaultConnector.kt` — type-safe access to queries/mutations
- Query types: `ListTeamsQuery`, `ListPlayersByTeamQuery`, etc.

After generation, set in `app/build.gradle.kts`:

```kotlin
buildConfigField("boolean", "USE_SQL_CONNECT", "true")
```

Then sync Gradle and rebuild.
