package ee.deepmooc

import ee.deepmooc.controller.CourseController
import ee.deepmooc.controller.GeneralController
import ee.deepmooc.controller.ServiceProviderMetadataController
import ee.deepmooc.controller.TestController
import ee.deepmooc.modules.KomapperModule
import ee.deepmooc.modules.KomapperTransactionalRequest
import ee.deepmooc.modules.KotlinxSerializationModule
import ee.deepmooc.modules.SamlAuthModule
import io.jooby.Kooby
import io.jooby.di.GuiceModule
import io.jooby.hikari.HikariModule
import io.jooby.runApp

class App : Kooby({
    install(KotlinxSerializationModule())
    install(GuiceModule())
    install(HikariModule())
    install(KomapperModule())
    decorator(KomapperTransactionalRequest())

    mvc(TestController::class)
    mvc(ServiceProviderMetadataController::class)

    install(SamlAuthModule())

    mvc(GeneralController::class)
    mvc(CourseController::class)
})

fun main(args: Array<String>) {
    runApp(args, App::class)
}
