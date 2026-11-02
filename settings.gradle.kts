pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.ornithemc.net/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter.create(rootProject) {
    version("1.8.9-ornithe", "1.8.9")
    for (mc in listOf("1.21.1", "1.21.10", "1.21.11", "26.1", "26.2", "26.3")) {
        version("$mc-fabric", mc)
    }
    vcsVersion = "26.3-fabric"
}

rootProject.name = "framejob"
