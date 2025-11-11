plugins {
    kotlin("jvm") version "2.0.21"
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.robertomike"
version = "3.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.11")
    implementation(project(":baradum-hefesto"))

    api("org.apache.tomcat.embed:tomcat-embed-core:10.1.11")
    api(project(":baradum-hefesto"))

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.4.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
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
        languageVersion.set(JavaLanguageVersion.of(17))
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
        artifactId = "baradum-apache-tomcat",
        version = project.version.toString()
    )
    
    pom {
        name.set("Baradum - Apache Tomcat")
        description.set("This is an open-source Java library for creation of query with requests and hefesto - Apache Tomcat support")
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
