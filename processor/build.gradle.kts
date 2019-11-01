plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))

    implementation(kotlin("stdlib"))

    // kotlin metadata
    implementation(Config.Libs.metadata) { forceKotlin() }

    // generate code
    implementation(Config.Libs.kotlinpoet) { forceKotlin() }

    // processor generator
    compileOnly(Config.Libs.autoservice) { forceKotlin() }
    kapt(Config.Libs.autoservice) { forceKotlin() }
}


apply(from = "${project.rootDir}/mavenizer/gradle-mavenizer.gradle")