import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    checkstyle
    jacoco
    id("org.sonarqube") version "6.2.0.5505"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.h2database:h2:2.3.232")
    implementation("com.zaxxer:HikariCP:6.3.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    implementation("org.apache.commons:commons-text:1.13.1")

//    implementation("org.slf4j:slf4j-simple:2.0.17")

    implementation("io.javalin:javalin:6.6.0")
    implementation("io.javalin:javalin-bundle:6.6.0")
    implementation("io.javalin:javalin-rendering:6.6.0")
    testImplementation("io.javalin:javalin-testtools:6.6.0")

    implementation("ch.qos.logback:logback-classic:1.5.12")

    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("gg.jte:jte:3.2.1")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation ("com.konghq:unirest-java:3.14.2")
    implementation ("org.jsoup:jsoup:1.17.2") // Для парсинга HTML
    testImplementation ("com.squareup.okhttp3:mockwebserver:4.12.0")

    implementation ("org.eclipse.jetty:jetty-server:11.0.24")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "hexlet.code.App"
}

tasks.test {
    useJUnitPlatform()
    // https://technology.lastminute.com/junit5-kotlin-and-gradle-dsl/
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        // showStackTraces = true
        // showCauses = true
        showStandardStreams = true
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

sonar {
    properties {
        property("sonar.projectKey", "Shturman13_java-project-72")
        property("sonar.organization", "shturman13")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/test/jacocoTestReport.xml")
    }
}
