rootProject.name = "deepmooc-backend"

pluginManagement {
    val kotlinVersion: String by settings
    val joobyVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("io.jooby.openAPI") version joobyVersion
    }
}