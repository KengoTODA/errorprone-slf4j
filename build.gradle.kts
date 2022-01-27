plugins {
    `jacoco`
    `java-library`
    `maven-publish`
    `signing`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.sonarqube") version "3.3"
}

val autoServiceVersion = "1.0.1"
val errorproneVersion = "2.11.0"

group = "jp.skypencil.errorprone.slf4j"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service:${autoServiceVersion}")
    compileOnly("com.google.auto.service:auto-service:${autoServiceVersion}")
    compileOnly("com.google.errorprone:error_prone_check_api:${errorproneVersion}")
    testImplementation("junit:junit:4.13")
    testImplementation("org.slf4j:slf4j-api:2.0.0-alpha6")
    testImplementation("com.google.errorprone:error_prone_check_api:${errorproneVersion}")
    testImplementation("com.google.errorprone:error_prone_test_helpers:${errorproneVersion}")
}

tasks {
    test {
        useJUnit()
    }
    withType<JavaCompile> {
        options.fork(mapOf(Pair("jvmArgs", listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
