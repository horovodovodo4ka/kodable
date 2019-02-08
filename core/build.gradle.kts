plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven")
}

dependencies {
    implementation(kotlin("stdlib"))

    "com.github.fluidsonic:fluid-json-basic:0.9.10".apply {
        api (this) { exclude("org.jetbrains.kotlin") }
        implementation (this) { exclude("org.jetbrains.kotlin") }
    }
}
