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
var hefestoVersion = "3.0.0"

dependencies {
    // Core module dependency
    api(project(":baradum-core"))
    
    // Hefesto dependencies
    implementation("io.github.robertomike:hefesto-hibernate:$hefestoVersion")
    api("io.github.robertomike:hefesto-hibernate:$hefestoVersion")

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
    
    // Database for testing
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.hibernate.orm:hibernate-core:6.2.7.Final")
    testImplementation("org.hibernate.orm:hibernate-hikaricp:6.2.7.Final")
    testImplementation("com.zaxxer:HikariCP:5.0.1")
    
    // Logging for Hibernate SQL debugging
    testImplementation("org.apache.logging.log4j:log4j-core:2.20.0")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    
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
            artifactId = "baradum-hefesto"
            version = version

            pom {
                name = "Baradum Hefesto"
                description = "Hefesto implementation for Baradum filtering library"
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
