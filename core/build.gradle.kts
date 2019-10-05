plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
}

group = "pro.horovodovodo4ka.kodable"

dependencies {
    implementation(kotlin("stdlib"))

    api(Config.Libs.fluidJson) { forceKotlin() }
}

apply(from = "${project.rootDir}/mavenizer/gradle-mavenizer.gradle")