plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven")
}

dependencies {
    implementation(project(":core"))

    implementation(kotlin("stdlib"))

    // kotlin metadata
//    implementation ("me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0") {
//        exclude("org.jetbrains.kotlin")
////        exclude(module = "kotlin-compiler-lite")
//    }

    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.0.5")

    // used in kodable and processor has links to it
    implementation ("com.github.fluidsonic:fluid-json-basic:0.9.10") {
        exclude("org.jetbrains.kotlin")
    }

    // generate code
    implementation ("com.squareup:kotlinpoet:0.7.0") {
        exclude("org.jetbrains.kotlin")
    }

    // processor generator
    "com.google.auto.service:auto-service:1.0-rc4".apply {
        compileOnly (this) { exclude("org.jetbrains.kotlin") }
        kapt (this) { exclude("org.jetbrains.kotlin") }
    }
}
