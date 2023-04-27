plugins {
    `java-library`
    java
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.5.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.thegatesdev"
version = "0.3"
description = "witheronia-maze"
java.sourceCompatibility = JavaVersion.VERSION_17

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")

    api("com.github.stefvanschie.inventoryframework:IF:0.10.8")
    api("io.github.thegatesdev:maze-generator:1.1")

    compileOnly("io.github.thegatesdev:threshold:0.2")
    compileOnly("io.github.thegatesdev:actionable:1.2")
    compileOnly("io.github.thegatesdev:eventador:1.4.2")
    compileOnly("io.github.thegatesdev:stacker:0.9.2")

    compileOnly("dev.jorel:commandapi-core:8.8.0")
}

tasks{
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
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
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }

    shadowJar{
        minimize()
        dependencies{
            include(dependency("com.github.stefvanschie.inventoryframework:IF"))
            include(dependency("io.github.thegatesdev:maze-generator"))
        }
    }

    assemble {
        dependsOn(reobfJar)
    }

    register<Copy>("copyJarToLocalServer") {
        from(jar)
        into("D:\\Coding\\Minecraft\\SERVER\\plugins")
    }
}