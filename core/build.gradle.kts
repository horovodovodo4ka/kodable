plugins {
    kotlin("jvm")
    kotlin("kapt")
    maven
}

group = "pro.horovodovodo4ka.kodable"

dependencies {
    implementation(kotlin("stdlib"))
    
    api(Config.Libs.fluidJson) {
        forceKotlin()
        isTransitive = true
    }
}
