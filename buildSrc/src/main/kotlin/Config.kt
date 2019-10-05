import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency

object Config {
    const val kotlinVersion = "1.3.50"

    object Libs {
        const val fluidJson = "com.github.fluidsonic:fluid-json-basic:0.9.24"
        const val kotlinpoet = "com.squareup:kotlinpoet:0.7.0"
        const val metadata = "com.github.colriot:poc-kotlinx-metadata:v1.4.0"
        const val autoservice = "com.google.auto.service:auto-service:1.0-rc4"
    }
}


fun ExternalModuleDependency.forceKotlin(): ModuleDependency = exclude(mapOf("group" to "org.jetbrains.kotlin"))