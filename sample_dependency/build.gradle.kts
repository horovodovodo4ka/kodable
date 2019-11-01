plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation (project(":core"))

    kapt (project(":processor"))
}