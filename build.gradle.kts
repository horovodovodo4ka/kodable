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
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(Config.kotlinVersion)
            }
        }
    }
}
