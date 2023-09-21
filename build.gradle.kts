plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "com.github.phosphorIntellij"
version = "1.0.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set("Phosphor Icons")
    version.set("2022.3.3")
    type.set("IU") // Target IDE Platform
    downloadSources.set(true)

    val pluginList = listOf("com.intellij.java", "properties"
        ,"JavaScript", "Dart:223.8950", "io.flutter:75.0.2")

    plugins.set(pluginList)
}

tasks {
    // Set the JVM compatibility versions
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
    }

    runIde {
        autoReloadPlugins.set(false)
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))
        }

        resources {
            setSrcDirs(listOf("src", "resources"))
        }
    }
}