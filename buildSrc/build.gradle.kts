plugins {
    `kotlin-dsl`
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:${libs.plugins.spotless.get().version}")
    implementation("io.github.gradle-nexus:publish-plugin:2.0.0")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:4.2.0")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:6.2.0.5505")
}

spotless {
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
        indentWithSpaces()
    }
}
