import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("org.jetbrains.kotlinx.kover") version "0.9.7"

    application
}

group = "no.nav.frivillig.skattetrekk"

repositories {
    mavenCentral()
}

val toolsJacksonVersion = "3.0.4"
val kotlinLoggingVersion = "3.0.5"
val janionVersion = "3.1.12"
val logbackVersion = "1.5.29"
val logstashVersion = "9.0"
val micrometerVersion = "1.16.3"
val mockkVersion = "1.14.9"

dependencies {

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("tools.jackson.core:jackson-databind:$toolsJacksonVersion")
    implementation("tools.jackson.core:jackson-core:$toolsJacksonVersion")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    runtimeOnly("org.codehaus.janino:janino:$janionVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("ch.qos.logback:logback-core:$logbackVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    // Micrometer / Prometheus
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test-classic") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
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
        languageVersion.set(JavaLanguageVersion.of(25))
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
        gradleVersion = "9.3.0"
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
