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

sourceSets {
    main {
        java {
            exclude("**/archived/**")
        }
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")

    api("com.github.stefvanschie.inventoryframework:IF:0.10.8")
    compileOnly("dev.jorel:commandapi-bukkit-core:9.0.0")

    compileOnly("io.github.thegatesdev:threshold:")
    compileOnly("io.github.thegatesdev:actionable:")
    compileOnly("io.github.thegatesdev:stacker:")
}

tasks {
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "'1.20'"
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

    shadowJar {
        minimize()
        dependencies {
            include(dependency("com.github.stefvanschie.inventoryframework:IF"))
        }
    }

    register<Copy>("pluginJar") {
        from(shadowJar)
        into(buildDir.resolve("pluginJar"))
        rename { "${project.name}-plugin.jar" }
    }
}