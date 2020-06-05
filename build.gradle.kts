import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    java
    kotlin("jvm") version "1.3.31"
    id("com.google.cloud.tools.jib") version "2.3.0"
}

group = "dev.peterevans"
version = "0.1"
val buildNumber by extra("0")

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    val kotlinVersion = "1.3.+"
    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    val ktorVersion = "1.2.+"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    
    implementation("org.slf4j:slf4j-api:1.7.+")
    implementation("ch.qos.logback:logback-classic:1.2.+")

    val junitVersion = "5.3.+"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation(kotlin("test-junit5", kotlinVersion))
}

dependencyLocking {
    lockAllConfigurations()
}

application {
    mainClassName = "dev.peterevans.webservice.ServerKt"
}

jib {
    to {
        image = "peterevans/webservice"
        tags = setOf("$version", "$version.${extra["buildNumber"]}")
        auth {
            username = System.getenv("DOCKERHUB_USERNAME")
            password = System.getenv("DOCKERHUB_PASSWORD")
        }
    }
    container {
        labels = mapOf(
            "maintainer" to "Peter Evans <mail@peterevans.dev>",
            "org.opencontainers.image.title" to "webservice",
            "org.opencontainers.image.description" to "An example webservice",
            "org.opencontainers.image.version" to "$version",
            "org.opencontainers.image.authors" to "Peter Evans <mail@peterevans.dev>",
            "org.opencontainers.image.url" to "https://github.com/peter-evans/kotlin-ktor-jib",
            "org.opencontainers.image.vendor" to "https://peterevans.dev",
            "org.opencontainers.image.licenses" to "MIT"
        )
        jvmFlags = listOf(
            "-server",
            "-Djava.awt.headless=true",
            "-XX:InitialRAMFraction=2",
            "-XX:MinRAMFraction=2",
            "-XX:MaxRAMFraction=2",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=100",
            "-XX:+UseStringDeduplication"
        )
        workingDirectory = "/webservice"
        ports = listOf("8080")
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    "test"(Test::class) {
        useJUnitPlatform()
    }

    named<JavaExec>("run")
}
