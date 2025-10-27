import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.3"

    application
}

group = "no.nav.frivillig.skattetrekk"

repositories {
    mavenCentral()
}

val jacksonVersion = "2.20.0"
val logstashVersion = "9.0"
val logbackVersion = "1.5.20"
val micrometerVersion = "1.15.5"
val kotlinLoggingVersion = "3.0.5"
val janionVersion = "3.1.12"
val mockkVersion = "1.14.6"

dependencies {

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    runtimeOnly("org.codehaus.janino:janino:$janionVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // Micrometer / Prometheus
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
}

// Vulnerability fix because of id("org.jlleitschuh.gradle.ktlint") uses ch.qos.logback:logback-classic:1.3.5
configurations.ktlint {
    resolutionStrategy.force("ch.qos.logback:logback-classic:$logbackVersion")
}

application {
    mainClass.set("no.nav.sokos.frivillig.skattetrekk.backend.ApplicationKt")
}

sourceSets {
    main {
        java {
            srcDirs("${layout.buildDirectory.get()}/generated/src/main/kotlin")
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {

    withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xannotation-default-target=param-property",
            )
        }

        dependsOn("ktlintFormat")
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
        reports.forEach { it.required.set(false) }

        finalizedBy(koverHtmlReport)
    }

    withType<Wrapper> {
        gradleVersion = "9.1.0"
    }

    named("build") {
        dependsOn("copyPreCommitHook")
    }

    register<Copy>("copyPreCommitHook") {
        from(".scripts/pre-commit")
        into(".git/hooks")
        filePermissions {
            user { execute = true }
        }
        doFirst { println("Installing git hooks...") }
        doLast { println("Git hooks installed successfully.") }
        description = "Copy pre-commit hook to .git/hooks"
        group = "git hooks"
        outputs.upToDateWhen { false }
    }
}
