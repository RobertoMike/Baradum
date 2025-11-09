plugins {
    kotlin("jvm") version "2.0.21"
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "io.github.robertomike"
version = "3.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

var jdkCompileVersion = 17

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    
    api("com.fasterxml.jackson.core:jackson-databind:2.14.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.4.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation(kotlin("test"))
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(jdkCompileVersion)
}

publishing {
    publications {
        register("library", MavenPublication::class) {
            from(components["java"])

            groupId = "$group"
            artifactId = "baradum-core"
            version = version

            pom {
                name = "Baradum Core"
                description = "Core abstractions for Baradum filtering library"
                url = "https://github.com/RobertoMike/Baradum"
                inceptionYear = "2024"

                licenses {
                    license {
                        name = "MIT License"
                        url = "http://www.opensource.org/licenses/mit-license.php"
                    }
                }
                developers {
                    developer {
                        name = "Roberto Micheletti"
                        email = "rmworking@hotmail.com"
                        organization = "Roberto Micheletti"
                        organizationUrl = "https://github.com/RobertoMike"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/RobertoMike/Baradum.git"
                    developerConnection = "scm:git:ssh://github.com:RobertoMike/Baradum.git"
                    url = "https://github.com/RobertoMike/Baradum"
                }
            }
        }
    }
}
