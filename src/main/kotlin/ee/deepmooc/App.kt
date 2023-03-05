package ee.deepmooc

import ee.deepmooc.auth.AuthExtension
import ee.deepmooc.auth.ServiceProviderMetadataController
import ee.deepmooc.controller.TestController
import ee.deepmooc.controller.UserController
import io.jooby.Kooby
import io.jooby.di.GuiceModule
import io.jooby.hibernate.HibernateModule
import io.jooby.hibernate.TransactionalRequest
import io.jooby.hikari.HikariModule
import io.jooby.json.JacksonModule
import io.jooby.runApp

class App : Kooby({
    install(JacksonModule())
    install(GuiceModule())
    install(HikariModule())
    install(HibernateModule())
    decorator(TransactionalRequest())

    mvc(TestController::class)
    mvc(ServiceProviderMetadataController::class)

    install(AuthExtension())

    mvc(UserController::class)
})

fun main(args: Array<String>) {
    runApp(args, App::class)
}
