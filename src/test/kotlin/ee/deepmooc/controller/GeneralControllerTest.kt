package ee.deepmooc.controller

import ee.deepmooc.App
import io.jooby.JoobyTest
import io.jooby.StatusCode
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@JoobyTest(App::class)
class GeneralControllerTest(
    private val serverPath: String
) {

    companion object {
        private val client = OkHttpClient()
    }

    private fun getPath(endpoint: String): String {
        return "${serverPath}api/general$endpoint"
    }

    @Test
    fun testGetAllUsers() {
        val req = Request.Builder()
            .header("Authentication", "test")
            .url(getPath("/my-registrations"))
            .build()

        client.newCall(req).execute().use { rsp ->
            assertEquals(StatusCode.OK.value(), rsp.code)
            println(rsp.body!!.string())
        }
    }

    @Test
    fun testGetStudents() {
    }
}