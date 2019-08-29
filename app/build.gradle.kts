import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation (project(":core"))
    implementation (project(":sample_dependency"))

    kapt (project(":processor"))
}

sourceSets[MAIN_SOURCE_SET_NAME].java {
    srcDir("$buildDir/generated/source/kaptKotlin")
}