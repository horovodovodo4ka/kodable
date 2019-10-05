plugins {
    kotlin("jvm")
    maven
}

group = "pro.horovodovodo4ka.kodable"

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly(Config.Libs.fluidJson)
    runtime(Config.Libs.fluidJson)
}

apply(from = "${project.rootDir}/mavenizer/gradle-mavenizer.gradle")