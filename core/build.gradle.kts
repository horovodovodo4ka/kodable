plugins {
    kotlin("jvm")
    kotlin("kapt")
    maven
}

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

repositories {
    mavenCentral()
    jcenter()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib"))

    // testing
    testImplementation(project(":sample_dependency"))
    kaptTest(project(":processor"))

    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
}

apply(from = "${project.rootDir}/mavenizer/gradle-mavenizer.gradle")
