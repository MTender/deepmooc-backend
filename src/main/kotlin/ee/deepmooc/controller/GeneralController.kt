package ee.deepmooc.controller

import ee.deepmooc.auth.getUid
import ee.deepmooc.dto.CourseRegistration
import ee.deepmooc.dto.User
import ee.deepmooc.service.RegistrationService
import ee.deepmooc.service.UserService
import io.jooby.annotations.ContextParam
import io.jooby.annotations.GET
import io.jooby.annotations.Path
import io.swagger.v3.oas.annotations.tags.Tag
import org.pac4j.core.profile.CommonProfile
import javax.inject.Inject

@Path("/api/general")
@Tag(name = "General")
class GeneralController @Inject constructor(
    private val userService: UserService,
    private val registrationService: RegistrationService
) {

    @GET("/my-courses")
    fun myCourses(@ContextParam("user") profile: CommonProfile): List<CourseRegistration> {
        return registrationService.getCourseRegistrationsOfUser(profile.getUid())
    }

    @GET("/me")
    fun me(@ContextParam("user") profile: CommonProfile): User {
        return userService.getUser(profile.getUid())
    }
}