package ee.deepmooc

import ee.deepmooc.controller.TestController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UnitTest {
    @Test
    fun welcome() {
        assertEquals("Welcome to Jooby!", TestController().sayHi())
    }
}
