package ee.deepmooc.model

import kotlinx.serialization.Serializable
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@KomapperEntity(aliases = ["courses"])
@KomapperTable(name = "courses")
data class CourseEntity(

    @KomapperId
    @KomapperAutoIncrement
    val id: Long,

    val name: String,

    val code: String
)

@Serializable
data class Course(
    val name: String,
    val code: String,
    val courseRegistrations: Set<CourseRegistration>?
) {

    constructor(entity: CourseEntity) : this(entity.name, entity.code, null)

    constructor(entity: CourseEntity, courseRegistrations: Set<CourseRegistration>) : this(
        entity.name,
        entity.code,
        courseRegistrations
    )
}