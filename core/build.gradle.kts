plugins {
    kotlin("jvm")
    kotlin("kapt")
    maven
}

group = "pro.horovodovodo4ka.kodable"

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Config.Libs.fluidJson) { forceKotlin() }
}

apply(from = "${project.rootDir}/mavenizer/gradle-mavenizer.gradle")