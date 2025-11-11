plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("kapt")
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.robertomike"
version = "3.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

var jdkCompileVersion = 17
var querydslVersion = "5.0.0"

dependencies {
    // Core module dependency
    api(project(":baradum-core"))
    
    // QueryDSL dependencies
    implementation("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
    api("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
    kapt("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
    
    // Jakarta Persistence API
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    api("jakarta.persistence:jakarta.persistence-api:3.1.0")

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
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("reflect"))
    
    // Servlet API for tests
    testImplementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.11")
    
    // Database for testing
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.hibernate.orm:hibernate-core:6.2.7.Final")
    testImplementation("org.hibernate.orm:hibernate-hikaricp:6.2.7.Final")
    testImplementation("com.zaxxer:HikariCP:5.0.1")
    testImplementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    
    // QueryDSL APT processor for generating Q-classes
    kaptTest("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
    kaptTest("org.hibernate.orm:hibernate-jpamodelgen:6.2.7.Final")
    
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

// Configure KAPT for QueryDSL Q-class generation
kapt {
    arguments {
        arg("querydsl.entityAccessors", "true")
    }
    correctErrorTypes = true
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jdkCompileVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    
    // Only sign if credentials are available (CI environment)
    if (project.hasProperty("signing.keyId")) {
        signAllPublications()
    }
    
    coordinates(
        groupId = project.group.toString(),
        artifactId = "baradum-querydsl",
        version = project.version.toString()
    )
    
    pom {
        name.set("Baradum QueryDSL")
        description.set("QueryDSL implementation for Baradum filtering library - provides type-safe queries with QueryDSL 5.0+")
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

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}
