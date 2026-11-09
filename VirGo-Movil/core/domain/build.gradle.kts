plugins {
    id("virgo.jvm.library")
}

dependencies {
    api(libs.javax.inject)
    implementation(project(":core:common"))
    testImplementation(libs.junit)
}
