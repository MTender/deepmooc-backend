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
    val id: Long,

    val username: String
)

@Serializable
data class User(
    val username: String,
    val courseRegistrations: Set<CourseRegistration>?
) {

    constructor(userEntity: UserEntity) : this(userEntity.username, null)

    constructor(userEntity: UserEntity, courseRegistrations: Set<CourseRegistration>) : this(
        userEntity.username,
        courseRegistrations
    )
}