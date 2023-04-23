package ee.deepmooc

import com.typesafe.config.Config
import ee.deepmooc.controller.CourseController
import ee.deepmooc.controller.GeneralController
import ee.deepmooc.controller.ServiceProviderMetadataController
import ee.deepmooc.modules.*
import io.jooby.Kooby
import io.jooby.OpenAPIModule
import io.jooby.di.GuiceModule
import io.jooby.hikari.HikariModule
import io.jooby.runApp
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "DeepMOOC API",
        description = "This is the documentation for the DeepMOOC backend web API"
    )
)
class App : Kooby({
    install(KotlinxSerializationModule())
    install(GuiceModule())
    install(HikariModule())
    install(KomapperModule())
    decorator(KomapperTransactionalRequest())

    install(OpenAPIModule())

    mvc(ServiceProviderMetadataController::class)

    val testAuth: Boolean = config.getBooleanOrDefault("useTestAuth", false)

    if (testAuth) {
        install(TestAuthModule())
    } else {
        install(SamlAuthModule())
    }

    mvc(GeneralController::class)

    mvc(CourseController::class)
})

fun main(args: Array<String>) {
    runApp(args, App::class)
}

fun Config.getBooleanOrDefault(path: String, default: Boolean): Boolean {
    if (this.hasPath(path)) return this.getBoolean(path)
    return default
}