plugins {
    kotlin("jvm")
    kotlin("kapt")
    maven
}

group = "pro.horovodovodo4ka.kodable"

dependencies {
    implementation(kotlin("stdlib"))

    // gradle 5 transitive dependency for correct .pom
    implementation(Config.Libs.fluidJson) { forceKotlin() }
    api(Config.Libs.fluidJson) { forceKotlin() }
}
