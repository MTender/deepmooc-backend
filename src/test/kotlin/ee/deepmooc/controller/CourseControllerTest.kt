package ee.deepmooc.controller

import ee.deepmooc.App
import ee.deepmooc.IntegrationTest
import ee.deepmooc.dto.Group
import ee.deepmooc.dto.RegisteredUser
import ee.deepmooc.dto.User
import ee.deepmooc.model.*
import io.jooby.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@ExperimentalSerializationApi
@JoobyTest(App::class)
class CourseControllerTest(
    private val serverPath: String,
    private val application: Jooby
) : IntegrationTest() {

    @Test
    fun testNonexistentCourse() {
        // test
        val req = Request.Builder()
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/my-groups", "NO.SUCH.COURSE"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testGetMyGroups() {
        // prep
        val groups = testGroups.filter { it.name in setOf("test_I_1", "test_I_3") }.map { Group(it) }.toSet()

        // test
        val req = Request.Builder()
            .header(AUTHENTICATION_HEADER, "test2")
            .url(getUrl("/my-groups"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                groups,
                format.decodeFromString<Set<Group>>(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testGetGroupsOfUser() {
        // prep
        val userEntity = testUsers.single { it.username == "test2" }
        val groups = testGroups.filter { it.name in setOf("test_I_1", "test_I_3") }.map { Group(it) }.toSet()

        // test
        val req = Request.Builder()
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/groups/${userEntity.id}"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                groups,
                format.decodeFromString<Set<Group>>(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testGetGroupsOfUserAsStudent() {
        // prep
        val userEntity = testUsers.single { it.username == "test2" }

        // test
        val req = Request.Builder()
            .header("Authentication", "test2")
            .url(getUrl("/groups/${userEntity.id}"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testGetGroupsOfNonexistentUser() {
        // prep
        val userId = 99

        // test
        val req = Request.Builder()
            .header("Authentication", "test1")
            .url(getUrl("/groups/$userId"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "No such user registered to the course",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testGetRegistrations() {
        // prep
        val userIds = testUsers.filter { it.username in setOf("test1", "test2") }.map { it.id }
        val registrations = testCourseRegistrations.filter { it.userId in userIds && it.courseId == getTestCourseId() }
            .map { registration ->
                RegisteredUser(
                    testUsers.single { it.id == registration.userId },
                    registration.accessLevel
                )
            }.toSet()

        // test
        val req = Request.Builder()
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/registered-users"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                registrations,
                format.decodeFromString<Set<RegisteredUser>>(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testGetRegistrationsAsStudent() {
        // test
        val req = Request.Builder()
            .header("Authentication", "test2")
            .url(getUrl("/registered-users"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testGetStudents() {
        // prep
        val users = testUsers.filter { it.username == "test2" }.map { User(it) }.toSet()

        // test
        val req = Request.Builder()
            .header("Authentication", "test1")
            .url(getUrl("/students"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                users,
                format.decodeFromString<Set<User>>(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testGetStudentsAsStudent() {
        // test
        val req = Request.Builder()
            .header("Authentication", "test2")
            .url(getUrl("/students"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testAddStudentToCourse() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test3" }.id)

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndAccessLevelInput(userIds, AccessLevel.STUDENT))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                testUsers.filter { it.id in userIds }.map { RegisteredUser(it, AccessLevel.STUDENT) }.toSet(),
                format.decodeFromString<Set<RegisteredUser>>(rsp.body!!.string())
            )
        }

        // cleanup
        getDb().runQuery {
            QueryDsl.delete(cr)
                .where {
                    cr.courseId eq getTestCourseId()
                    and {
                        cr.userId inList userIds.toList()
                    }
                }
        }
    }

    @Test
    fun testAddTeacherToCourse() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test3" }.id)

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndAccessLevelInput(userIds, AccessLevel.TEACHER))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                testUsers.filter { it.id in userIds }.map { RegisteredUser(it, AccessLevel.TEACHER) }.toSet(),
                format.decodeFromString<Set<RegisteredUser>>(rsp.body!!.string())
            )
        }

        // cleanup
        getDb().runQuery {
            QueryDsl.delete(cr)
                .where {
                    cr.courseId eq getTestCourseId()
                    and {
                        cr.userId inList userIds.toList()
                    }
                }
        }
    }

    @Test
    fun testAddRegisteredUserToCourse() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test2" }.id)

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndAccessLevelInput(userIds, AccessLevel.TEACHER))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                testUsers.filter { it.id in userIds }.map { RegisteredUser(it, AccessLevel.TEACHER) }.toSet(),
                format.decodeFromString<Set<RegisteredUser>>(rsp.body!!.string())
            )
        }

        // cleanup
        getDb().runQuery {
            QueryDsl.update(cr)
                .set {
                    cr.accessLevel eq AccessLevel.STUDENT
                }
                .where {
                    cr.courseId eq getTestCourseId()
                    and {
                        cr.userId inList userIds.toList()
                    }
                }
        }
    }

    @Test
    fun testAddStudentToCourseAsStudent() {
        // test
        val req = Request.Builder()
            .post("".toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test2")
            .url(getUrl("/add-to-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testAddNonexistentUserToCourse() {
        // prep
        val userIds = setOf(99L)

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndAccessLevelInput(userIds, AccessLevel.STUDENT))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "At least one of the users does not exist",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testAddUserToCourseWithNonexistentAccessLevel() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test2" }.id)

        // test
        val json = "{\"userIds\":$userIds,\"accessLevel\":\"NONEXISTENT\"}"

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertContains(
                getBadRequestMessage(rsp.body!!.string()),
                "AccessLevel does not contain element with name &apos;NONEXISTENT&apos;"
            )
        }
    }

    @Test
    fun testAddUserToCourseWithoutAccessLevel() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test2" }.id)

        // test
        val json = "{\"userIds\":$userIds}"

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertContains(
                getBadRequestMessage(rsp.body!!.string()),
                "Field &apos;accessLevel&apos; is required"
            )
        }
    }

    @Test
    fun testRemoveStudentFromCourse() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test3" }.id)

        // setup
        getDb().runQuery {
            QueryDsl.insert(cr)
                .multiple(userIds.map {
                    CourseRegistrationEntity(
                        userId = it,
                        courseId = getTestCourseId(),
                        accessLevel = AccessLevel.STUDENT
                    )
                })
        }

        // test
        val json = Json.encodeToString(CourseController.UserIdsInput(userIds))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.NO_CONTENT.value(), rsp.code)
        }
    }

    @Test
    fun testRemoveTeacherFromCourse() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test3" }.id)

        // setup
        getDb().runQuery {
            QueryDsl.insert(cr)
                .multiple(userIds.map {
                    CourseRegistrationEntity(
                        userId = it,
                        courseId = getTestCourseId(),
                        accessLevel = AccessLevel.TEACHER
                    )
                })
        }

        // test
        val json = Json.encodeToString(CourseController.UserIdsInput(userIds))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.NO_CONTENT.value(), rsp.code)
        }
    }

    @Test
    fun testRemoveUnregisteredUserFromCourse() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test3" }.id)

        // test
        val json = Json.encodeToString(CourseController.UserIdsInput(userIds))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.NO_CONTENT.value(), rsp.code)
        }
    }

    @Test
    fun testRemoveUserFromCourseAsStudent() {
        // test
        val req = Request.Builder()
            .delete("".toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test2")
            .url(getUrl("/remove-from-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testRemoveNonexistentUserFromCourse() {
        // prep
        val userIds = setOf(99L)

        // test
        val json = Json.encodeToString(CourseController.UserIdsInput(userIds))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-course"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "At least one of the users does not exist",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testAddUserToGroup() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test2" }.id)
        val group = testGroups.single { it.name == "test_I_2" }

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, group.id))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                testUsers.filter { it.id in userIds }.map { User(it) }.toSet(),
                format.decodeFromString<Set<User>>(rsp.body!!.string())
            )
        }

        // cleanup
        val db = getDb()

        val courseRegistrationIds: List<Long?> = db.runQuery {
            QueryDsl.from(cr)
                .where {
                    cr.courseId eq getTestCourseId()
                    and {
                        cr.userId inList userIds.toList()
                    }
                }
                .select(cr.id)
        }

        db.runQuery {
            QueryDsl.delete(gr)
                .where {
                    gr.courseRegistrationId inList courseRegistrationIds
                    and {
                        gr.groupId eq group.id
                    }
                }
        }
    }

    @Test
    fun testAddUserInGroupToGroup() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test2" }.id)
        val group = testGroups.single { it.name == "test_I_1" }

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, group.id))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                testUsers.filter { it.id in userIds }.map { User(it) }.toSet(),
                format.decodeFromString<Set<User>>(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testAddStudentToGroupAsStudent() {
        // test
        val req = Request.Builder()
            .post("".toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test2")
            .url(getUrl("/add-to-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testAddUnregisteredUserToGroup() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test3" }.id)
        val group = testGroups.single { it.name == "test_I_2" }

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, group.id))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "At least one of the users is not registered to the course",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testAddUserToUnrelatedGroup() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test2" }.id)
        val group = testGroups.single { it.name == "test_I_2" }

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, group.id))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-group", testCourses[1].code))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "Group id does not match course code",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testAddNonexistentUserToGroup() {
        // prep
        val userIds = setOf(99L)
        val group = testGroups.single { it.name == "test_I_2" }

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, group.id))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-to-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "At least one of the users does not exist",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testRemoveUserFromGroup() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test2" }.id)
        val groupId = testGroups.single { it.name == "test_I_2" }.id

        // setup
        val db = getDb()

        val courseRegistrationIds: List<Long?> = db.runQuery {
            QueryDsl.from(cr)
                .where {
                    cr.courseId eq getTestCourseId()
                    and {
                        cr.userId inList userIds.toList()
                    }
                }
                .select(cr.id)
        }

        db.runQuery {
            QueryDsl.insert(gr)
                .multiple(courseRegistrationIds.map {
                    GroupRegistrationEntity(
                        groupId = groupId,
                        courseRegistrationId = it!!
                    )
                })
        }

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, groupId))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.NO_CONTENT.value(), rsp.code)
        }
    }

    @Test
    fun testRemoveUserNotInGroupFromGroup() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test1" }.id)
        val groupId = testGroups.single { it.name == "test_I_2" }.id

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, groupId))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.NO_CONTENT.value(), rsp.code)
        }
    }

    @Test
    fun testRemoveUserFromGroupAsStudent() {
        // test
        val req = Request.Builder()
            .delete("".toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test2")
            .url(getUrl("/remove-from-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testRemoveUnregisteredUserFromGroup() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test3" }.id)
        val groupId = testGroups.single { it.name == "test_I_2" }.id

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, groupId))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "At least one of the users is not registered to the course",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testRemoveUserFromUnrelatedGroup() {
        // prep
        val userIds = setOf(testUsers.single { it.username == "test2" }.id)
        val groupId = testGroups.single { it.name == "test_I_2" }.id

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, groupId))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-group", testCourses[1].code))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "Group id does not match course code",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testRemoveNonexistentUserFromGroup() {
        // prep
        val userIds = setOf(99L)
        val groupId = testGroups.single { it.name == "test_I_2" }.id

        // test
        val json = Json.encodeToString(CourseController.UserIdsAndGroupIdInput(userIds, groupId))

        val req = Request.Builder()
            .delete(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/remove-from-group"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.BAD_REQUEST.value(), rsp.code)
            assertEquals(
                "At least one of the users does not exist",
                getBadRequestMessage(rsp.body!!.string())
            )
        }
    }

    private fun getUrl(endpoint: String, courseCode: String = TEST_COURSE_CODE): String {
        return "${serverPath}api/$courseCode$endpoint"
    }

    private fun getDb(): JdbcDatabase {
        return application.services.get(ServiceKey.key(JdbcDatabase::class.java))
    }

    private fun getTestCourseId(): Long {
        return testCourses.single { it.code == TEST_COURSE_CODE }.id
    }

    companion object {

        private const val AUTHENTICATION_HEADER = "Authentication"
        private const val TEST_COURSE_CODE = "TEST.01.001"

        private val BAD_REQUEST_MESSAGE_REGEX = Regex("<h2>message: (.+)</h2>")

        private fun getBadRequestMessage(responseString: String): String {
            return BAD_REQUEST_MESSAGE_REGEX.find(responseString)!!.groups[1]!!.value
        }
    }
}