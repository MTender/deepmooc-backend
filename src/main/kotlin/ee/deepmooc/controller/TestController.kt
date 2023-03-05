package ee.deepmooc.controller

import io.jooby.annotations.*

@Path("/api/test")
class TestController {

    @GET
    @Path
    fun sayHi(): String {
        return "Welcome to Jooby!"
    }
}
