package ee.deepmooc.controller

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.CourseRegistration
import ee.deepmooc.model.RequiredAccessLevel
import ee.deepmooc.service.RegistrationService
import io.jooby.MediaType
import io.jooby.annotations.*
import javax.inject.Inject

@Path("/api/{courseCode}")
class CourseController @Inject constructor(
    private val registrationService: RegistrationService
) {

    @GET("/users")
    @RequiredAccessLevel(AccessLevel.TEACHER)
    @Produces(MediaType.JSON)
    fun getAllUsers(@PathParam courseCode: String): List<CourseRegistration> {
        return registrationService.getRegisteredUsers(courseCode)
    }

    @GET("/students")
    @RequiredAccessLevel(AccessLevel.TEACHER)
    @Produces(MediaType.JSON)
    fun getStudents(@PathParam courseCode: String): List<CourseRegistration> {
        return registrationService.getRegisteredStudents(courseCode)
    }
}
