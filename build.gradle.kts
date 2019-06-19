plugins {
    kotlin("jvm") version Config.kotlinVersion
    kotlin("kapt") version Config.kotlinVersion
}

allprojects {
    repositories {
        maven(url = "https://jitpack.io")
        mavenCentral()
        jcenter()
    }
}

configurations.all {
    isTransitive = true
}