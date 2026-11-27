import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.21"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.11"

    application
}

group = "no.nav.frivillig.skattetrekk"

repositories {
    mavenCentral()
}


val toolsJacksonVersion = "3.2.4"
val jacksonAnnotationVersion = "2.22"
val kotlinLoggingVersion = "3.0.5"
val logbackVersion = "1.6.6"
val logstashVersion = "9.0"
val micrometerVersion = "1.17.2"
val mockkVersion = "1.14.11"
val ktorVersion = "3.6.1"

dependencies {

    implementation(platform("io.netty:netty-bom:4.2.16.Final"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("com.auth0:jwks-rsa:0.22.1")
    implementation("com.nimbusds:nimbus-jose-jwt:10.4.2")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson3:$ktorVersion")

    implementation("tools.jackson.core:jackson-databind:$toolsJacksonVersion")
    implementation("tools.jackson.core:jackson-core:$toolsJacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonAnnotationVersion")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("ch.qos.logback:logback-core:$logbackVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    // Micrometer / Prometheus
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
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

    matching { it.name.contains("ktlint", ignoreCase = true) }.configureEach {
        enabled = false
    }

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
