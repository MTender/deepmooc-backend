val joobyVersion: String by project
val kotlinVersion: String by project
val komapperVersion: String by project

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")

    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.google.devtools.ksp") version "1.8.20-1.0.11"
    id("io.jooby.openAPI") version "2.16.3"
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    kapt("io.jooby:jooby-apt:$joobyVersion")

    implementation("io.jooby:jooby-jetty:$joobyVersion")
    implementation("io.jooby:jooby-guice:$joobyVersion")

    implementation("io.jooby:jooby-pac4j:$joobyVersion")
    implementation("org.pac4j:pac4j-saml:4.5.7")

    platform("org.komapper:komapper-platform:$komapperVersion").let {
        implementation(it)
        ksp(it)
    }
    implementation("org.komapper:komapper-starter-jdbc:$komapperVersion")
    implementation("org.komapper:komapper-dialect-postgresql-jdbc:$komapperVersion")
    ksp("org.komapper:komapper-processor:$komapperVersion")

    implementation("io.jooby:jooby-hikari:$joobyVersion")
    implementation("org.postgresql:postgresql:42.7.3")

    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("io.jooby:jooby-swagger-ui:$joobyVersion")

    testImplementation(kotlin("test-junit5", kotlinVersion))
    testImplementation("io.jooby:jooby-test:$joobyVersion")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
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

// openAPI
tasks.joobyRun {
    dependsOn(tasks.openAPI)
}

tasks.jar {
    dependsOn(tasks.openAPI)
}

tasks.shadowJar {
    mergeServiceFiles()
    dependsOn(tasks.openAPI)
}