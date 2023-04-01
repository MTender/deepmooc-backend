package ee.deepmooc.controller

import ee.deepmooc.App
import ee.deepmooc.IntegrationTest
import io.jooby.JoobyTest
import io.jooby.StatusCode
import okhttp3.Request
import kotlin.test.Test
import kotlin.test.assertEquals

@JoobyTest(App::class)
class CourseControllerTest : IntegrationTest() {

    @Test
    fun testGetAllUsers(serverPath: String) {
        val url = getUrl(serverPath, "TEST.01.001", "/users")

        val req = Request.Builder()
            .header("Authentication", "test1")
            .url(url)
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
    fun testGetAllUsersAsStudent(serverPath: String) {
        val url = getUrl(serverPath, "TEST.01.001", "/users")

        val req = Request.Builder()
            .header("Authentication", "test2")
            .url(url)
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    @Test
    fun testGetAllStudents(serverPath: String) {
        val url = getUrl(serverPath, "TEST.01.001", "/students")

        val req = Request.Builder()
            .header("Authentication", "test1")
            .url(url)
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                "[" +
                        "{\"user\":{\"id\":10,\"username\":\"test2\"},\"accessLevel\":\"STUDENT\"}" +
                        "]",
                rsp.body!!.string()
            )
        }
    }

    @Test
    fun testGetStudentsAsStudent(serverPath: String) {
        val url = getUrl(serverPath, "TEST.01.001", "/students")

        val req = Request.Builder()
            .header("Authentication", "test2")
            .url(url)
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.FORBIDDEN.value(), rsp.code)
        }
    }

    private fun getUrl(serverPath: String, courseCode: String, endpoint: String): String {
        return "${serverPath}api/$courseCode$endpoint"
    }
}