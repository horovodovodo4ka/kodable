plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    testImplementation(kotlin("stdlib"))

    testImplementation (project(":core"))

    kaptTest(project(":processor"))
}