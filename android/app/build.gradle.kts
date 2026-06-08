import groovy.json.JsonSlurper
import java.util.Properties

plugins {
    id("com.android.application")
    // Google services Gradle plugin — makes google-services.json config available to Firebase SDKs
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val useFirebaseEmulators = localProperties.getProperty("firebase.emulators", "false") == "true"

// Google official test AdMob IDs by default — override in local.properties for production.
val admobAppId = localProperties.getProperty("admob.app.id")
    ?: "ca-app-pub-3940256099942544~3347511713"
val admobRewardedUnitId = localProperties.getProperty("admob.rewarded.unit.id")
    ?: "ca-app-pub-3940256099942544/5224354917"
val admobInterstitialUnitId = localProperties.getProperty("admob.interstitial.unit.id")
    ?: "ca-app-pub-3940256099942544/1033173712"
val admobBannerUnitId = localProperties.getProperty("admob.banner.unit.id")
    ?: "ca-app-pub-3940256099942544/6300978111"

@Suppress("UNCHECKED_CAST")
val projectConfig = JsonSlurper().parseText(
    rootProject.projectDir.parentFile.resolve("project.config.json").readText(),
) as Map<String, Any>
val firebase = projectConfig["firebase"] as Map<String, Any>
val dataConnect = projectConfig["dataConnect"] as Map<String, Any>
val emulators = projectConfig["emulators"] as Map<String, Any>

fun quote(value: String) = "\"$value\""

@Suppress("UNCHECKED_CAST")
fun extractGoogleWebClientId(): String {
    val jsonFile = file("google-services.json")
    if (!jsonFile.exists()) return ""
    val root = JsonSlurper().parseText(jsonFile.readText()) as Map<String, Any>
    val clients = root["client"] as? List<*> ?: return ""
    val firstClient = clients.firstOrNull() as? Map<*, *> ?: return ""
    val oauthClients = firstClient["oauth_client"] as? List<*> ?: return ""
    for (entry in oauthClients) {
        val oauth = entry as? Map<*, *> ?: continue
        val type = oauth["client_type"]
        if (type == 3 || type == 3.0) {
            return oauth["client_id"]?.toString().orEmpty()
        }
    }
    return ""
}

val googleWebClientId = extractGoogleWebClientId()
val gcpPublicProjectId = firebase["gcpPublicProjectId"] as? String ?: "project-424696015515"
val firebaseProjectNumber = firebase["projectNumber"] as? String ?: "424696015515"

android {
    namespace = firebase["androidPackage"] as String
    compileSdk = 35

    defaultConfig {
        applicationId = firebase["androidPackage"] as String
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["admobAppId"] = admobAppId
        buildConfigField("String", "ADMOB_APP_ID", quote(admobAppId))
        buildConfigField("String", "REWARDED_AD_UNIT_ID", quote(admobRewardedUnitId))
        buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", quote(admobInterstitialUnitId))
        buildConfigField("String", "BANNER_AD_UNIT_ID", quote(admobBannerUnitId))
        buildConfigField("String", "FIREBASE_PROJECT_ID", quote(firebase["projectId"] as String))
        buildConfigField("String", "ANDROID_PACKAGE", quote(firebase["androidPackage"] as String))
        buildConfigField("String", "FUNCTIONS_REGION", quote(firebase["functionsRegion"] as String))
        buildConfigField("String", "STORAGE_BUCKET", quote(firebase["storageBucket"] as String))
        buildConfigField("String", "DATA_CONNECT_SERVICE_ID", quote(dataConnect["serviceId"] as String))
        buildConfigField("String", "DATA_CONNECT_LOCATION", quote(dataConnect["location"] as String))
        buildConfigField("String", "DATA_CONNECT_INSTANCE_ID", quote(dataConnect["instanceId"] as String))
        buildConfigField("String", "DATA_CONNECT_DATABASE", quote(dataConnect["database"] as String))
        buildConfigField("int", "EMULATOR_AUTH_PORT", "${emulators["auth"]}")
        buildConfigField("int", "EMULATOR_FUNCTIONS_PORT", "${emulators["functions"]}")
        buildConfigField("int", "EMULATOR_FIRESTORE_PORT", "${emulators["firestore"]}")
        buildConfigField("int", "EMULATOR_STORAGE_PORT", "${emulators["storage"]}")
        buildConfigField("int", "EMULATOR_DATA_CONNECT_PORT", "${emulators["dataconnect"]}")
        buildConfigField("boolean", "USE_FIREBASE_EMULATORS", useFirebaseEmulators.toString())
        // Set true after: firebase dataconnect:sdk:generate && SQL Connect deploy
        buildConfigField("boolean", "USE_SQL_CONNECT", "false")
        buildConfigField("String", "GCP_PUBLIC_PROJECT_ID", quote(gcpPublicProjectId))
        buildConfigField("String", "FIREBASE_PROJECT_NUMBER", quote(firebaseProjectNumber))
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", quote(googleWebClientId))
        buildConfigField("boolean", "GOOGLE_SIGN_IN_ENABLED", (googleWebClientId.isNotEmpty()).toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "DEBUG", "false")
            // Production ad unit IDs from local.properties (AdMobConfig.useTestAds = false).
            buildConfigField("boolean", "USE_TEST_ADMOB", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            buildConfigField("boolean", "DEBUG", "true")
            // Google sample ad units on emulator / debug installs (AdMobConfig.useTestAds = true).
            buildConfigField("boolean", "USE_TEST_ADMOB", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.4")

    implementation("com.google.dagger:hilt-android:2.58")
    kapt("com.google.dagger:hilt-android-compiler:2.58")
    kapt("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Firebase BoM — keeps all Firebase library versions compatible
    implementation(platform("com.google.firebase:firebase-bom:34.14.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-storage")
    // Firebase SQL Connect — Kotlin SDK (requires generated connector in dataconnect/generated)
    implementation("com.google.firebase:firebase-dataconnect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}

kapt {
    correctErrorTypes = true
}
