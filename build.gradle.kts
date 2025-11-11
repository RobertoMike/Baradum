plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false
    id("com.vanniktech.maven.publish") version "0.30.0" apply false
}

group = "io.github.robertomike"
version = "3.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

// This is now a parent project - actual artifacts are in submodules
var jdkCompileVersion = 17
var hefestoVersion = "3.0.0"
