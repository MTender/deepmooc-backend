package ee.deepmooc.model

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@KomapperEntity(aliases = ["courses"])
@KomapperTable(name = "courses")
data class CourseEntity(

    @KomapperId
    @KomapperAutoIncrement
    val id: Long = 0,

    val name: String,

    val code: String
)