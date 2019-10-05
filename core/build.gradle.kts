plugins {
    kotlin("jvm")
    maven
}

dependencies {
    implementation(kotlin("stdlib"))

    api(Config.Libs.fluidJson)
}

apply(from = "${project.rootDir}/mavenizer/gradle-mavenizer.gradle")