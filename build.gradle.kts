import net.ltgt.gradle.errorprone.errorprone
import org.sonarqube.gradle.SonarQubeTask

plugins {
    `jacoco`
    `java-library`
    `maven-publish`
    `signing`
    id("com.diffplug.spotless") version "6.2.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("net.ltgt.errorprone") version "2.0.2"
    id("org.sonarqube") version "3.3"
}

group = "jp.skypencil.errorprone.slf4j"
val autoServiceVersion = "1.0.1"
val errorproneVersion = "2.11.0"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service:$autoServiceVersion")
    compileOnly("com.google.auto.service:auto-service:$autoServiceVersion")
    compileOnly("com.google.errorprone:error_prone_check_api:$errorproneVersion")
    errorprone("com.google.errorprone:error_prone_core:$errorproneVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.slf4j:slf4j-api:2.0.0-alpha6")
    testImplementation("com.google.errorprone:error_prone_check_api:$errorproneVersion")
    testImplementation("com.google.errorprone:error_prone_test_helpers:$errorproneVersion")
}

val exportsArgs = listOf(
    "--add-exports",
    "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    "--add-exports",
    "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
    "--add-exports",
    "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
    "--add-exports",
    "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
    "--add-exports",
    "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
    "--add-exports",
    "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
    "--add-exports",
    "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    "--add-exports",
    "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
)

val jacocoTestReport = tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}
val addExportsFile = file("$buildDir/tmp/javadoc/add-exports.txt")
val createJavadocOptionFile by tasks.registering {
    outputs.file(addExportsFile)
    doLast {
        addExportsFile.printWriter().use { writer ->
            exportsArgs.chunked(2).forEach {
                writer.println("${it[0]}=${it[1]}")
            }
        }
    }
}
tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.compilerArgs.addAll(exportsArgs)
        options.errorprone.disableWarningsInGeneratedCode.set(true)
    }
    withType<Javadoc> {
        dependsOn(createJavadocOptionFile)
        options.optionFiles(addExportsFile)
    }
    withType<SonarQubeTask> {
        dependsOn(jacocoTestReport)
    }
    check {
        dependsOn(jacocoTestReport)
    }
    test {
        useJUnit()
        jvmArgs = exportsArgs
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
    withJavadocJar()
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
        indentWithSpaces()
    }
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectKey", "jp.skypencil.errorprone.slf4j:errorprone-slf4j")
        property("sonar.organization", "kengotoda-github")
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
            pom {
                groupId = "jp.skypencil.errorprone.slf4j"
                artifactId = "errorprone-slf4j"
                name.set("Error Prone SLF4J")
                description.set("Error Prone plugin for SLF4J")
                inceptionYear.set("2018")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("eller86")
                        name.set("Kengo TODA")
                        url.set("https://www.kengo-toda.jp/")
                        email.set("skypencil@gmail.com")
                        timezone.set("+8")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:KengoTODA/errorprone-slf4j.git")
                    developerConnection.set("scm:git:git@github.com:KengoTODA/errorprone-slf4j.git")
                    url.set("https://github.com/KengoTODA/errorprone-slf4j")
                }
                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/KengoTODA/errorprone-slf4j/issues")
                }
            }
        }
    }
}
