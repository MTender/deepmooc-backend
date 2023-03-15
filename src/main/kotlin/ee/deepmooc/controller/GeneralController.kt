package ee.deepmooc.controller

import ee.deepmooc.model.User
import ee.deepmooc.modules.getUid
import ee.deepmooc.service.UserService
import io.jooby.MediaType
import io.jooby.annotations.ContextParam
import io.jooby.annotations.GET
import io.jooby.annotations.Path
import io.jooby.annotations.Produces
import org.pac4j.saml.profile.SAML2Profile
import javax.inject.Inject

@Path("/api/general")
class GeneralController @Inject constructor(
    private val userService: UserService
) {

    @GET("/my-courses")
    @Produces(MediaType.JSON)
    fun myCourses(@ContextParam("user") profile: SAML2Profile): User {
        return userService.getUserWithCourses(profile.getUid())
    }
}