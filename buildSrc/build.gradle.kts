plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.9.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.9.1")
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:2.0.2")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.4.0.2513")
}

spotless {
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
        indentWithSpaces()
    }
}
