plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("fabric-loom") version "1.3-SNAPSHOT"
    java
}

val archives_base_name: String by project
base.archivesName.set(archives_base_name)

val javaVersion = 17

repositories {
    maven("https://notnite.github.io/blockbuild/mvn/")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    modApi(include("pm.n2:hajlib:${property("hajlib_version")}")!!)

    implementation(include("org.xerial:sqlite-jdbc:3.42.0.0")!!)
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    processResources {
        filteringCharset = "UTF-8"
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE") {
            rename { "LICENSE_${archives_base_name}" }
        }
    }
}

java {
    withSourcesJar()
}

loom {
    accessWidenerPath = file("src/main/resources/scraft.accesswidener")
}
