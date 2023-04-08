package ee.deepmooc.controller

import ee.deepmooc.App
import ee.deepmooc.IntegrationTest
import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.model.GroupRegistrationEntity
import io.jooby.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@JoobyTest(App::class)
class CourseControllerTest(
    private val serverPath: String,
    private val application: Jooby
) : IntegrationTest() {

    @Test
    fun testGetAllUsers() {
        // test
        val req = Request.Builder()
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/users"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                "[" +
                        "{\"user\":{\"id\":9,\"username\":\"test1\"},\"accessLevel\":\"TEACHER\"}," +
                        "{\"user\":{\"id\":10,\"username\":\"test2\"},\"accessLevel\":\"STUDENT\"}" +
                        "]",
                rsp.body!!.string()
            )
        }
    }

    @Test
    fun testGetAllUsersAsStudent() {
        // test
        val req = Request.Builder()
            .header("Authentication", "test2")
            .url(getUrl("/users"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testGetAllStudents() {
        // test
        val req = Request.Builder()
            .header("Authentication", "test1")
            .url(getUrl("/students"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            val body = rsp.body!!.string()
            assertEquals(
                "[" +
                        "{\"user\":{\"id\":10,\"username\":\"test2\"},\"accessLevel\":\"STUDENT\"}" +
                        "]",
                body
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
        val json = Json.encodeToString(CourseController.UserIdsInput(userIds))

        val req = Request.Builder()
            .post(json.toRequestBody(MediaType.JSON.toMediaType()))
            .header(AUTHENTICATION_HEADER, "test1")
            .url(getUrl("/add-students-to-course"))
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
            .url(getUrl("/add-students-to-course"))
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