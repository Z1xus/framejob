import dev.kikugie.loomx.LoomCompatDependencyExtension
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi

plugins {
    java
    id("dev.kikugie.loom-back-compat")
    id("ploceus") version "1.17.6" apply false
}

val stonecutter = extensions.getByName("stonecutter") as StonecutterBuildExtension
val mcVersion = stonecutter.current.version
val isOrnithe = mcVersion == "1.8.9"
val isModern = stonecutter.current.parsed >= "26.1"
val javaVersion = if (isOrnithe || isModern) 25 else 21

group = "us.z1x"
version = "${property("mod_version")}+$mcVersion"
base.archivesName = "framejob"

if (isOrnithe) {
    pluginManager.apply("net.fabricmc.fabric-loom-remap")
    pluginManager.apply("ploceus")
    configurations.configureEach { exclude(group = "org.lwjgl.lwjgl") }
    extensions.getByType<PloceusGradleExtensionApi>().setIntermediaryGeneration(2)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)

repositories {
    maven("https://repo.polyfrost.org/releases") {
        content { includeGroup("org.polyfrost.oneconfig") }
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    if (isOrnithe) {
        mappings(project.extensions.getByType<PloceusGradleExtensionApi>().layeredMappings {
            mappings("net.ornithemc:feather-gen2:$mcVersion+build.${property("feather_build")}:v2") {
                containsUnpick()
            }
        })
    } else {
        dependencies.extensions.getByType<LoomCompatDependencyExtension>().applyMojangMappings()
    }
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    for (module in listOf("config", "config-impl")) {
        compileOnly("org.polyfrost.oneconfig:$module:${property("oneconfig_version")}") { isTransitive = false }
    }
}

tasks.processResources {
    val properties = mapOf(
        "version" to version,
        "minecraft" to if (isModern) "~$mcVersion" else mcVersion,
        "java" to javaVersion,
    )
    inputs.properties(properties)
    filesMatching(listOf("fabric.mod.json", "mixins.framejob.json")) { expand(properties) }
    from(rootProject.file("LICENSE")) { into("META-INF") }
}
