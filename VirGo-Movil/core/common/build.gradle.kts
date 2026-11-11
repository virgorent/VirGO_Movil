plugins {
    id("virgo.jvm.library")
    alias(libs.plugins.pitest)
}

pitest {
    targetClasses.set(listOf("mx.virgo.core.common.*"))
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.hilt.core)
    api(libs.javax.inject)
    testImplementation(libs.junit)
}
