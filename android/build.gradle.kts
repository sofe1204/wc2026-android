plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0" apply false
    id("com.google.dagger.hilt.android") version "2.58" apply false
    // Google services Gradle plugin — reads google-services.json for Firebase SDKs
    id("com.google.gms.google-services") version "4.4.4" apply false
}

tasks.register<Exec>("syncProject") {
    group = "worldcup"
    description = "Validate and sync project.config.json across Android, Functions, and Firebase"
    workingDir = rootProject.projectDir.parentFile
    commandLine("python3", "scripts/sync_project.py")
}
