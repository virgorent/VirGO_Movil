plugins {
    id("virgo.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "mx.virgo.core.network"
}

dependencies {
    implementation(project(":core:common"))
    api(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
}
