plugins {
    kotlin("jvm") version "2.0.21"

    id("java-library")
    id("maven-publish")
    id("signing")
    id("org.jreleaser") version "1.15.0"
}

group = "io.github.robertomike"
version = "2.1.2"

repositories {
    mavenCentral()
}

var jdkCompileVersion = 17
var hefestoVersion = "2.1.3"

dependencies {
    implementation("io.github.robertomike:hefesto-hibernate:$hefestoVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")

    api("io.github.robertomike:hefesto-hibernate:$hefestoVersion")
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
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
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
            artifactId = "baradum"
            version = version

            pom {
                name = "Baradum"
                description = "This is an open-source Java library for creation of query with requests and hefesto"
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
    repositories {
        maven {
            name = "OSSRH"
            url = uri(layout.buildDirectory.dir("repos/OSSRH"))
        }
    }
}

if (!project.hasProperty("local")) {
    signing {
        setRequired { !version.toString().endsWith("SNAPSHOT") }
        sign(publishing.publications["library"])
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("$jdkCompileVersion"))
    }
}

jreleaser {
    project {
        description = "This is an open-source Java library for creation of query with requests and hefesto"
        authors.add("Roberto Micheletti")
        license = "MIT"
        links {
            homepage = "https://github.com/RobertoMike/Baradum"
        }
        inceptionYear = "2024"
    }
    
    signing {
        active = org.jreleaser.model.Active.NEVER
        armored = true
    }
    
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = org.jreleaser.model.Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/repos/OSSRH")
                    applyMavenCentralRules = true
                    sign = false
                    checksums = false
                    sourceJar = false
                    javadocJar = false
                }
            }
        }
    }
}
