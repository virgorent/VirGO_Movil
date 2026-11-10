import java.util.Properties

plugins {
    id("virgo.android.library")
    id("virgo.hilt")
    alias(libs.plugins.kotlin.serialization)
}

val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
val debugBaseUrl = localProperties.getProperty("virgo.apiBaseUrl") ?: "http://10.0.2.2:5122/"
val releaseBaseUrl = System.getenv("VIRGO_API_BASE_URL") ?: ""
if (releaseBaseUrl.isNotEmpty() && !releaseBaseUrl.startsWith("https://")) {
    throw GradleException("VIRGO_API_BASE_URL de release debe empezar con https:// (ADR-08)")
}

android {
    namespace = "mx.virgo.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"$debugBaseUrl\"")
        buildConfigField("boolean", "IS_DEBUGGABLE", "true")
    }

    buildTypes {
        release {
            buildConfigField("String", "API_BASE_URL", "\"$releaseBaseUrl\"")
            buildConfigField("boolean", "IS_DEBUGGABLE", "false")
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    api(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
