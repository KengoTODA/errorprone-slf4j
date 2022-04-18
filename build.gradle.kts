import net.ltgt.gradle.errorprone.errorprone
import org.sonarqube.gradle.SonarQubeTask

plugins {
    `jacoco`
    `java-library`
    `maven-publish`
    `signing`
    `conventions`
}

group = "jp.skypencil.errorprone.slf4j"

val errorproneVersion = "2.13.1"

dependencies {
    compileOnly("com.google.errorprone:error_prone_check_api:$errorproneVersion")
    errorprone("com.google.errorprone:error_prone_core:$errorproneVersion")
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

val signingKey: String? by project
val signingPassword: String? by project

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "9"
        targetCompatibility = "9"
        options.compilerArgs.addAll(exportsArgs)
        options.errorprone.disableWarningsInGeneratedCode.set(true)
    }
    withType<Javadoc> {
        dependsOn(createJavadocOptionFile)
        options.optionFiles(addExportsFile)
    }
    withType<Sign> {
        onlyIf {
            signingKey.isNullOrEmpty().not() && signingPassword.isNullOrEmpty().not()
        }
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
                url.set("https://github.com/KengoTODA/errorprone-slf4j")
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

signing {
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}
