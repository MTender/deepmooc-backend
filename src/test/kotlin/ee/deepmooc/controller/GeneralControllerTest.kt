package ee.deepmooc.controller

import ee.deepmooc.App
import ee.deepmooc.IntegrationTest
import ee.deepmooc.model.Course
import ee.deepmooc.model.CourseRegistration
import ee.deepmooc.model.User
import io.jooby.JoobyTest
import io.jooby.StatusCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import okhttp3.Request
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalSerializationApi
@JoobyTest(App::class)
class GeneralControllerTest(
    private val serverPath: String
) : IntegrationTest() {

    @Test
    fun testGetMyCourses() {
        val userEntity = testUsers.single { it.username == "test2" }

        val courseRegistrations = testCourseRegistrations.filter { it.userId == userEntity.id }.map { cre ->
            CourseRegistration(
                cre,
                Course(testCourses.single { it.id == cre.courseId })
            )
        }.toSet()

        val url = getUrl("/my-courses")

        val req = Request.Builder()
            .header("Authentication", userEntity.username)
            .url(url)
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                courseRegistrations,
                format.decodeFromString<Set<CourseRegistration>>(rsp.body!!.string())
            )
        }
    }

    @Test
    fun testGetMe() {
        val userEntity = testUsers.single { it.username == "test2" }
        val user = User(userEntity)

        val req = Request.Builder()
            .header("Authentication", userEntity.username)
            .url(getUrl("/me"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                user,
                format.decodeFromString<User>(rsp.body!!.string())
            )
        }
    }

    private fun getUrl(endpoint: String): String = "${serverPath}api/general$endpoint"
}