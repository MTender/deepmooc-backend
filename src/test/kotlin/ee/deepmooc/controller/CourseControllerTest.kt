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
import kotlin.test.assertEquals

@ExperimentalSerializationApi
@JoobyTest(App::class)
class CourseControllerTest(
    private val serverPath: String,
    private val application: Jooby
) : IntegrationTest() {

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
    fun testAddStudentsToCourse() {
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
    fun testAddStudentsToCourseAsStudent() {
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
    fun testRemoveFromCourse() {
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
    fun testRemoveFromCourseAsStudent() {
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
    fun testAddStudentsToGroup() {
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
    fun testAddStudentsToGroupAsStudent() {
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
    fun testRemoveFromGroup() {
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
    fun testRemoveFromGroupAsStudent() {
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

    private fun getUrl(endpoint: String): String {
        return "${serverPath}api/$TEST_COURSE_CODE$endpoint"
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
    }
}