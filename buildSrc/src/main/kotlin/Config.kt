import org.gradle.api.artifacts.ExternalModuleDependency

object Config {
    const val kotlinVersion = "1.3.31"

    object Libs {
        const val fluidJson = "com.github.fluidsonic.fluid-json:fluid-json-basic:0.9.22"
        const val kotlinpoet = "com.squareup:kotlinpoet:0.7.0"
        const val metadata = "com.github.colriot:poc-kotlinx-metadata:v1.4.0"
        const val autoservice = "com.google.auto.service:auto-service:1.0-rc4"
    }
}


fun ExternalModuleDependency.forceKotlin() = { exclude(mapOf("group" to "org.jetbrains.kotlin")) }