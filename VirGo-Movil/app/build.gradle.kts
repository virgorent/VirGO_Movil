plugins {
    id("virgo.android.application")
    id("virgo.android.compose")
    id("virgo.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "mx.virgo.app"

    defaultConfig {
        applicationId = "mx.virgo.app"
        versionCode = 1
        versionName = "0.1.0"
    }

    val signingKeyStorePath = System.getenv("SIGNING_KEYSTORE_FILE")
    val signingKeyAliasEnv = System.getenv("SIGNING_KEY_ALIAS")
    val signingKeyPasswordEnv = System.getenv("SIGNING_KEY_PASSWORD")
    val signingStorePasswordEnv = System.getenv("SIGNING_STORE_PASSWORD")
    val releaseSigningMissing =
        listOf(signingKeyStorePath, signingKeyAliasEnv, signingKeyPasswordEnv, signingStorePasswordEnv)
            .any { it.isNullOrBlank() }
    val releaseBuildRequested = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
    if (releaseBuildRequested && releaseSigningMissing) {
        throw GradleException(
            "Falta la firma de release: define SIGNING_KEYSTORE_FILE, SIGNING_KEY_ALIAS, SIGNING_KEY_PASSWORD y " +
                "SIGNING_STORE_PASSWORD. Release nunca se firma con la llave de debug (T026).",
        )
    }

    signingConfigs {
        if (!releaseSigningMissing) {
            create("release") {
                storeFile = file(signingKeyStorePath!!)
                storePassword = signingStorePasswordEnv
                keyAlias = signingKeyAliasEnv
                keyPassword = signingKeyPasswordEnv
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (!releaseSigningMissing) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<Test>().configureEach {
    jvmArgs(
        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
    )
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
