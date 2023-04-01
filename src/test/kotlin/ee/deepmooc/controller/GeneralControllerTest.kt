package ee.deepmooc.controller

import ee.deepmooc.App
import ee.deepmooc.IntegrationTest
import io.jooby.JoobyTest
import io.jooby.StatusCode
import okhttp3.Request
import kotlin.test.Test
import kotlin.test.assertEquals

@JoobyTest(App::class)
class GeneralControllerTest : IntegrationTest() {

    @Test
    fun testGetMyRegistrations(serverPath: String) {
        val url = getUrl(serverPath, "/my-registrations")

        val req = Request.Builder()
            .header("Authentication", "test2")
            .url(url)
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                "[" +
                        "{\"course\":" +
                        "{\"id\":2,\"name\":\"Test course I\",\"code\":\"TEST.01.001\"}," +
                        "\"accessLevel\":\"STUDENT\"," +
                        "\"groups\":[{\"id\":1,\"name\":\"test_I_1\"},{\"id\":3,\"name\":\"test_I_3\"}]}," +
                        "{\"course\":" +
                        "{\"id\":3,\"name\":\"Test course II\",\"code\":\"TEST.01.002\"}," +
                        "\"accessLevel\":\"STUDENT\"," +
                        "\"groups\":[]}" +
                        "]",
                rsp.body!!.string()
            )
        }
    }

    @Test
    fun testGetMe(serverPath: String) {
        val req = Request.Builder()
            .header("Authentication", "test2")
            .url(getUrl(serverPath, "/me"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            assertEquals(
                "{\"id\":10,\"username\":\"test2\"}",
                rsp.body!!.string()
            )
        }
    }

    private fun getUrl(serverPath: String, endpoint: String): String = "${serverPath}api/general$endpoint"
}