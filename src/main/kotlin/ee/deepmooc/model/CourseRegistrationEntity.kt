package ee.deepmooc.model

import org.komapper.annotation.*

@KomapperEntity(aliases = ["courseRegistrations"])
@KomapperTable(name = "course_registrations")
data class CourseRegistrationEntity(

    @KomapperId
    @KomapperAutoIncrement
    val id: Long = 0,

    val userId: Long,

    val courseId: Long,

    @KomapperEnum(EnumType.NAME)
    val accessLevel: AccessLevel
)