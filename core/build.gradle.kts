plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(kotlin("stdlib"))

    api(Config.Libs.fluidJson)
}

apply(from = "${project.rootDir}/mavenizer/gradle-mavenizer.gradle")