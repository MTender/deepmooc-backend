package ee.deepmooc.dto

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.UserEntity
import kotlinx.serialization.Serializable

@Serializable
data class RegisteredUser(
    val id: Long,
    val username: String,
    val accessLevel: AccessLevel
) {

    constructor(userEntity: UserEntity, accessLevel: AccessLevel) : this(
        id = userEntity.id,
        username = userEntity.username,
        accessLevel = accessLevel
    )
}