package ee.deepmooc.controller

import ee.deepmooc.auth.MinimumAccessLevel
import ee.deepmooc.auth.getUid
import ee.deepmooc.controller.CourseController.Companion.COURSE_CODE_PATH_PARAM
import ee.deepmooc.dto.Group
import ee.deepmooc.dto.RegisteredUser
import ee.deepmooc.dto.User
import ee.deepmooc.model.*
import ee.deepmooc.service.InputVerificationService
import ee.deepmooc.service.RegistrationService
import io.jooby.annotations.*
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.serialization.Serializable
import org.pac4j.core.profile.CommonProfile
import javax.inject.Inject

@Path("/api/{$COURSE_CODE_PATH_PARAM}")
@Tag(name = "Course specific")
class CourseController @Inject constructor(
    private val registrationService: RegistrationService,
    private val verificationService: InputVerificationService
) {

    @GET("/my-groups")
    @MinimumAccessLevel(AccessLevel.STUDENT)
    fun myGroups(@PathParam courseCode: String, @ContextParam("user") profile: CommonProfile): List<Group> {
        val courseId = verificationService.verifyCourseCode(courseCode)

        return registrationService.getGroupsOfUser(profile.getUid(), courseId)
    }

    @GET("/groups/{userId}")
    fun groupsOfUser(@PathParam courseCode: String, @PathParam userId: Long): List<Group> {
        val courseId = verificationService.verifyCourseCode(courseCode)

        return registrationService.getGroupsOfUser(userId, courseId)
    }

    @GET("/registered-users")
    fun registrations(@PathParam courseCode: String): List<RegisteredUser> {
        val courseId = verificationService.verifyCourseCode(courseCode)

        return registrationService.getRegisteredUsers(courseId)
    }

    @GET("/students")
    fun students(@PathParam courseCode: String): List<User> {
        val courseId = verificationService.verifyCourseCode(courseCode)

        return registrationService.getRegisteredUsers(courseId)
            .filter { it.accessLevel == AccessLevel.STUDENT }
            .map { User(it) }
    }

    @POST("/add-to-course")
    fun addToCourse(@PathParam courseCode: String, body: UserIdsAndAccessLevelInput): List<RegisteredUser> {
        val courseId = verificationService.verifyCourseCode(courseCode)

        registrationService.addUsersToCourse(body.userIds, courseId, body.accessLevel)

        return registrationService.getRegisteredUsers(courseId).filter {
            it.id in body.userIds
        }
    }

    @DELETE("/remove-from-course")
    @ApiResponse(responseCode = "204")
    fun removeStudentsFromCourse(@PathParam courseCode: String, body: UserIdsInput) {
        val courseId = verificationService.verifyCourseCode(courseCode)

        registrationService.removeUsersFromCourse(body.userIds.toList(), courseId)
    }

    @POST("/add-to-group")
    fun addStudentsToGroup(@PathParam courseCode: String, body: UserIdsAndGroupIdInput): List<User> {
        val courseId = verificationService.verifyCourseCode(courseCode)

        val userIdsList = body.userIds.toList()

        verificationService.verifyGroupMatchesCourse(body.groupId, courseId)
        verificationService.verifyUsersRegisteredToCourse(userIdsList, courseId)

        registrationService.addUsersToGroup(userIdsList, body.groupId)

        return registrationService.getMembersOfGroup(body.groupId).filter {
            it.id in body.userIds
        }
    }

    @DELETE("/remove-from-group")
    @ApiResponse(responseCode = "204")
    fun removeStudentsFromGroup(@PathParam courseCode: String, body: UserIdsAndGroupIdInput) {
        val courseId = verificationService.verifyCourseCode(courseCode)

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
