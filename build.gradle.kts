plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "eu.qwsome"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.stormpot)
    api(libs.slf4j)

    testImplementation(libs.bundles.testing)
    testImplementation(libs.logback.classic)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name = "Stormpot Logging"
                description = "Logging library for Stormpot object pool"
                url = "https://github.com/sgflt/stormpot-logging"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/sgflt/stormpot-logging.git"
                    developerConnection = "scm:git:ssh://github.com:sgflt/stormpot-logging.git"
                    url = "https://github.com/sgflt/stormpot-logging/tree/main"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
