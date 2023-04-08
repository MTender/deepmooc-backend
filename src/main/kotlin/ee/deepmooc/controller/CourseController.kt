package ee.deepmooc.controller

import ee.deepmooc.controller.CourseController.Companion.COURSE_CODE_PATH_PARAM
import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.CourseRegistration
import ee.deepmooc.service.InputVerificationService
import ee.deepmooc.service.RegistrationService
import io.jooby.MediaType
import io.jooby.annotations.*
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Path("/api/{$COURSE_CODE_PATH_PARAM}")
class CourseController @Inject constructor(
    private val registrationService: RegistrationService,
    private val verificationService: InputVerificationService
) {

    @GET("/users")
    @Produces(MediaType.JSON)
    fun getAllUsers(@ContextParam courseId: Long): List<CourseRegistration> {
        return registrationService.getRegisteredUsers(courseId)
    }

    @GET("/students")
    @Produces(MediaType.JSON)
    fun getStudents(@ContextParam courseId: Long): List<CourseRegistration> {
        return registrationService.getRegisteredStudents(courseId)
    }

    @POST("/add-students-to-course")
    @Consumes(MediaType.JSON)
    fun addStudentsToCourse(@ContextParam courseId: Long, body: UserIdsInput) {
        registrationService.addUsersToCourse(body.userIds, courseId, AccessLevel.STUDENT)
    }

    @DELETE("/remove-from-course")
    @Consumes(MediaType.JSON)
    fun removeStudentsFromCourse(@ContextParam courseId: Long, body: UserIdsInput) {
        registrationService.removeUsersFromCourse(body.userIds.toList(), courseId)
    }

    @POST("/add-to-group")
    @Consumes(MediaType.JSON)
    fun addStudentsToGroup(@ContextParam courseId: Long, body: UserIdsAndGroupIdInput) {
        val userIdsList = body.userIds.toList()

        verificationService.verifyGroupMatchesCourse(body.groupId, courseId)
        verificationService.verifyUsersRegisteredToCourse(userIdsList, courseId)

        registrationService.addUsersToGroup(userIdsList, body.groupId)
    }

    @DELETE("/remove-from-group")
    @Consumes(MediaType.JSON)
    fun removeStudentsFromGroup(@ContextParam courseId: Long, body: UserIdsAndGroupIdInput) {
        val userIdsList = body.userIds.toList()

        verificationService.verifyGroupMatchesCourse(body.groupId, courseId)
        verificationService.verifyUsersRegisteredToCourse(userIdsList, courseId)

        registrationService.removeUsersFromGroup(userIdsList, body.groupId)
    }

    companion object {
        const val COURSE_CODE_PATH_PARAM = "courseCode"
    }

    @Serializable
    data class UserIdsInput(val userIds: Set<Long>)

    @Serializable
    data class UserIdsAndGroupIdInput(val userIds: Set<Long>, val groupId: Long)
}
