plugins {
    id("virgo.jvm.library")
    alias(libs.plugins.pitest)
}

pitest {
    targetClasses.set(listOf("mx.virgo.core.domain.*"))
}

dependencies {
    api(libs.javax.inject)
    implementation(project(":core:common"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
