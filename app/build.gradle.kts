/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.10.2/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    // MockK for mocking
    testImplementation(libs.io.mockk)

    // Cucumber with JUnit engine for larger, flow tests.
    testImplementation("io.cucumber:cucumber-java:7.20.1")
    testImplementation("io.cucumber:cucumber-junit:7.20.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)

    // Easy logger with SLF4J backed.
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.exeval.AppKt"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
    finalizedBy("cucumberTest")
}

sourceSets["main"].java {
    srcDir("build/generated/out/kotlin")
}

configurations.register("cucumberRuntime") {
    extendsFrom(configurations["testImplementation"])
}

tasks.register("cucumberTest") {
    group = "verification"
    description = "Runs compiler flow tests on example programs."
    dependsOn("assemble", "testClasses")
    var tags = "not @notImplemented"
    if (project.hasProperty("cucumberTags")) {
        tags = project.property("cucumberTags") as String
    }
    doLast {
        javaexec {
            mainClass = "io.cucumber.core.cli.Main"
            classpath = configurations["cucumberRuntime"] + sourceSets["main"].output + sourceSets["test"].output
            args = listOf(
                "--plugin", "pretty",
                "--plugin", "html:build/reports/cucumber-report.html",
                "--tags", tags
            )
        }
    }
}
