val joobyVersion: String by project
val kotlinVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.8.10"
    kotlin("kapt") version "1.8.10"
    kotlin("plugin.jpa") version "1.8.10"
    kotlin("plugin.allopen") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("io.jooby.run") version "2.16.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "ee.deepmooc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://build.shibboleth.net/nexus/content/repositories/releases/")
    }
}

application {
    mainClass.set("ee.deepmooc.AppKt")
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    kapt("io.jooby:jooby-apt:$joobyVersion")

    implementation("io.jooby:jooby-jetty:$joobyVersion")
    implementation("io.jooby:jooby-guice:$joobyVersion")

    implementation("io.jooby:jooby-pac4j:$joobyVersion")
    implementation("org.pac4j:pac4j-saml:4.5.6")

    implementation("io.jooby:jooby-hikari:$joobyVersion")
    implementation("io.jooby:jooby-hibernate:$joobyVersion")
    implementation("org.postgresql:postgresql:42.5.4")

    implementation("ch.qos.logback:logback-classic:1.4.5")
//    implementation("org.hibernate.validator:hibernate-validator:6.2.5.Final")
//    implementation("org.glassfish:javax.el:3.0.0")

    testImplementation(kotlin("test-junit5", kotlinVersion))
    testImplementation("io.jooby:jooby-test:$joobyVersion")
    testImplementation("com.squareup.okhttp3:okhttp:4.10.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

// for annotation processing
kapt {
    arguments {
        arg("jooby.incremental", true)
        arg("jooby.services", true)
        arg("jooby.debug", false)
    }
}

// for integration tests
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.javaParameters = true
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embedabble")
}