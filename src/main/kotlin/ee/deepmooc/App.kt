package ee.deepmooc

import ee.deepmooc.controller.CourseController
import ee.deepmooc.controller.GeneralController
import ee.deepmooc.modules.*
import io.jooby.OpenAPIModule
import io.jooby.guice.GuiceModule
import io.jooby.hikari.HikariModule
import io.jooby.kt.Kooby
import io.jooby.kt.runApp
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
    use(KomapperTransactionalRequest())

    install(OpenAPIModule())

    val testAuth: Boolean = config.hasPath("useTestAuth") && config.getBoolean("useTestAuth")

    if (testAuth) {
        install(TestAuthModule())
    } else {
        install(SamlAuthModule())
    }

    mvc(GeneralController::class)
    mvc(CourseController::class)

    assets("/saml-sp-metadata", config.getString("saml.spMetadataPath"))
})

fun main(args: Array<String>) {
    runApp(args, ::App)
}