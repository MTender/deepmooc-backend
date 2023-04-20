package ee.deepmooc.controller

import ee.deepmooc.auth.getUid
import ee.deepmooc.controller.CourseController.Companion.COURSE_CODE_PATH_PARAM
import ee.deepmooc.model.*
import ee.deepmooc.service.InputVerificationService
import ee.deepmooc.service.RegistrationService
import io.jooby.MediaType
import io.jooby.annotations.*
import kotlinx.serialization.Serializable
import org.pac4j.core.profile.CommonProfile
import javax.inject.Inject

@Path("/api/{$COURSE_CODE_PATH_PARAM}")
class CourseController @Inject constructor(
    private val registrationService: RegistrationService,
    private val verificationService: InputVerificationService
) {

    @GET("/my-groups")
    @MinimumAccessLevel(AccessLevel.STUDENT)
    @Produces(MediaType.JSON)
    fun myGroups(@ContextParam courseId: Long, @ContextParam("user") profile: CommonProfile): List<Group> {
        return registrationService.getGroupsOfUser(profile.getUid(), courseId)
    }

    @GET("/groups/{userId}")
    @Produces(MediaType.JSON)
    fun groupsOfUser(@ContextParam courseId: Long, @PathParam("userId") userId: Long): List<Group> {
        return registrationService.getGroupsOfUser(userId, courseId)
    }

    @GET("/registrations")
    @Produces(MediaType.JSON)
    fun registrations(@ContextParam courseId: Long): List<CourseRegistration> {
        return registrationService.getRegistrations(courseId)
    }

    @GET("/students")
    @Produces(MediaType.JSON)
    fun students(@ContextParam courseId: Long): List<User> {
        return registrationService.getRegistrations(courseId)
            .filter { it.accessLevel == AccessLevel.STUDENT }
            .map { it.user!! }
    }

    @POST("/add-to-course")
    @Consumes(MediaType.JSON)
    fun addToCourse(@ContextParam courseId: Long, body: UserIdsAndAccessLevelInput) {
        registrationService.addUsersToCourse(body.userIds, courseId, body.accessLevel)
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
    data class UserIdsAndAccessLevelInput(val userIds: Set<Long>, val accessLevel: AccessLevel)

    @Serializable
    data class UserIdsAndGroupIdInput(val userIds: Set<Long>, val groupId: Long)
}
