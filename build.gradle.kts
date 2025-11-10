plugins {
    kotlin("jvm") version "2.0.21"
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.30.0"
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

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("$jdkCompileVersion"))
    }
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    
    // Only sign if credentials are available (CI environment)
    if (project.hasProperty("signing.keyId")) {
        signAllPublications()
    }
    
    coordinates(
        groupId = project.group.toString(),
        artifactId = "baradum",
        version = project.version.toString()
    )
    
    pom {
        name.set("Baradum")
        description.set("This is an open-source Java library for creation of query with requests and hefesto")
        url.set("https://github.com/RobertoMike/Baradum")
        inceptionYear.set("2024")
        
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        
        developers {
            developer {
                id.set("robertomike")
                name.set("Roberto Micheletti")
                email.set("rmworking@hotmail.com")
                url.set("https://github.com/RobertoMike")
            }
        }
        
        scm {
            connection.set("scm:git:git://github.com/RobertoMike/Baradum.git")
            developerConnection.set("scm:git:ssh://git@github.com/RobertoMike/Baradum.git")
            url.set("https://github.com/RobertoMike/Baradum")
        }
    }
}
