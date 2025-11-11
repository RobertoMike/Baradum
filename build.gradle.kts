plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false

    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "io.github.robertomike"
version = "3.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

// This is now a parent project - actual artifacts are in submodules
// Keep this file for backwards compatibility but mark as deprecated

var jdkCompileVersion = 17
var hefestoVersion = "3.0.0"

// Root project is now just an aggregator - no dependencies or tasks
