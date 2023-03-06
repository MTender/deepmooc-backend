package ee.deepmooc.controller

import ee.deepmooc.model.User
import io.jooby.annotations.*

@Path("/api/test")
class TestController {

    @GET
    fun sayHi(): String {
        return "Welcome to Jooby!"
    }

    @POST
    fun save(user: User) {
        println(user.toString())
    }
}
