package ee.deepmooc.model

import kotlinx.serialization.Serializable
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@KomapperEntity(aliases = ["users"])
@KomapperTable(name = "users")
data class UserEntity(
    @KomapperId
    @KomapperAutoIncrement
    val id: Long = 0,

    val username: String
)

@Serializable
data class User(
    val id: Long,
    val username: String,
    val courseRegistrations: Set<CourseRegistration>?
) {

    constructor(userEntity: UserEntity, courseRegistrations: Set<CourseRegistration>? = null) : this(
        userEntity.id,
        userEntity.username,
        courseRegistrations
    )
}