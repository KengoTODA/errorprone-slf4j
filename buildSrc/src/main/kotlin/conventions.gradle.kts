plugins {
    `java`
    id("com.diffplug.spotless")
    id("io.github.gradle-nexus.publish-plugin")
    id("net.ltgt.errorprone")
    id("org.sonarqube")
}

val autoServiceVersion = "1.1.1"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service:$autoServiceVersion")
    compileOnly("com.google.auto.service:auto-service:$autoServiceVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.slf4j:slf4j-api:2.0.9")
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat()
    }
    kotlinGradle {
        target("build.gradle.kts")
        ktlint()
        indentWithSpaces()
    }
}

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectKey", "jp.skypencil.errorprone.slf4j:errorprone-slf4j")
        property("sonar.organization", "kengotoda-github")
    }
}
