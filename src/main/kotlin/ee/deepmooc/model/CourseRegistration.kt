package ee.deepmooc.model

import kotlinx.serialization.Serializable
import org.komapper.annotation.EnumType
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

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

@Serializable
data class CourseRegistration(
    val user: User?,
    val course: Course?,
    val accessLevel: AccessLevel,
    val groups: Set<Group>?
) {

    constructor(entity: CourseRegistrationEntity, course: Course, groups: Set<Group>? = null) : this(
        null,
        course,
        entity.accessLevel,
        groups
    )

    constructor(entity: CourseRegistrationEntity, user: User) : this(user, null, entity.accessLevel, null)
}