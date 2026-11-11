plugins {
    id("virgo.jvm.library")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
