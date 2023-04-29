plugins {
    `java-library`
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.thegatesdev"
version = "0.3"
description = "The official Witheronia Maze gamemode plugin"
java.sourceCompatibility = JavaVersion.VERSION_17

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    api("com.github.stefvanschie.inventoryframework:IF:0.10.8")
    compileOnly("dev.jorel:commandapi-core:8.8.0")

    api("io.github.thegatesdev:maze-generator:1.1")
    compileOnly("io.github.thegatesdev:threshold:0.2")
    compileOnly("io.github.thegatesdev:actionable:1.2")
    compileOnly("io.github.thegatesdev:eventador:1.4.2")
    compileOnly("io.github.thegatesdev:stacker:0.9.2")
}

tasks{
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.19"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    shadowJar{
        minimize()
        dependencies{
            include(dependency("com.github.stefvanschie.inventoryframework:IF"))
            include(dependency("io.github.thegatesdev:maze-generator"))
        }
    }

    register<Copy>("copyJarToLocalServer") {
        from(jar)
        into("D:\\Coding\\Minecraft\\SERVER\\plugins")
    }
}