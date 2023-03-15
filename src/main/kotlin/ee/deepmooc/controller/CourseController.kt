package ee.deepmooc.controller

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.Course
import ee.deepmooc.model.RequiredAccessLevel
import ee.deepmooc.service.CourseService
import io.jooby.MediaType
import io.jooby.annotations.*
import javax.inject.Inject

@Path("/api/{courseCode}")
class CourseController @Inject constructor(
    private val courseService: CourseService
) {

    @GET("/users")
    @RequiredAccessLevel(AccessLevel.TEACHER)
    @Produces(MediaType.JSON)
    fun getAllUsers(@PathParam courseCode: String): Course {
        return courseService.getCourseWithUsers(courseCode)
    }

    @GET("/students")
    @RequiredAccessLevel(AccessLevel.TEACHER)
    @Produces(MediaType.JSON)
    fun getStudents(@PathParam courseCode: String): Course {
        return courseService.getCourseWithStudents(courseCode)
    }
}
