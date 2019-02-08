plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven")
}

val forceKotlin: ExternalModuleDependency.() -> Unit = { exclude("org.jetbrains.kotlin") }

dependencies {
    implementation(project(":core"))

    implementation(kotlin("stdlib"))

    // kotlin metadata
    implementation ("com.github.colriot:poc-kotlinx-metadata:v1.4.0", forceKotlin)

    // used in kodable and processor has links to it
    implementation ("com.github.fluidsonic:fluid-json-basic:0.9.10", forceKotlin)

    // generate code
    implementation ("com.squareup:kotlinpoet:0.7.0", forceKotlin)

    // processor generator
    "com.google.auto.service:auto-service:1.0-rc4".apply {
        compileOnly (this, forceKotlin)
        kapt (this, forceKotlin)
    }
}
