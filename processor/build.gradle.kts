plugins {
    kotlin("jvm")
    kotlin("kapt")
    maven
}

dependencies {
    implementation(project(":core"))

    implementation(kotlin("stdlib"))

    // kotlin metadata
    implementation(Config.Libs.metadata) { forceKotlin() }

    // used in kodable and processor has links to it
    implementation(Config.Libs.fluidJson) { forceKotlin() }

    // generate code
    implementation(Config.Libs.kotlinpoet) { forceKotlin() }

    // processor generator
    compileOnly(Config.Libs.autoservice) { forceKotlin() }
    kapt(Config.Libs.autoservice) { forceKotlin() }
}


apply(from = "${project.rootDir}/mavenizer/gradle-mavenizer.gradle")