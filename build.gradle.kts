plugins {
    kotlin("jvm") version "1.3.21"
    kotlin("kapt") version "1.3.21"
    id("maven")
}

allprojects {
    repositories {
        maven(url = "https://jitpack.io")
        mavenCentral()
        jcenter()
    }
}