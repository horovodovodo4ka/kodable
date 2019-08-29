plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation (project(":core"))

    kapt (project(":processor"))
}